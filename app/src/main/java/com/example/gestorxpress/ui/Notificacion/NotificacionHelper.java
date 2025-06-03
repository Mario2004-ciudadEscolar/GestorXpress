package com.example.gestorxpress.ui.Notificacion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;

/**
 * Autores: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class NotificacionHelper
{
    /**
     * Este metodo sirve para programa una notificación para
     * que se muestre en un momento específico.
     *
     * @param context Contexto de la aplicación
     * @param titulo Título de la tarea
     * @param triggerAtMillis Tiempo en milisegundos en el que debe lanzarse la notificación
     */
    public static void programarNotificacion(Context context, String titulo, long triggerAtMillis) {
        // Se crea un intent que será capturado por NotificacionReceiver
        Intent intent = new Intent(context, NotificacionReceiver.class);
        intent.putExtra("titulo", titulo); // Pasamos los datos extras (El primero como titulo)
        intent.putExtra("mensaje", "La tarea \"" + titulo + "\" finalizará en 1 hora"); // Luego pasamos un mensaje personalizado
        // Los dos Itent que pasamos como extra, es lo que se vera en la notificación personalizada.

        // Se crea un PendingIntent con el intent anterior
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) System.currentTimeMillis(), // ID único para evitar colisiones
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        // Se obtenemos el servicio de alarmas del sistema
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Si obtenemos el servicio de la alarma, pasamos a generar la alarma
        if (alarmManager != null)
        {
            Log.d("NotificacionHelper", "AlarmManager no es null, programando alarma"); // Logs para comprobar funcionamientos
            // Programamos la alarma para que se dispare exactamente a la hora deseada
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Log.d("NotificacionHelper", "Alarma programada para: " + new java.util.Date(triggerAtMillis).toString()); // Logs para comprobar funcionamientos
        }
        else
        {
            // Si no mostramos un mensaje informativo indicando el error
            Log.e("NotificacionHelper", "AlarmManager es null, no se pudo programar la alarma");
        }
    }

    /**
     * Este método programa una notificación basada en los datos de la tabla Tarea (Que se acaba de crear).
     * Busca cuánto tiempo antes de la fecha de fin debe notificarse, y programa la notificación.
     *
     * @param context Contexto de la aplicación.
     * @param idTarea ID de la tarea para la que se quiere programar la notificación.
     * @param titulo  Título de la tarea.
     * @param fechaFinCalendar Fecha y hora de finalización de la tarea .
     * @param dbHelper Objeto SQLiteOpenHelper para acceder a la base de datos.
     */
    public static void programarNotificacionDesdeBD(Context context, int idTarea, String titulo, Calendar fechaFinCalendar, SQLiteOpenHelper dbHelper) {
        // Validamos que el 'id' de tareas exita, si no exite
        // o no hemos obtenido el id, mostramos un mensaje informativo
        if (idTarea == -1)
        {
            Log.e("NotificacionHelper", "ID de tarea inválido para notificación");
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor consulta = null;

        try
        {
            // Consulta SQL para obtener el tiempoAntes de la notificación para esa tarea
            consulta = db.rawQuery(
                    "SELECT tiempoAntes FROM Notificacion WHERE tarea_id = ? ORDER BY id DESC LIMIT 1",
                    new String[]{String.valueOf(idTarea)}
            );

            if (consulta != null && consulta.moveToFirst())
            {
                // Obtenemos el valor del tiempoAntes en minutos desde la base de datos
                int tiempoAntesMin = consulta.getInt(0);
                long tiempoAntesMillis = tiempoAntesMin * 60 * 1000; // Lo convertimos a milisegundos

                // Calcula el momento en que debe lanzarse la notificación
                long tiempoNotificacionMillis = fechaFinCalendar.getTimeInMillis() - tiempoAntesMillis;

                Log.d("NotificacionHelper", "Notificación con " + tiempoAntesMin + " minutos de antelación");

                // Llamamos al método que programa la notificación en sí
                programarNotificacion(context, titulo, tiempoNotificacionMillis);
            }
            else
            {
                Log.w("NotificacionHelper", "No se encontró tiempoAntes en BD para tarea " + idTarea);
            }
        }
        catch (Exception e)
        {
            Log.e("NotificacionHelper", "Error al consultar notificación en BD: " + e.getMessage());
        }
        finally
        {
            // Cerramos la conexión a la bbdd
            if (consulta != null) consulta.close();
            db.close();
        }
    }

}
