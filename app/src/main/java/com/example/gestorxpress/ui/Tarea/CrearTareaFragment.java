package com.example.gestorxpress.ui.Tarea;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.Notificacion.NotificacionHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CrearTareaFragment extends Fragment
{
    // Atributos
    private EditText editTitulo, editDescripcion, editFechaFin, editFechaInicio;
    private Spinner spinnerPrioridad, spinnerEstado, spinnerHijos;
    private Button btnGuardar;

    // Instancia a la clase DatabaseHelper
    private DatabaseHelper dbHelper;

    //Atributo para guardar el ID del usuario
    private int idUsuario = -1;

    private Calendar fechaHoraInicioCalendar, fechaFinCalendar;

    // Atributo para comprobar si es el padre o no
    private boolean esPadre = false;

    // Constructor sin parametros
    public CrearTareaFragment() { }

    /**
     * e llama para inflar el diseño del fragmento y configurar los datos visuales.
     * .
     * Se genero automaticamente al crear el javaDoc
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return La llamada al xml (Vista del fragmento)
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_crear__tarea, container, false);
    }

    /**
     * Se llama este metodo imediatamente despues de que onCreateView haya retornado la vista del fragmento
     *
     * Este metodo es útil
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // Inicialización de vistas
        editTitulo = view.findViewById(R.id.editTitulo);
        editDescripcion = view.findViewById(R.id.editDescripcion);
        editFechaFin = view.findViewById(R.id.editFechaFin);
        editFechaInicio = view.findViewById(R.id.editFechaInicio);
        spinnerPrioridad = view.findViewById(R.id.spinnerPrioridad);
        spinnerEstado = view.findViewById(R.id.spinnerEstado);
        spinnerHijos = view.findViewById(R.id.spinnerHijos);
        btnGuardar = view.findViewById(R.id.btnGuardar);

        // Instnacia a la bbdd e inicio de conexión a la base de datos
        dbHelper = new DatabaseHelper(requireContext());

        fechaHoraInicioCalendar = Calendar.getInstance();
        fechaFinCalendar = Calendar.getInstance();

        // Obtener el ID del usuario desde los argumentos
        // Con esto obtenemos el ID del usuario que esta logueado en este momento
        idUsuario = dbHelper.obtenerIdUsuario();  // Método en nuestro DatabaseHelper

        // Si no existe el usuario, Mostraremos un mensaje informativo, indicando
        // el error y desactivando el boton para que no pueda crear la tarea.
        if (idUsuario == -1)
        {
            Toast.makeText(getContext(), "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
            btnGuardar.setEnabled(false); // Desactiva el botón de guardar
            return;
        }

        // Si idUsuario es inválido (menos que 0), desactivar el botón de guardar
        if (idUsuario <= 0)
        {
            Toast.makeText(getContext(), "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
            Log.e("CREAR_TAREA", "Usuario inválido, idUsuario = " + idUsuario);
            btnGuardar.setEnabled(false); // Desactiva el botón
            return;
        }

        Log.d("DEBUG_ID", "idUsuario recibido: " + idUsuario);

        // Determinar si el usuario actual es un padre
        esPadre = dbHelper.esUsuarioPadrePorId(idUsuario);
        Log.d("CREAR_TAREA", "¿El usuario es padre (administrador)? " + esPadre);

        // Configurar los Spinners con adaptadores desde strings.xml
        ArrayAdapter<CharSequence> adapterPrioridad = ArrayAdapter.createFromResource(
                requireContext(), R.array.opciones_prioridad, android.R.layout.simple_spinner_item);
        adapterPrioridad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrioridad.setAdapter(adapterPrioridad);

        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(
                requireContext(), R.array.opciones_estado, android.R.layout.simple_spinner_item);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstado);

        // Configuramos el 'Spinner' (Nombre de los usuarios) de hijos si es padre
        if (esPadre)
        {
            // Si el usuario es padre (administrador): Muestra el spinner con los hijos
            spinnerHijos.setVisibility(View.VISIBLE);

            // Mediante el metodo que hemos lavorado en la bbdd, obtenemos todos los nombres de los usuarios
            // y lo guardamos en una lista, para luego mostrarlo en el Spinner
            List<String> nombresHijos = dbHelper.obtenerNombresHijos();

            // Si la lista esta vacia, mostraremos un mensaje informativo indicando el tipo de error.
            // El error puede ser porque no hay hijos (usuarios) dado de alta en la aplicación,
            // osea que no hay otros usuarios creados aparte del padre (administrador).
            if (nombresHijos.isEmpty())
            {
                nombresHijos.add("Sin hijos disponibles");
                Toast.makeText(getContext(), "No tienes hijos registrados. No puedes asignar tareas.", Toast.LENGTH_LONG).show();
                btnGuardar.setEnabled(false);
            }

            // Lo usamos para mostrar en el Spinner los nombres de los hijos que lo hemos guardado
            // previamente en la lista.
            ArrayAdapter<String> hijosAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    nombresHijos
            );
            hijosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHijos.setAdapter(hijosAdapter);
        }
        else
        {
            // Si el que va crear la tarea es el hijo,
            // no mostraremos el Spinner que es previamente para el padre
            spinnerHijos.setVisibility(View.GONE);
        }

        // Fecha y hora de inicio
        editFechaInicio.setOnClickListener(v -> mostrarSelectorFechaHora(editFechaInicio, fechaHoraInicioCalendar));

        // Fecha y hora de fin
        editFechaFin.setOnClickListener(v -> mostrarSelectorFechaHora(editFechaFin, fechaFinCalendar));

        // Configurar el listener del botón de guardar
        btnGuardar.setOnClickListener(v -> guardarTarea());
    }

    /**
     * Lo que hace este método es obtener el contenido que ha escrito el usuario para crear la tarea
     * y en un método que llamamos desde la BBDD, creamos y guardamos la tarea en la BBDD.
     */
    private void guardarTarea()
    {
        // Obtenemos el contenido de los campos y lo guardamos en las variables
        String titulo = editTitulo.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();
        String prioridad = spinnerPrioridad.getSelectedItem().toString();
        String estado = spinnerEstado.getSelectedItem().toString();

        // Comprovamos que los campos no esten vacios, si algunos de los campos
        // estan vacios, indicamos un mensaje informativo indicando el error.
        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion)
                || TextUtils.isEmpty(editFechaInicio.getText()) || TextUtils.isEmpty(editFechaFin.getText()))
        {
            Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Valida que la fecha de inicio sea anterior a la fecha de fin usando
        if (fechaHoraInicioCalendar.after(fechaFinCalendar))
        {
            Toast.makeText(getContext(), "La fecha de inicio debe ser anterior a la fecha de fin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Formatear las fechas y horas completas
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String fechaHoraInicio = sdf.format(fechaHoraInicioCalendar.getTime());
        String fechaHoraFin = sdf.format(fechaFinCalendar.getTime());

        // Este atributo lo usamos para comprobar si ha creado correctamente la tarea.
        boolean exito;

        // Si es el Padre (administrador)
        if (esPadre)
        {
            // Obtenemos el nombre del hijo que ha seleccionado en el Spinner
            String nombreHijoSeleccionado = spinnerHijos.getSelectedItem().toString();

            // Si no ha seleccionado ningún hijo, mostramos un mensaje informativo.
            if (nombreHijoSeleccionado.equals("Sin hijos disponibles"))
            {
                Toast.makeText(getContext(), "No puedes asignar tareas si no tienes hijos registrados", Toast.LENGTH_SHORT).show();
                return;
            }

            // Si ha obtenido algún nombre de cualquier hijo seleccionado, llamamos al metodo
            // que hemos elavorado en la bbdd, donde obtenemos el id del hijo gracias al nombre
            // que hemos obtenido.
            int idHijo = dbHelper.obtenerIdUsuarioPorNombre(nombreHijoSeleccionado);

            // Comprobamos si existe ese ID, si no mostramos un mensaje informativo.
            if (idHijo == -1)
            {
                Toast.makeText(getContext(), "Error al obtener el ID del hijo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Si hemos obtenido un ID, le asignamos la tarea al hijo y lo insertamos en la bbdd
            exito = dbHelper.crearTareaUsuarioAsignado(idHijo, titulo, descripcion, prioridad, estado, fechaHoraFin, fechaHoraInicio);
        }
        else
        {
            // Si es el hijo el que esta creando la tarea, creamos la tarea para el
            exito = dbHelper.crearTarea(idUsuario, titulo, descripcion, prioridad, estado, fechaHoraFin, fechaHoraInicio);
        }

        // Si se ha creado correctamente, mostraremos un mensaje informativo
        // indicando que se ha creado correctamente
        if (exito)
        {
            Toast.makeText(getContext(), "Tarea guardada correctamente", Toast.LENGTH_SHORT).show();

            // Aqui programamos notificaciones y eventos
            Log.d("Tareas", "Programando alarma de inicio para: " + fechaHoraInicioCalendar.getTime().toString());
            programarAlarmaEnReloj(fechaHoraInicioCalendar, "Inicio: " + titulo);

            Log.d("Tareas", "Programando alarma de fin para: " + fechaFinCalendar.getTime().toString());
            programarAlarmaEnReloj(fechaFinCalendar, "Fin: " + titulo);

            insertarEventoEnCalendario(titulo, descripcion, fechaHoraInicioCalendar.getTimeInMillis(), fechaFinCalendar.getTimeInMillis());

            // Aqui programamos una notificación 1 hora antes de que finalice
            int idTarea = dbHelper.obtenerUltimoIdTareaInsertada();
            NotificacionHelper.programarNotificacionDesdeBD(requireContext(), idTarea, titulo, fechaFinCalendar, dbHelper);

            limpiarCampos();
        }
        else
        {
            // Si no indicamos el error.
            Toast.makeText(getContext(), "Error al guardar la tarea", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Una vez que se ha creado y guardado la tarea en la BBDD,
     * se limpiarán todos los campos para crear otra tarea por
     * si el usuario quiere crear otra tarea más.
     */
    private void limpiarCampos()
    {
        editTitulo.setText("");
        editDescripcion.setText("");
        editFechaFin.setText("");
        editFechaInicio.setText("");
        spinnerPrioridad.setSelection(0);
        spinnerEstado.setSelection(0);
    }

    /**
     * Método para mostrar un selector combinado de fecha y hora.
     */
    private void mostrarSelectorFechaHora(EditText campo, Calendar calendario)
    {
        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, y, m, d) ->
        {
            calendario.set(Calendar.YEAR, y);
            calendario.set(Calendar.MONTH, m);
            calendario.set(Calendar.DAY_OF_MONTH, d);

            int hora = calendario.get(Calendar.HOUR_OF_DAY);
            int minuto = calendario.get(Calendar.MINUTE);

            new TimePickerDialog(requireContext(), (timeView, h, min) ->
            {
                calendario.set(Calendar.HOUR_OF_DAY, h);
                calendario.set(Calendar.MINUTE, min);
                SimpleDateFormat formatoCompleto = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault());
                campo.setText(formatoCompleto.format(calendario.getTime()));
            }, hora, minuto, true).show();

        }, anio, mes, dia).show();
    }

    private void programarAlarmaEnReloj(Calendar fecha, String mensaje)
    {
        int hora = fecha.get(Calendar.HOUR_OF_DAY);
        int minuto = fecha.get(Calendar.MINUTE);

        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(AlarmClock.EXTRA_HOUR, hora);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minuto);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, mensaje);
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true); // para que no abra el reloj

        // Solo continúa si hay app de reloj que acepte la intent
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "No se encontró una app de Reloj compatible", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Inserta un evento en el calendario con la información de la tarea.
     */
    private void insertarEventoEnCalendario(String titulo, String descripcion, long inicioMillis, long finMillis)
    {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, titulo)
                .putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicioMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, finMillis)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        startActivity(intent);
    }

}
