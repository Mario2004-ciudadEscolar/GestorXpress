package com.example.gestorxpress.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import com.example.gestorxpress.ui.Tarea.Tarea;

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
    private static final int DATABASE_VERSION = 3;


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
                        "logged_in INTEGER DEFAULT 0" +  // Agregado para el logged_in
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
                        "prioridad TEXT, " +            // Enum simulado con TEXT
                        "estado TEXT, " +               // Enum simulado con TEXT
                        "fechaCreacion TEXT NOT NULL, " +
                        "FOREIGN KEY(usuario_id) REFERENCES Usuario(id) ON DELETE CASCADE" +
                        ");"
        );

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

        // Tabla ActividadUsuario
        db.execSQL(
                "CREATE TABLE Actividad_usuario (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "usuario_id INTEGER NOT NULL, " +
                        "fecha TEXT NOT NULL, " +
                        "tareasCompletadas INTEGER DEFAULT 0, " +
                        "promedioCompletadas INTEGER DEFAULT 0, " +
                        "FOREIGN KEY(usuario_id) REFERENCES Usuario(id) ON DELETE CASCADE" +
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

        if (cursor.getCount() == 0) {
            Log.d("Database", "No hay usuarios en la base de datos");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        while (cursor.moveToNext()) {
            String rawDate = cursor.getString(5);  // Fecha de registro
            String formattedDate;  // Puede requerir un formato adecuado

            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(rawDate);
                formattedDate = dateFormat.format(date);  // Formatear la fecha
            } catch (Exception e) {
                e.printStackTrace();
                formattedDate = rawDate;
            }

            // Aquí accedemos a la contraseña (que está en el índice 4)
            //String contrasenia = cursor.getString(4);  // Valor real de la contraseña

            // Construimos la cadena de texto con la contraseña en texto claro
            data.append("Usuario -> ID: ").append(cursor.getInt(0))
                    .append(", Nombre: ").append(cursor.getString(1))
                    .append(", Apellido: ").append(cursor.getString(2))
                    .append(", Correo: ").append(cursor.getString(3))  // Correo
                    //.append(", Contraseña: ").append(contrasenia)  // Aquí mostramos la contraseña en texto claro
                    .append(", Fecha de Registro: ").append(formattedDate)  // Fecha de Registro
                    .append(", Loggin_id: ").append((cursor.getInt(6)))
                    .append("\n");
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

    /**
     * Recupera el historial de actividad de usuarios desde la tabla "Actividad_usuario".
     *
     * - Incluye ID, ID del usuario, fecha y cantidad de tareas completadas.
     *
     * @return Una cadena con la información de todas las actividades de los usuarios.
     */
    public String getActividadUsuarios() {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder data = new StringBuilder();
        Cursor cursor = db.rawQuery("SELECT * FROM Actividad_usuario", null);
        while (cursor.moveToNext()) {
            data.append("Actividad -> ID: ").append(cursor.getInt(0))
                    .append(", UsuarioID: ").append(cursor.getInt(1))
                    .append(", Fecha: ").append(cursor.getString(2))
                    .append(", TareasCompletadas: ").append(cursor.getInt(3))
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

                // Actualizamos el estado de logged_in del usuario que ha iniciado sesión a 1
                ContentValues contentValues = new ContentValues();
                contentValues.put("logged_in", 1);
                db.update("Usuario", contentValues, "id = ?", new String[]{String.valueOf(usuarioId)});

                return setUsuarioLogueado(usuarioId);  // Llamamos a setUsuarioLogueado para marcar al usuario en la sesión
            }
        }

        cursor.close();
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
     * @return devuelve un booleano, si ocurrio algun error a la hora de insertar
     * el nuevo usuario.
     */
    public boolean registrarUsuario(String nombre, String apellido, String correo, String contrasenia) {
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
        String regex = "^[\\w.-]+@(hotmail|gmail|yahoo)\\.(com|es)$";
        return correo.matches(regex);
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
    public boolean crearTarea(int usuarioId, String titulo, String descripcion, String prioridad, String estado, String fechaLimite) {
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

        // Al hacer la inserción en la BBDD de SQLite, en la variable resultado
        // se guarda un numero donde se comprueba si se ha creado la tarea o no.
        long resultado = db.insert("Tarea", null, values);
        db.close();

        return resultado != -1;
    }




    //----------------------- METODO OBTENER TAREA -----------------------//
    /**
     * Recupera todas las tareas asociadas a un usuario específico desde la base de datos.
     *
     * @param idUsuario ID del usuario cuyas tareas se desean obtener.
     * @return Una lista de objetos {@link Tarea} que pertenecen al usuario especificado.
     */
    public List<Tarea> obtenerTareasPorUsuario(int idUsuario) {
        List<Tarea> tareas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Tarea WHERE usuario_id = ?", new String[]{String.valueOf(idUsuario)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
                String descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"));
                String prioridad = cursor.getString(cursor.getColumnIndexOrThrow("prioridad"));
                String estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"));
                String fechaLimite = cursor.getString(cursor.getColumnIndexOrThrow("fechaLimite"));

                tareas.add(new Tarea(id, idUsuario, titulo, descripcion, prioridad, estado, fechaLimite));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tareas;
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
                    // Usamos getColumnIndexOrThrow para asegurarnos de que la columna 'id' existe
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
    public boolean actualizarUsuario(int idUsuario, String nombre, String apellido, String correo, String password)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        // Agregar nuevos valores
        values.put("nombre", nombre);
        values.put("apellido", apellido);
        values.put("correo", correo);

        // Hashear la contraseña antes de guardarla
        String passwordHasheada = hashPassword(password);
        if (passwordHasheada != null)
        {
            values.put("password", passwordHasheada);
        }
        else
        {
            Log.e("Database", "Error al hashear la contraseña");
            return false;
        }

        // Ejecutar la actualización
        int filasActualizadas = db.update("Usuario", values, "id = ?", new String[]{String.valueOf(idUsuario)});

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

}
