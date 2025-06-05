package com.example.gestorxpress.ui.Notificacion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.gestorxpress.R;

/**
 * Autores: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class NotificacionReceiver extends BroadcastReceiver
{

    /**
     * Este método que se ejecuta automáticamente cuando se recibe una señal de alarma programada.
     * Construye y lanza una notificación con el título y mensaje recibidos.
     *
     * @param context El contexto de la aplicación
     * @param intent  El intent que obtenems como extras con la información de la tarea (título y mensaje).
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // Extraemos los datos enviados desde el intent (título y mensaje de la tarea)
        String titulo = intent.getStringExtra("titulo");
        String mensaje = intent.getStringExtra("mensaje");

        Log.d("NotificacionReceiver", "Notificación recibida para: " + titulo + " con mensaje: " + mensaje); // Logs para comprobar el funcionamiento

        // Obtiene el servicio del sistema responsable de mostrar notificaciones
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String canalId = "canal_tareas";
        String canalNombre = "Recordatorios de tareas";

        // Si el dispositivo está usando Android Oreo (API 26) o superior, se debe crear un canal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel canal = new NotificationChannel(canalId, canalNombre, NotificationManager.IMPORTANCE_HIGH); // Alta prioridad para mostrar la alerta
            canal.setDescription("Notificaciones de tareas pendientes");
            canal.enableLights(true); // Habilita luz LED al recibir notificación
            canal.enableVibration(true); // Habilita vibración al recibir notificación
            notificationManager.createNotificationChannel(canal);
            Log.d("NotificacionReceiver", "Canal de notificación creado o existente");
        }

        // Creamos la notificación usando NotificationCompat (para compatibilidad con versiones antiguas)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, canalId)
                .setSmallIcon(R.drawable.gestorxpress2) // Icono a mostrado en la barra de estado
                .setContentTitle(titulo) // Título de la notificación
                .setContentText(mensaje) // Cuerpo de la notificación
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta para que suene y aparezca
                .setAutoCancel(true); // La notificación se cierra al tocarla

        // Muestra la notificación al usuario con un ID único (usando el timestamp actual)
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        Log.d("NotificacionReceiver", "Notificación mostrada"); // Logs para comprobar que se ha mostrado la notificación
    }
}
