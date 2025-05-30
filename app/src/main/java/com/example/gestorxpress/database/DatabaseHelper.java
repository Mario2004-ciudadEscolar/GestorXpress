package com.example.gestorxpress.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DatabaseHelper extends SQLiteOpenHelper
{
    /**
     * Nombre de como se va llamar nuestra base de datos,
     * que en este caso es gestorxpress (Nombre de nuestra empresa)
     */
    private static final String DATABASE_NAME = "gestorxpress.db";

    /**
     * Versión de nuestra base de datos, esta versión puede cambiar
     * cuando hacemos una modificación en la bbdd, como puede ser
     * modificación de tabla o inserción de nuevos campos.
     *.
     * Devemos de tener en cuenta que si cambiamos la versión,
     * la primera clase que se ejecuta tiene que tener el metodo
     * de descargar la bbdd si no la tenemos o abrirla si la tenemos.
     *.
     * !!IMPORTANTE¡¡ Ver si cuando cambiamos la versión se nos
     * cambia automaticamente a nosotros tambien.
     */
    private static final int DATABASE_VERSION = 8;


    /**
     * Constructor donde indicamos el nombre de nuestra base de datos y la versión de bbdd,
     * que como hemos dicho antes, nos viene bien por si realizamos algún cambio en la
     * bbdd en un futuro.
     */
    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     *
     * @param
     */
    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // Activa las claves foráneas
    }

    /**
     * Metodo donde voy a crear base de datos con sus tablas y relaciones de cada tabla
     *
     * @param
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // Tabla Usuario
        db.execSQL(
                "CREATE TABLE Usuario (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT NOT NULL, " +
                        "apellido TEXT NOT NULL, " +
                        "correo TEXT NOT NULL UNIQUE, " +
                        "contrasenia TEXT NOT NULL, " +
                        "fechaRegistro TEXT NOT NULL," +
                        "fotoPerfil BLOB NOT NULL,"+ //tipo de dato que sirve para almacenar archivos binarios (IMAGENES AUDIO VIDEOS)
                        "logged_in INTEGER DEFAULT 0," +  // Agregado para el logged_in
                        "esPadre INTEGER DEFAULT 0" +
                        ");"
        );

        // Tabla Tarea
        db.execSQL(
                "CREATE TABLE Tarea (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "usuario_id INTEGER NOT NULL, " +
                        "titulo TEXT NOT NULL, " +
                        "descripcion TEXT, " +
                        "fechaLimite TEXT, " +
                        "fechaTareaFinalizada TEXT, "+
                        "prioridad TEXT, " +            // Enum simulado con TEXT
                        "estado TEXT, " +               // Enum simulado con TEXT
                        "fechaCreacion TEXT NOT NULL, " +
                        "fechaHoraInicio TEXT, "+ //Este se ha agregado
                        "FOREIGN KEY(usuario_id) REFERENCES Usuario(id) ON DELETE CASCADE" +
                        ");"
        );

        // Trigger para actualizar fechaTareaFinalizada cuando una tarea se completa
        db.execSQL("CREATE TRIGGER IF NOT EXISTS actualizar_fecha_finalizacion " +
                "AFTER UPDATE ON Tarea " +
                "FOR EACH ROW " +
                "WHEN NEW.estado = 'Completada' AND OLD.estado != 'Completada' " +
                "BEGIN " +
                "UPDATE Tarea SET fechaTareaFinalizada = DATETIME('now') WHERE id = NEW.id; " +
                "END;");


        // Tabla Notificacion
        db.execSQL(
                "CREATE TABLE Notificacion (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "tarea_id INTEGER NOT NULL, " +
                        "tipo TEXT, " +                 // Enum simulado con TEXT
                        "tiempoAntes INTEGER, " +
                        "horaPersonalizada TEXT, " +
                        "activa INTEGER, " +            // 0 = false, 1 = true
                        "FOREIGN KEY(tarea_id) REFERENCES Tarea(id) ON DELETE CASCADE" +
                        ");"
        );

    }

    /**
     * Con este metodó, lo que hace es cuando cambiamos de versión a la bbdd,
     * nos elimina la antigua versión de la base de datos y nos inserta
     * la nueva versión, es como si hicieramos una actualización (UPDATE).
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Elimina todo si actualizas (cuidado en producción)
        db.execSQL("DROP TABLE IF EXISTS Actividad_usuario");
        db.execSQL("DROP TABLE IF EXISTS Notificacion");
        db.execSQL("DROP TABLE IF EXISTS Tarea");
        db.execSQL("DROP TABLE IF EXISTS Usuario");
        onCreate(db);
    }

    //----------------------- METODOS (SENTENCIA SQL) -----------------------//

    //----------------------- GETTERS -----------------------//
    /**
     * Recupera todos los registros de la tabla "Usuario" y los devuelve como una cadena formateada.
     *.
     * - Muestra ID, nombre, apellido, correo electrónico, fecha de registro y estado de sesión.
     * - Formatea la fecha de registro de "yyyy-MM-dd" a "dd/MM/yyyy".
     *
     * @return Una cadena de texto con la información de todos los usuarios.
     */
    public String getUsuarios() {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder data = new StringBuilder();
        Cursor cursor = db.rawQuery("SELECT * FROM Usuario", null);

        if (cursor == null || cursor.getCount() == 0) {
            return "No hay usuarios en la base de datos.\n";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        while (cursor.moveToNext()) {
            try {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                String apellido = cursor.getString(cursor.getColumnIndexOrThrow("apellido"));
                String correo = cursor.getString(cursor.getColumnIndexOrThrow("correo"));
                String fechaRaw = cursor.getString(cursor.getColumnIndexOrThrow("fechaRegistro"));
                int logged = cursor.getInt(cursor.getColumnIndexOrThrow("logged_in"));
                int esPadre = cursor.getInt(cursor.getColumnIndexOrThrow("esPadre"));

                String fechaFormateada;
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(fechaRaw);
                    fechaFormateada = dateFormat.format(date);
                } catch (Exception e) {
                    fechaFormateada = fechaRaw;
                }

                data.append("Usuario -> ID: ").append(id)
                        .append(", Nombre: ").append(nombre)
                        .append(", Apellido: ").append(apellido)
                        .append(", Correo: ").append(correo)
                        .append(", Fecha de Registro: ").append(fechaFormateada)
                        .append(", Logged_in: ").append(logged)
                        .append(", esPadre: ").append(esPadre)
                        .append("\n");

            } catch (Exception e) {
                data.append("Error leyendo un usuario. Fila corrupta.\n");
                e.printStackTrace();
            }
        }

        cursor.close();
        return data.toString();
    }


    /**
     * Recupera todos los registros de la tabla "Tarea" y los devuelve como una cadena formateada.
     *.
     * - Muestra ID, ID del usuario relacionado, título y estado de cada tarea.
     * - De momento solo vamos a mostras estos cuatros campos, mas adelante mostraremos lo demas
     * - para ver como si se inserta correctamente (hacer en un futuro no muy lejano)
     *
     * @return Una cadena con la información de todas las tareas.
     */
    public String getTareas() {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder data = new StringBuilder();
        Cursor cursor = db.rawQuery("SELECT * FROM Tarea", null);
        while (cursor.moveToNext()) {
            data.append("Tarea -> ID: ").append(cursor.getInt(0))
                    .append(", UsuarioID: ").append(cursor.getInt(1))
                    .append(", Título: ").append(cursor.getString(2))
                    .append(", Estado: ").append(cursor.getString(6))
                    .append("\n");
        }
        cursor.close();
        return data.toString();
    }

    /**
     * Recupera todas las notificaciones desde la tabla "Notificacion" y las devuelve como texto.
     *.
     * - Incluye ID de la notificación, ID de la tarea asociada, tipo y estado (activa o no).
     * - Aquí más de lo mismo, solo mostramos algunas informaciones, ya mas adelante mostramos todos
     *
     * @return Una cadena con los datos de todas las notificaciones registradas.
     */
    public String getNotificaciones() {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder data = new StringBuilder();
        Cursor cursor = db.rawQuery("SELECT * FROM Notificacion", null);
        while (cursor.moveToNext()) {
            data.append("Notificación -> ID: ").append(cursor.getInt(0))
                    .append(", TareaID: ").append(cursor.getInt(1))
                    .append(", Tipo: ").append(cursor.getString(2))
                    .append(", Activa: ").append(cursor.getInt(5))
                    .append("\n");
        }
        cursor.close();
        return data.toString();
    }


    //----------------------- METODO SETTERS -----------------------//

    /**
     * Establece el estado de sesión activa (logged_in = 1) para el usuario especificado.
     *
     * @param usuarioId ID del usuario que ha iniciado sesión.
     * @return true si se actualizó correctamente; false si no se modificó ninguna fila.
     */
    public boolean setUsuarioLogueado(int usuarioId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Actualizamos el valor de logged_in a 1 para el usuario logueado
        ContentValues contentValues = new ContentValues();
        contentValues.put("logged_in", 1);

        int rowsUpdated = db.update("Usuario", contentValues, "id = ?", new String[]{String.valueOf(usuarioId)});
        db.close();

        return rowsUpdated > 0; // Si se actualizó al menos una fila
    }

    //----------------------- METODO COMPROBACIÓN SI ES PADRE -----------------------//

    /**
     * Metodo que comprueba si el id del usuario es el padre (administrador)
     * @param usuarioId del usuario.
     * @return Boolean TRUE/FALSE si el id que le pasamos es el padre o no.
     */
    public boolean esUsuarioPadrePorId(int usuarioId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT esPadre FROM Usuario WHERE id = ?",
                new String[]{String.valueOf(usuarioId)}
        );

        boolean esPadre = false;
        if (cursor.moveToFirst())
        {
            esPadre = cursor.getInt(0) == 1;
        }

        cursor.close();
        db.close();
        return esPadre;
    }

    //----------------------- METODO CONTRASEÑA HASH -----------------------//

    /**
     * Este metodo es muy importante ya que con esto no estaremos poniendo la contraseña
     * como un texto, ya que esto seria un tema de seguridad muy mala, ya que alguien
     * externo podra adivinar contraseñas con aplicaciones (No me acuerdo como se llaman)
     * donde pueden sacar la contraseña de nuestros clientes, pues con esto tendremos un
     * poco mas de seguridad a la hora de guardar la contraseña del cliente (usuario).
     * @param password
     * @return Devolvemos la contraseña HASHEADA
     */
    // Método para hacer el hash de la contraseña usando SHA-256
    public String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    //----------------------- METODO LOGIN -----------------------//
    /**
     * Valida si un usuario existe con ese correo y contraseña
     * @param correo <-- Que le pasamos por parametro (lo que pone el usuario)
     * @param contrasenia <-- Que le pasamos por parametro (lo que pone el usuario)
     * @return Devuelve TRUE si la contraseña y correo existe en nuestra base de datos,
     * que este dado de alta en nuestra aplicación.
     */
    public boolean validarUsuario(String correo, String contrasenia) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Hasheamos la contraseña proporcionada por el usuario
        String contraseniaHasheada = hashPassword(contrasenia);

        // Comprobamos si el usuario existe en la base de datos
        Cursor cursor = db.rawQuery(
                "SELECT id, nombre, apellido, correo, contrasenia, fechaRegistro, logged_in FROM Usuario WHERE correo = ? AND contrasenia = ?",
                new String[]{correo, contraseniaHasheada}
        );

        if (cursor != null && cursor.moveToFirst()) {
            // Verificamos si la columna "id" existe en el cursor antes de acceder a ella
            int idColumnIndex = cursor.getColumnIndex("id");

            if (idColumnIndex != -1) {
                int usuarioId = cursor.getInt(idColumnIndex);
                cursor.close();

                // Ponemos a 0 el estado de todos los usuarios (desloguear)
                ContentValues resetValues = new ContentValues();
                resetValues.put("logged_in", 0);
                db.update("Usuario", resetValues, null, null); // ← afecta a todos los registros

                // Actualizamos el estado de logged_in del usuario que ha iniciado sesión a 1
                ContentValues contentValues = new ContentValues();
                contentValues.put("logged_in", 1);
                db.update("Usuario", contentValues, "id = ?", new String[]{String.valueOf(usuarioId)});

                return setUsuarioLogueado(usuarioId);  // Llamamos a setUsuarioLogueado para marcar al usuario en la sesión
            }
        }

        cursor.close();
        db.close();
        return false; // No se encontró el usuario o la contraseña es incorrecta
    }

    //----------------------- METODO REGISTRO -----------------------//

    /**
     * Método para registrar un nuevo usuario en la base de datos
     *
     * @param nombre <-- Que le pasamos por parametro (lo que pone el usuario)
     * @param apellido <-- Que le pasamos por parametro (lo que pone el usuario)
     * @param correo <-- Que le pasamos por parametro (lo que pone el usuario)
     * @param contrasenia <-- Que le pasamos por parametro (lo que pone el usuario)
     * @param imagen <-- Foto de perfil en formato byte[] (puede venir de la galería o por defecto)
     * @return devuelve un booleano, si ocurrio algun error a la hora de insertar
     * el nuevo usuario.
     */
    public boolean registrarUsuario(String nombre, String apellido, String correo, String contrasenia, byte[] imagen) {
        // Validación de formato de correo
        if (!esCorreoValido(correo)) {
            Log.d("Registro", "Correo no válido: " + correo);
            return false;  // El correo no cumple con el formato
        }

        SQLiteDatabase db = this.getWritableDatabase();

        /**
         * Aquí vamos a comprobar que el correo no exista en nuestra base de datos,
         * ya que si exite no dejariamos crear un nuevo usuario con el mismo correo.
         */
        Cursor cursor = db.rawQuery("SELECT * FROM Usuario WHERE correo = ?", new String[]{correo});
        if (cursor.getCount() > 0) {
            // Si existe un usuario con el mismo correo, retornamos false
            cursor.close();
            db.close();
            return false;
        }
        cursor.close(); // Cerramos la sentencia de comprobación.

        // ¿Es el primer usuario?
        int esPadre = 0;
        Cursor cursorCount = db.rawQuery("SELECT COUNT(*) FROM Usuario", null);
        if (cursorCount.moveToFirst()) {
            int count = cursorCount.getInt(0);
            esPadre = (count == 0) ? 1 : 0; // Si no hay usuarios, es el padre
        }
        cursorCount.close();


        // Hasheamos la contraseña proporcionada por el usuario
        String contraseniaHasheada = hashPassword(contrasenia);

        // Aqui vamos a preparar los valores a insertar
        ContentValues contentValues = new ContentValues();
        contentValues.put("nombre", nombre);
        contentValues.put("apellido", apellido);
        contentValues.put("correo", correo);
        contentValues.put("contrasenia", contraseniaHasheada);
        contentValues.put("fechaRegistro", getCurrentDate());
        contentValues.put("logged_in", 0); // Inicialmente no está logueado
        contentValues.put("fotoPerfil", imagen); // Imagen del usuario en byte[], obligatorio según la estructura de la tabla
        contentValues.put("esPadre", esPadre); // Aquí se define si es padre

        // Insertamos el usuario y verificamos si la inserción fue exitosa
        long result = db.insert("Usuario", null, contentValues);
        db.close();

        return result != -1;  // Si result es -1, significa que la inserción falló
    }

    /**
     * Verifica si el correo electrónico proporcionado tiene un formato válido.
     *.
     * Este método comprueba que el correo cumpla con el siguiente patrón:
     *.
     * Contiene caracteres válidos antes del '@' (letras, números, puntos o guiones).
     * Incluye un dominio específico: hotmail, gmail o yahoo.
     * Termina en una extensión válida: .com o .es.
     *
     * @param correo El correo electrónico a validar.
     * @return {@code true} si el correo cumple con el formato requerido, {@code false} en caso contrario.
     */
    public boolean esCorreoValido(String correo) {
       /* String regex = "^[\\w.-]+@(hotmail|gmail|yahoo)\\.(com|es)$";
        return correo.matches(regex);*/
        return correo != null && correo.matches("^[A-Za-z0-9._%+-]+@(gmail|hotmail|yahoo)\\.(com|es)$");
    }

    /**
     * Metodo donde obtengo la fecha actual en un formato adecuado a la base de datos,
     * estos nos sirve para que cuando creemos un nuevo usuario (que se da de alta a
     * nuestra aplicación, pues que salga en fechaRegistro la fecha actal cuando se
     * registro).
     * @return devuelve la fecha actual cuando se registro en formato date adecuado a la bbdd.
     */
    // Método para obtener la fecha actual en formato adecuado para la base de datos
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    //----------------------- METODO OBTENER ID -----------------------//
    /**
     * Obtiene el ID del usuario que actualmente tiene la sesión iniciada.
     *
     * @return El ID del usuario logueado; devuelve -1 si no se encuentra un usuario con sesión activa.
     */
    public int obtenerIdUsuario() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Usuario WHERE logged_in = 1", null);

        int idUsuario = -1; // Valor por defecto si no se encuentra un usuario logueado

        if (cursor != null)
        {
            if (cursor.moveToFirst()) // Aqui comprobamos si el curson encontro la consulta (se mueve a la primera fila), si es asi devuelve un TRUE
            {
                try
                {
                    // Aqui obtenemos el valor que esta en el campo ID, ya que queremos el id del usuario que esta logeado en este momento
                    idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                }
                catch (IllegalArgumentException e)
                {
                    Log.e("Database", "Columna 'id' no encontrada en la consulta");
                    e.printStackTrace();
                }
            }
            else
            {
                Log.d("Database", "No se encontró un usuario logueado");
            }
            cursor.close();
        }
        else
        {
            Log.e("Database", "Error al ejecutar la consulta para obtener el usuario logueado");
        }

        return idUsuario;
    }

    /**
     * Cierra la sesión del usuario actualmente logueado.
     * Cambia el valor de la columna `logged_in` a 0 para todos los usuarios.
     *
     * @return true si se cerró la sesión de al menos un usuario; false en caso contrario.
     */
    public boolean cerrarSesion()
    {
        SQLiteDatabase db = this.getWritableDatabase();

        // Ponemos el valor de logged_in a 0 para todos los usuarios
        ContentValues contentValues = new ContentValues();
        contentValues.put("logged_in", 0);

        int rowsUpdated = db.update("Usuario", contentValues, "logged_in = 1", null);
        db.close();

        return rowsUpdated > 0; // Si se actualizó al menos una fila
    }


    /**
     * Actualiza los datos del usuario en la base de datos.
     *
     * @param idUsuario ID del usuario que se va a actualizar.
     * @param nombre Nuevo nombre del usuario (Lo cual lo vamos a actualizar).
     * @param apellido Nuevo apellido del usuario (Lo cual lo vamos a actualizar).
     * @param correo Nuevo correo del usuario (Lo cual lo vamos a actualizar).
     * @param password Nueva contraseña del usuario (sin hashear, se hashea dentro del método).
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarUsuario(int idUsuario, String nombre, String apellido, String correo, String password, byte[] nuevaImagen)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        // Agregar nuevos valores
        values.put("nombre", nombre);
        values.put("apellido", apellido);
        values.put("correo", correo);

        // Hashear la contraseña antes de guardarla di es contraseña valida
        if (password != null && !password.trim().isEmpty())
        {
            String passwordHasheada = hashPassword(password);
            if (passwordHasheada != null)
            {
                values.put("contrasenia", passwordHasheada);
            }
        }
        if (nuevaImagen != null) {
            values.put("fotoPerfil", nuevaImagen);
        }


        // Ejecutar la actualización
        int filasActualizadas = db.update("Usuario", values, "id = ?", new String[]{String.valueOf(idUsuario)});

        db.close();
        // Verificar si se actualizó al menos una fila
        return filasActualizadas > 0;
    }

    /**
     * Elimina un usuario de la base de datos según su ID.
     *
     * @param id El ID del usuario que se desea eliminar.
     * @return true si se eliminó al menos una fila, false si no se encontró el usuario.
     */
    public boolean eliminarUsuarioPorId(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        int filasEliminadas = db.delete("Usuario", "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return filasEliminadas > 0;
    }

    //----------------------- METODO INSERTAR TAREA -----------------------//

    /**
     * Crea una nueva tarea en la base de datos asociada a un usuario.
     *
     * @param usuarioId   ID del usuario al que se asociará la tarea.
     * @param titulo      Título de la tarea.
     * @param descripcion Descripción detallada de la tarea.
     * @param prioridad   Nivel de prioridad de la tarea (por ejemplo: Alta, Media, Baja).
     * @param estado      Estado actual de la tarea (por ejemplo: Pendiente, Completada).
     * @param fechaLimite Fecha límite para completar la tarea (en formato yyyy-MM-dd).
     * @return true si la tarea fue creada exitosamente; false si ocurrió un error.
     */

    public boolean crearTarea(int usuarioId, String titulo, String descripcion, String prioridad, String estado, String fechaLimite, String fechaHoraInicio) {
        SQLiteDatabase db = this.getWritableDatabase();

        String fechaCreacion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        // El ContentValues se utiliza para almacenar pares clave-valor
        // Donde las claves son los nombres de las columnas de una tabla de la BBDD SQLite
        ContentValues values = new ContentValues();
        values.put("usuario_id", usuarioId);
        values.put("titulo", titulo);
        values.put("descripcion", descripcion);
        values.put("prioridad", prioridad);
        values.put("estado", estado);
        values.put("fechaLimite", fechaLimite);
        values.put("fechaCreacion", fechaCreacion);
        values.put("fechaHoraInicio", fechaHoraInicio); // NUEVO campo

        // Al hacer la inserción en la BBDD de SQLite, en la variable resultado
        // se guarda un numero donde se comprueba si se ha creado la tarea o no.
        long resultado = db.insert("Tarea", null, values);
        db.close();

        return resultado != -1;
    }

    /**
     *
     * @param usuarioAsignadoId
     * @param titulo
     * @param descripcion
     * @param prioridad
     * @param estado
     * @param fechaLimite
     * @param fechaCreacion
     * @return
     */
    public boolean crearTareaUsuarioAsignado(int usuarioAsignadoId, String titulo, String descripcion, String prioridad, String estado, String fechaLimite, String fechaCreacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usuario_id", usuarioAsignadoId);
        values.put("titulo", titulo);
        values.put("descripcion", descripcion);
        values.put("prioridad", prioridad);
        values.put("estado", estado);
        values.put("fechaLimite", fechaLimite);
        values.put("fechaCreacion", fechaCreacion);

        long resultado = db.insert("Tarea", null, values);
        return resultado != -1;
    }

    /**
     * Al llamar este metodo, lo que hacemos es borrar dicha tarea seleccionada
     * @param idTarea de la tarea a borrar en la bbdd.
     * @return boolean TRUE/FALSE si se ha borrado o no.
     */
    public boolean eliminarTarea(int idTarea) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filasEliminadas = db.delete("Tarea", "id = ?", new String[]{String.valueOf(idTarea)});
        db.close(); // <-- recomendable cerrarlo
        return filasEliminadas > 0;
    }

    /**
     * Al llamar este metodo lo que hacemos e modificar la tarea que selecciono el usuario
     * Obtenemos los siguientes parametros posibles a modificar.
     * @param idTarea Que puede ser modificado.
     * @param nuevoTitulo Que puede ser modificado.
     * @param nuevaDescripcion Que puede ser modificado.
     * @param nuevaPrioridad Que puede ser modificado.
     * @param nuevoEstado Que puede ser modificado.
     * @param nuevaFechaLimite Que puede ser modificado.
     * @param nuevaFechaHoraInicio Que puede ser modificado.
     * @return Bolleano TRUE/FALSE para comprobar si se ha modificado la tarea o no.
     */
    public boolean editarTarea(int idTarea, String nuevoTitulo, String nuevaDescripcion,
                               String nuevaPrioridad, String nuevoEstado,
                               String nuevaFechaLimite, String nuevaFechaHoraInicio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("titulo", nuevoTitulo);
        values.put("descripcion", nuevaDescripcion);
        values.put("prioridad", nuevaPrioridad);
        values.put("estado", nuevoEstado);
        values.put("fechaLimite", nuevaFechaLimite);
        values.put("fechaHoraInicio", nuevaFechaHoraInicio);

        int filasAfectadas = db.update("Tarea", values, "id = ?", new String[]{String.valueOf(idTarea)});
        db.close();
        return filasAfectadas > 0;
    }


    //----------------------- METODOS PARA LA TABLA ACTIVIDAD USUARIO -----------------------//


    //----------------------- METODOS PARA EL PADRE (ADMINISTRADOR) -----------------------//

    /**
     * Este metodo lo usamos para obtener los nombres de los susario que no son padre (administrador) en este caso,
     * ya que al obtener el nombre lo ponemos en un "Spinner" para seleccionar el nombre del hijo al que le vamos a
     * crearle una tarea.
     * @return Lista de todos los usuarios que no son padres, vamos que son los hijos.
     */
    public List<String> obtenerNombresHijos() {
        List<String> nombres = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Obtener todos los usuarios que NO son padre (esPadre = 0)
        Cursor cursor = db.rawQuery("SELECT nombre FROM Usuario WHERE esPadre = 0", null);
        while (cursor.moveToNext()) {
            nombres.add(cursor.getString(0));
        }
        cursor.close();
        return nombres;
    }

    /**
     * Metodo que usamos para obtener el id por el nombre, ya que una vez que el padre seleccione el nombre
     * del hijo cual le va añadir una tarea, pues necesitamos obtener el id para crearle la tarea y hacer
     * la conexión con la tabla usaurio.
     * .
     * ¡¡IMPORTANTE!! Esto lo tendriamos que cambiar por un usuario podria tener el mismo nombre que otro usuario.
     * .
     * @param nombre del usuario seleccionado.
     * @return id del usuario.
     */
    public int obtenerIdUsuarioPorNombre(String nombre) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Usuario WHERE nombre = ?", new String[]{nombre});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }

    /**
     * Metodo que uso para obtener el nombre del usuario por su id, el id lo saco de la tabla
     * tareas en otro metodo.
     * @param idUsuario de la tabla tarea
     * @return nombre del usuario obtenido por su id.
     */
    public String obtenerNombreUsuarioPorId(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre FROM Usuario WHERE id = ?", new String[]{String.valueOf(idUsuario)});
        String nombre = "";
        if (cursor.moveToFirst()) {
            nombre = cursor.getString(0);
        }
        cursor.close();
        return nombre;
    }

    //----------------------- METODOS A USAR A FUTURO -----------------------//
    public void resetearUsuariosLogueados() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("logged_in", 0);
        db.update("Usuario", contentValues, null, null);
        db.close();
    }

    /**
     *
     * @param usuarioId
     * @return
     */
    public List<Map<String, String>> getTareasFuturas(int usuarioId) {
        List<Map<String, String>> tareas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        long ahora = System.currentTimeMillis();
        long en24Horas = ahora + (24 * 60 * 60 * 1000); // ahora + 24 horas

        Cursor cursor = db.rawQuery(
                "SELECT titulo, descripcion, fechaHoraInicio, fechaLimite FROM Tarea WHERE usuario_id = ?",
                new String[]{String.valueOf(usuarioId)}
        );

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        while (cursor.moveToNext()) {
            try {
                String titulo = cursor.getString(0);
                String descripcion = cursor.getString(1);
                String inicio = cursor.getString(2);
                String fin = cursor.getString(3);

                long inicioMs = sdf.parse(inicio).getTime();
                long finMs = sdf.parse(fin).getTime();

                if ((inicioMs > ahora && inicioMs < en24Horas) || (finMs > ahora && finMs < en24Horas)) {
                    Map<String, String> tarea = new HashMap<>();
                    tarea.put("titulo", titulo);
                    tarea.put("descripcion", descripcion);
                    tarea.put("inicio", inicio);
                    tarea.put("fin", fin);
                    tareas.add(tarea);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        cursor.close();
        return tareas;
    }
    // Borra todos los datos de todas las tablas (sin eliminar las tablas)
    // por si se queda algn perfil corrupto borrarlo, en el onCreate del main debajo del bd metes esto     dbHelper.borrarTodo();
    //        Toast.makeText(this, "Base de datos limpiada", Toast.LENGTH_SHORT).show(); y ya esta
    public void borrarTodo() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("Usuario", null, null);
            // Añade más tablas si tienes otras, como por ejemplo:
            // db.delete("Tarea", null, null);
            // db.delete("Suscripcion", null, null);

            // Reinicia el contador de ID autoincremental (opcional)
            db.execSQL("DELETE FROM sqlite_sequence");

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

}
