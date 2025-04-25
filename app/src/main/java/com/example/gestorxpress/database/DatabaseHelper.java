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
     *
     * Devemos de tener en cuenta que si cambiamos la versión,
     * la primera clase que se ejecuta tiene que tener el metodo
     * de descargar la bbdd si no la tenemos o abrirla si la tenemos.
     *
     * !!IMPORTANTE¡¡ Ver si cuando cambiamos la versión se nos
     * cambia automaticamente a nosotros tambien.
     */
    private static final int DATABASE_VERSION = 1;


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
                        "fechaRegistro TEXT NOT NULL" +
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

        // Comprobamos si el usuario exite en la bbdd
        Cursor cursor = db.rawQuery(
                "SELECT * FROM Usuario WHERE correo = ? AND contrasenia = ?",
                new String[]{correo, contraseniaHasheada}
        );
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
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

        // Insertamos el usuario y verificamos si la inserción fue exitosa
        long result = db.insert("Usuario", null, contentValues);
        db.close();

        return result != -1;  // Si result es -1, significa que la inserción falló
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




}
