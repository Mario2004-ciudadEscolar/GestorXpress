package com.example.gestorxpress.ui.Tarea;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CrearTareaFragment extends Fragment {

    private EditText editTitulo, editDescripcion, editFechaFin, editFechaInicio, editHoraInicio;
    private Spinner spinnerPrioridad, spinnerEstado;
    private Button btnGuardar;
    private DatabaseHelper dbHelper;
    private int idUsuario = -1;

    private Calendar fechaHoraInicioCalendar, fechaFinCalendar;

    public CrearTareaFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crear__tarea, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicialización de vistas
        editTitulo = view.findViewById(R.id.editTitulo);
        editDescripcion = view.findViewById(R.id.editDescripcion);
        editFechaFin = view.findViewById(R.id.editFechaFin);
        editFechaInicio = view.findViewById(R.id.editFechaInicio);
        editHoraInicio = view.findViewById(R.id.editHoraInicio);
        spinnerPrioridad = view.findViewById(R.id.spinnerPrioridad);
        spinnerEstado = view.findViewById(R.id.spinnerEstado);
        btnGuardar = view.findViewById(R.id.btnGuardar);

        dbHelper = new DatabaseHelper(requireContext());
        fechaHoraInicioCalendar = Calendar.getInstance();
        fechaFinCalendar = Calendar.getInstance();

        // Obtener el ID del usuario desde los argumentos
        // Con esto obtenemos el ID defechaFinCalendar = Calendar.getInstance();l usuario que esta lageado en este momento
        idUsuario = dbHelper.obtenerIdUsuario();  // Método en nuestro DatabaseHelper
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

        // Configurar los Spinners con adaptadores desde strings.xml
        // Configurar los Spinners
        ArrayAdapter<CharSequence> adapterPrioridad = ArrayAdapter.createFromResource(
                requireContext(), R.array.opciones_prioridad, android.R.layout.simple_spinner_item);
        adapterPrioridad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrioridad.setAdapter(adapterPrioridad);

        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(
                requireContext(), R.array.opciones_estado, android.R.layout.simple_spinner_item);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstado);


        // Calendario para fecha de inicio
        editFechaInicio.setOnClickListener(v -> {
            int year = fechaHoraInicioCalendar.get(Calendar.YEAR);
            int month = fechaHoraInicioCalendar.get(Calendar.MONTH);
            int day = fechaHoraInicioCalendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(requireContext(), (view1, y, m, d) -> {
                fechaHoraInicioCalendar.set(Calendar.YEAR, y);
                fechaHoraInicioCalendar.set(Calendar.MONTH, m);
                fechaHoraInicioCalendar.set(Calendar.DAY_OF_MONTH, d);
                updateFechaInicio();
            }, year, month, day).show();
        });

        // Reloj para hora de inicio
        editHoraInicio.setOnClickListener(v -> {
            int hour = fechaHoraInicioCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = fechaHoraInicioCalendar.get(Calendar.MINUTE);
            new TimePickerDialog(requireContext(), (view12, h, m) -> {
                fechaHoraInicioCalendar.set(Calendar.HOUR_OF_DAY, h);
                fechaHoraInicioCalendar.set(Calendar.MINUTE, m);
                updateHoraInicio();
            }, hour, minute, true).show();
        });

        editFechaFin.setOnClickListener(v -> {
            int year = fechaFinCalendar.get(Calendar.YEAR);
            int month = fechaFinCalendar.get(Calendar.MONTH);
            int day = fechaFinCalendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(requireContext(), (view1, y, m, d) -> {
                fechaFinCalendar.set(Calendar.YEAR, y);
                fechaFinCalendar.set(Calendar.MONTH, m);
                fechaFinCalendar.set(Calendar.DAY_OF_MONTH, d);
                updateFechaFin();
            }, year, month, day).show();
        });

        // Configurar el listener del botón de guardar
        btnGuardar.setOnClickListener(v -> guardarTarea());
    }

    /**
     * Lo que hace este metodo es obtener el contenido que a escrito el usuario para crear la tarea
     * y en un metodo que llamamos desde la BBDD, creamos y guardamos la tarea en la BBDD.
     */
    private void guardarTarea() {
        String titulo = editTitulo.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();
        String prioridad = spinnerPrioridad.getSelectedItem().toString();
        String estado = spinnerEstado.getSelectedItem().toString();
        String fechaFin = editFechaFin.getText().toString().trim();

        // Validación de campos vacíos
        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion)
                || TextUtils.isEmpty(fechaFin) || TextUtils.isEmpty(editFechaInicio.getText())
                || TextUtils.isEmpty(editHoraInicio.getText())) {
            Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Formatear la fecha y hora de inicio
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String fechaHoraInicio = sdf.format(fechaHoraInicioCalendar.getTime());

        boolean exito = dbHelper.crearTarea(idUsuario, titulo, descripcion, prioridad, estado, fechaFin, fechaHoraInicio);
        if (exito) {
            Toast.makeText(getContext(), "Tarea guardada correctamente", Toast.LENGTH_SHORT).show();
            limpiarCampos();
        } else {
            Toast.makeText(getContext(), "Error al guardar la tarea", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Una vez que se a creado y guardado la tarea en la BBDD,
     * se limpiara todos los campos para crear otra tarea por
     * si el usuario quiere crear otra tarea más.
     */
    private void limpiarCampos() {
        editTitulo.setText("");
        editDescripcion.setText("");
        editFechaFin.setText("");
        editFechaInicio.setText("");
        editHoraInicio.setText("");
        spinnerPrioridad.setSelection(0);
        spinnerEstado.setSelection(0);
    }

    private void updateFechaInicio() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy", Locale.getDefault());
        editFechaInicio.setText(dateFormat.format(fechaHoraInicioCalendar.getTime()));
    }

    private void updateFechaFin() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy", Locale.getDefault());
        editFechaFin.setText(dateFormat.format(fechaFinCalendar.getTime()));
    }


    private void updateHoraInicio() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        editHoraInicio.setText(timeFormat.format(fechaHoraInicioCalendar.getTime()));
    }
}
