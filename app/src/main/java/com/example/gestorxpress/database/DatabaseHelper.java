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

/**
 * Autor: Alfonso Chenche y Mario Herrero
 */
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
     * de descargar la bbdd si no la tenemos no se actualizara
     * los cambios que hemos hecho en la bbdd (tablas).
     *.
     * !!IMPORTANTE¡¡ Ver si cuando cambiamos la versión se nos
     * cambia automaticamente a nosotros tambien.
     */
    private static final int DATABASE_VERSION = 7;


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
     * Se llama antes de que la base de datos sea abierta, permitiendo la configuración
     * de parámetros específicos de la conexión. En este caso, se habilitan las restricciones
     * de claves foráneas para asegurar la integridad referencial.
     *
     * @param db La base de datos SQLite que está siendo configurada.
     */
    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // Activa las claves foráneas
    }

    /**
     * Metodo donde voy a crear base de datos con sus tablas y relaciones de cada tabla.
     * Es lo primero que se inicia al iniciar la aplicación o al realizar una llamada
     * desde otra clase.
     * @param db La base de datos SQLite que está siendo configurada.
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
     * @param db La base de datos SQLite que está siendo configurada.
     * @param oldVersion La versión antigua de la bbdd.
     * @param newVersion La nueva versión de la bbdd.
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
        // Abrimos la conexión a la bbdd
        SQLiteDatabase db = this.getReadableDatabase();

        // Campo de texto donde vamos a guardar todos los datos de los usuario que vamos a mostrar.
        StringBuilder data = new StringBuilder();

        // La consulta que realizamos para obtener todos los datos de cada usuario.
        Cursor consulta = db.rawQuery("SELECT * FROM Usuario", null);

        // Comrobamos que la consulta que realizamos no sea nula, osea si no obtiene los datos que
        // hemos pedido, nos saltara un error.
        if (consulta == null || consulta.getCount() == 0)
        {
            return "Error. No hay usuarios en la base de datos.\n";
        }

        // Creamos un formateador para convertir fechas a un formato "dd/MM/YYYY"
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Bucle donde vamos a estar recorriendo la consulta, ya que vamos obteniendo todos los datos
        // de cada unos de los usuarios que hay en nuestra bbdd (que esten dado de alta en nuestra aplicación)
        while (consulta.moveToNext())
        {
            try
            {
                // Obtenemos los datos de cada usuario
                int id = consulta.getInt(consulta.getColumnIndexOrThrow("id"));
                String nombre = consulta.getString(consulta.getColumnIndexOrThrow("nombre"));
                String apellido = consulta.getString(consulta.getColumnIndexOrThrow("apellido"));
                String correo = consulta.getString(consulta.getColumnIndexOrThrow("correo"));
                String fechaRaw = consulta.getString(consulta.getColumnIndexOrThrow("fechaRegistro"));
                int logged = consulta.getInt(consulta.getColumnIndexOrThrow("logged_in"));
                int esPadre = consulta.getInt(consulta.getColumnIndexOrThrow("esPadre"));

                // Aqui es donde vamos a formatear la fecha cruda a un formato más legible
                String fechaFormateada;
                try
                {
                    // Creamos otro formateador para interpretar la fecha cruda que viene como "yyyy-MM-dd HH:mm:ss"
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(fechaRaw);
                    // Aqui es donde vamos a formatear la fecha al estilo "dd/MM/yyyy"
                    fechaFormateada = dateFormat.format(date);
                }
                catch (Exception e)
                {
                    // Si ocurre algún error, usamos la original (lo guardamos sin formatear)
                    fechaFormateada = fechaRaw;
                }
                // Construimos el StringBuilder, osea creamos los lineas de texto con los datos
                // de los usuarios que hemos obtenido.
                data.append("Usuario -> ID: ").append(id)
                        .append(", Nombre: ").append(nombre)
                        .append(", Apellido: ").append(apellido)
                        .append(", Correo: ").append(correo)
                        .append(", Fecha de Registro: ").append(fechaFormateada)
                        .append(", Logged_in: ").append(logged)
                        .append(", esPadre: ").append(esPadre)
                        .append("\n");

            }
            catch (Exception e)
            {
                data.append("Error. Se ha producido un error al leer un usuario\n");
                e.printStackTrace();
            }
        }
        // Cerramos la consulta
        consulta.close();

        // Devolvemos el StringBuilder, osea todos los tados obtenido para luego mostrarlo en otro metodo.
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
        // Conexion a la bbdd
        SQLiteDatabase db = this.getReadableDatabase();

        // Texto donde vamos a guardar todos los datos de la tabla Tarea
        StringBuilder data = new StringBuilder();

        // Realizamos la consulta donde vamos a obtener todos los datos de la tabla Tareas
        Cursor consulta = db.rawQuery("SELECT * FROM Tarea", null);

        // Bucle donde leemos cada tarea para obtener sus datos.
        while (consulta.moveToNext()) {
            // Guardamos en el campo de texto StringBuilder
            data.append("Tarea -> ID: ").append(consulta.getInt(0))
                    .append(", UsuarioID: ").append(consulta.getInt(1))
                    .append(", Título: ").append(consulta.getString(2))
                    .append(", Estado: ").append(consulta.getString(6))
                    .append("\n");
        }
        // Cerramos la consulta
        consulta.close();

        // Devolvemos un StringBuilder con los datos de las tareas obtenidos.
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
        //Conexión a la bbdd
        SQLiteDatabase db = this.getWritableDatabase();

        // Actualizamos el valor de logged_in a 1 para el usuario logueado
        ContentValues contentValues = new ContentValues();
        contentValues.put("logged_in", 1);

        // Realizamos un UPDATE del logged_in en la tabla usuario, y obtenemos un tipo numerico
        // Donde lo guardamos en la variable para luego comprobar si hubo uno actuzalizado por lo menos.
        int loggedActualizado = db.update("Usuario", contentValues, "id = ?", new String[]{String.valueOf(usuarioId)});

        // Cerramos la conexión a la bbdd.
        db.close();

        return loggedActualizado > 0; // Si se actualizó al menos una fila
    }

    //----------------------- METODO COMPROBACIÓN SI ES PADRE -----------------------//

    /**
     * Metodo que comprueba si el id del usuario es el padre (administrador)
     * @param usuarioId del usuario.
     * @return Boolean TRUE/FALSE si el id que le pasamos es el padre o no.
     */
    public boolean esUsuarioPadrePorId(int usuarioId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // El resultado se guarda en el 'Cursor', que permite recorrer los resultados fila por fila.
        Cursor consulta = db.rawQuery(
                "SELECT esPadre FROM Usuario WHERE id = ?",
                new String[]{String.valueOf(usuarioId)}
        );

        boolean esPadre = false;
        if (consulta.moveToFirst()) // Movemos la consulta a la primera fila
        {
            esPadre = consulta.getInt(0) == 1; // Y comprobamos que el int (esPadre) sea igual a 1
        }

        consulta.close();
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
    public String hashPassword(String password)
    {
        // Comprobamos que la contraseña que obtenemos por el parametro no sea nula
        // Si es asi no se ejecutara este metodo, osea que no se generara la contraseña hash
        if (password == null) return null;
        try
        {
            // Creamos una instancia de MessageDigest usando el algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes()); // Convertimos la contraseña en un arreglo de bytes y le aplicamos el algoritmo "SHA-256"
            StringBuilder contraHash = new StringBuilder(); // Variable donde guardamos la contraseña hash

            // Se recorre cada byte hash y se convierte a una cadena hexadecimal
            for (byte b : hash)
            {
                contraHash.append(String.format("%02x", b)); // "%02x" <-- Se convierte el byte a dos digitos hexadecimales
            }
            return contraHash.toString(); // Devolvemos la contraseña hasheada
        }
        catch (NoSuchAlgorithmException e)
        {
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
        // Conexión a la bbdd
        SQLiteDatabase db = this.getReadableDatabase();

        // Hasheamos la contraseña proporcionada por el usuario
        String contraseniaHasheada = hashPassword(contrasenia);

        // Comprobamos si el usuario existe en la base de datos
        Cursor consulta = db.rawQuery(
                "SELECT id, nombre, apellido, correo, contrasenia, fechaRegistro, logged_in FROM Usuario WHERE correo = ? AND contrasenia = ?",
                new String[]{correo, contraseniaHasheada}
        );

        if (consulta != null && consulta.moveToFirst()) {
            // Verificamos si la columna "id" existe en el cursor antes de acceder a ella
            int idColumnIndex = consulta.getColumnIndex("id");

            if (idColumnIndex != -1) {
                int usuarioId = consulta.getInt(idColumnIndex);
                consulta.close();

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

        consulta.close();
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
        if (!esCorreoValido(correo))
        {
            Log.d("Registro", "Correo no válido: " + correo);
            return false;  // El correo no cumple con el formato
        }

        // Conexión a la bbdd
        SQLiteDatabase db = this.getWritableDatabase();

        /**
         * Aquí vamos a comprobar que el correo no exista en nuestra base de datos,
         * ya que si exite no dejariamos crear un nuevo usuario con el mismo correo.
         */
        Cursor consulta = db.rawQuery("SELECT * FROM Usuario WHERE correo = ?", new String[]{correo});
        if (consulta.getCount() > 0) {
            // Si existe un usuario con el mismo correo, retornamos false
            consulta.close();
            db.close();
            return false;
        }
        consulta.close(); // Cerramos la sentencia de comprobación.

        // Aqui comprobamos si es el primer usuario que se crea, ya que ese seria el padre (administrador)
        int esPadre = 0;
        Cursor comprobaSiEsPadre = db.rawQuery("SELECT COUNT(*) FROM Usuario", null);
        if (comprobaSiEsPadre.moveToFirst())
        {
            int count = comprobaSiEsPadre.getInt(0);
            esPadre = (count == 0) ? 1 : 0; // Si no hay usuarios, es el padre
        }
        comprobaSiEsPadre.close();


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
    public boolean esCorreoValido(String correo)
    {
        return correo != null && correo.matches("^[A-Za-z0-9._%+-]+@(gmail|hotmail|yahoo)\\.(com|es)$");
    }

    /**
     * Metodo donde obtengo la fecha actual en un formato adecuado a la base de datos,
     * estos nos sirve para que cuando creemos un nuevo usuario (que se da de alta a
     * nuestra aplicación, pues que salga en fechaRegistro la fecha actal cuando se
     * registro).
     * @return devuelve la fecha actual cuando se registro en formato date adecuado a la bbdd.
     */
    private String getCurrentDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    //----------------------- METODO OBTENER ID -----------------------//
    /**
     * En este metodo obtiene el ID del usuario que actualmente tiene la sesión iniciada.
     *
     * @return El ID del usuario logueado; devuelve -1 si no se encuentra un usuario con sesión activa.
     */
    public int obtenerIdUsuario()
    {
        SQLiteDatabase db = this.getReadableDatabase(); // Realizanos la conexión a la bbdd
        Cursor consulta = db.rawQuery("SELECT id FROM Usuario WHERE logged_in = 1", null);

        int idUsuario = -1; // Valor por defecto si no se encuentra un usuario logueado

        // Comprobamos que la consulta realizada no sea nul. Osea que obtenga por lo menos un dato
        if (consulta != null)
        {
            if (consulta.moveToFirst()) // Aqui comprobamos si el curson encontro la consulta (se mueve a la primera fila), si es asi devuelve un TRUE
            {
                try
                {
                    // Aqui obtenemos el valor que esta en el campo ID, ya que queremos el id del usuario que esta logeado en este momento
                    idUsuario = consulta.getInt(consulta.getColumnIndexOrThrow("id"));
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
            consulta.close();
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
        // Realizamos la conexión a la bbdd
        SQLiteDatabase db = this.getWritableDatabase();

        // Generamos el ContentValues (estructura clave-valor) que se usa principal mente para actualizar o insertar datos
        // Que en este caso lo estamos usando para actualizar el logged_in
        ContentValues contentValues = new ContentValues();
        contentValues.put("logged_in", 0); // Ponemos el valor de logged_in a 0 para todos los usuarios

        // Al hacer una actualización, podemos obtener un valor numerico que lo podemos usar para comprobar si se ha actualizado o no
        int rowsUpdated = db.update("Usuario", contentValues, "logged_in = 1", null);
        db.close(); // Cerramos la conexión a la bbdd

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
        SQLiteDatabase db = this.getWritableDatabase(); // Realizamos la conexión a la bbdd

        // Generamos el ContentValues (estructura clave-valor) que se usa principal mente para actualizar o insertar datos
        // Que en este caso lo estamos usando para actualizar los datos del usuario.
        ContentValues actualiUsuario = new ContentValues();

        // Agregar nuevos valores
        actualiUsuario.put("nombre", nombre);
        actualiUsuario.put("apellido", apellido);
        actualiUsuario.put("correo", correo);

        // Hashear la contraseña antes de guardarla di es contraseña valida
        if (password != null && !password.trim().isEmpty())
        {
            // Llamamos el metodo donde encriptamos (hash) la contraseña y lo guardamos en una nueva variable
            String passwordHasheada = hashPassword(password);
            if (passwordHasheada != null) // Comprobamos que la contraseña no sea vacia
            {
                actualiUsuario.put("contrasenia", passwordHasheada);
            }
        }
        if (nuevaImagen != null)
        {
            actualiUsuario.put("fotoPerfil", nuevaImagen);
        }


        // Ejecutamos la actualización y obtenemos un numerico que lo usamos para comprobar si se ha actualizado o no
        int filasActualizadas = db.update("Usuario", actualiUsuario, "id = ?", new String[]{String.valueOf(idUsuario)});

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
     * Este metodo crea una nueva tarea en la base de datos asociada a un usuario.
     * Lo usa solo el hijo.
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
        ContentValues aniadirTarea = new ContentValues();
        aniadirTarea.put("usuario_id", usuarioId);
        aniadirTarea.put("titulo", titulo);
        aniadirTarea.put("descripcion", descripcion);
        aniadirTarea.put("prioridad", prioridad);
        aniadirTarea.put("estado", estado);
        aniadirTarea.put("fechaLimite", fechaLimite);
        aniadirTarea.put("fechaCreacion", fechaCreacion);
        aniadirTarea.put("fechaHoraInicio", fechaHoraInicio); // NUEVO campo

        // Al hacer la inserción en la BBDD de SQLite, en la variable resultado
        // se guarda un numero donde se comprueba si se ha creado la tarea o no.
        long resultado = db.insert("Tarea", null, aniadirTarea);
        db.close();

        return resultado != -1;
    }

    /**
     * Este metodo crea una nueva tarea en la bbdd asociada a un usuario.
     * (Esto lo usa el padre)
     *
     * @param usuarioAsignadoId ID del usuario (Hijo) al que se asociará la tarea.
     * @param titulo Título de la tarea.
     * @param descripcion  Descripción detallada de la tarea.
     * @param prioridad Nivel de prioridad de la tarea (por ejemplo: Alta, Media, Baja).
     * @param estado Estado actual de la tarea (por ejemplo: Pendiente, Completada).
     * @param fechaLimite Fecha límite para completar la tarea (en formato yyyy-MM-dd).
     * @param fechaCreacion Fecha cuando se creo la tarea (en formato yyyy-MM-dd).
     * @return true si la tarea fue creada exitosamente; false si ocurrió un error.
     */
    public boolean crearTareaUsuarioAsignado(int usuarioAsignadoId, String titulo, String descripcion, String prioridad, String estado, String fechaLimite, String fechaCreacion) {
        // Conexión a la bbdd
        SQLiteDatabase db = this.getWritableDatabase();

        // El ContentValues se utiliza para almacenar pares clave-valor
        ContentValues datos = new ContentValues();
        datos.put("usuario_id", usuarioAsignadoId);
        datos.put("titulo", titulo);
        datos.put("descripcion", descripcion);
        datos.put("prioridad", prioridad);
        datos.put("estado", estado);
        datos.put("fechaLimite", fechaLimite);
        datos.put("fechaCreacion", fechaCreacion);

        long resultado = db.insert("Tarea", null, datos);
        return resultado != -1;
    }

    /**
     * Al llamar este metodo, lo que hacemos es borrar dicha tarea seleccionada
     * @param idTarea de la tarea a borrar en la bbdd.
     * @return boolean TRUE/FALSE si se ha borrado o no.
     */
    public boolean eliminarTarea(int idTarea)
    {
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
                               String nuevaFechaLimite, String nuevaFechaHoraInicio)
    {
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

    //----------------------- METODOS PARA EL PADRE (ADMINISTRADOR) -----------------------//

    /**
     * Este metodo lo usamos para obtener los nombres de los susario que no son padre (administrador) en este caso,
     * ya que al obtener el nombre lo ponemos en un "Spinner" para seleccionar el nombre del hijo al que le vamos a
     * crearle una tarea.
     * @return Lista de todos los usuarios que no son padres, vamos que son los hijos.
     */
    public List<String> obtenerNombresHijos()
    {
        List<String> nombres = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Obtener todos los usuarios que NO son padre (esPadre = 0)
        Cursor consulta = db.rawQuery("SELECT nombre FROM Usuario WHERE esPadre = 0", null);
        while (consulta.moveToNext())
        {
            nombres.add(consulta.getString(0));
        }
        consulta.close();
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
    public int obtenerIdUsuarioPorNombre(String nombre)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor consulta = db.rawQuery("SELECT id FROM Usuario WHERE nombre = ?", new String[]{nombre});
        if (consulta.moveToFirst())
        {
            int id = consulta.getInt(0);
            consulta.close();
            return id;
        }
        consulta.close();
        return -1;
    }

    /**
     * Metodo que uso para obtener el nombre del usuario por su id, el id lo saco de la tabla
     * tareas en otro metodo.
     * @param idUsuario de la tabla tarea
     * @return nombre del usuario obtenido por su id.
     */
    public String obtenerNombreUsuarioPorId(int idUsuario)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor consulta = db.rawQuery("SELECT nombre FROM Usuario WHERE id = ?", new String[]{String.valueOf(idUsuario)});
        String nombre = "";
        if (consulta.moveToFirst())
        {
            nombre = consulta.getString(0);
        }
        consulta.close();
        return nombre;
    }

    //----------------------- METODOS A USAR A FUTURO -----------------------//
    /**
     *
     * @param usuarioId
     * @return
     */
    public List<Map<String, String>> getTareasFuturas(int usuarioId)
    {
        List<Map<String, String>> tareas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        long ahora = System.currentTimeMillis();
        long en24Horas = ahora + (24 * 60 * 60 * 1000); // ahora + 24 horas

        Cursor cursor = db.rawQuery(
                "SELECT titulo, descripcion, fechaHoraInicio, fechaLimite FROM Tarea WHERE usuario_id = ?",
                new String[]{String.valueOf(usuarioId)}
        );

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        while (cursor.moveToNext())
        {
            try
            {
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
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        cursor.close();
        return tareas;
    }

}
