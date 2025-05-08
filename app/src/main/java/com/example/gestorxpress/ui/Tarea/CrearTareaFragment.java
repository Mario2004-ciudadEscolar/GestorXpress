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

    private EditText editTitulo, editDescripcion, editFechaFin, editFechaInicio;
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
        spinnerPrioridad = view.findViewById(R.id.spinnerPrioridad);
        spinnerEstado = view.findViewById(R.id.spinnerEstado);
        btnGuardar = view.findViewById(R.id.btnGuardar);

        dbHelper = new DatabaseHelper(requireContext());
        fechaHoraInicioCalendar = Calendar.getInstance();
        fechaFinCalendar = Calendar.getInstance();

        // Obtener el ID del usuario desde los argumentos
        // Con esto obtenemos el ID del usuario que esta logueado en este momento
        idUsuario = dbHelper.obtenerIdUsuario();  // Método en nuestro DatabaseHelper
        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
            btnGuardar.setEnabled(false); // Desactiva el botón de guardar
            return;
        }

        // Si idUsuario es inválido (menos que 0), desactivar el botón de guardar
        if (idUsuario <= 0) {
            Toast.makeText(getContext(), "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
            Log.e("CREAR_TAREA", "Usuario inválido, idUsuario = " + idUsuario);
            btnGuardar.setEnabled(false); // Desactiva el botón
            return;
        }

        Log.d("DEBUG_ID", "idUsuario recibido: " + idUsuario);

        // Configurar los Spinners con adaptadores desde strings.xml
        ArrayAdapter<CharSequence> adapterPrioridad = ArrayAdapter.createFromResource(
                requireContext(), R.array.opciones_prioridad, android.R.layout.simple_spinner_item);
        adapterPrioridad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrioridad.setAdapter(adapterPrioridad);

        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(
                requireContext(), R.array.opciones_estado, android.R.layout.simple_spinner_item);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstado);

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
    private void guardarTarea() {
        String titulo = editTitulo.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();
        String prioridad = spinnerPrioridad.getSelectedItem().toString();
        String estado = spinnerEstado.getSelectedItem().toString();

        // Validación de campos vacíos
        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion)
                || TextUtils.isEmpty(editFechaInicio.getText()) || TextUtils.isEmpty(editFechaFin.getText())) {
            Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Formatear las fechas y horas completas
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String fechaHoraInicio = sdf.format(fechaHoraInicioCalendar.getTime());
        String fechaHoraFin = sdf.format(fechaFinCalendar.getTime());

        boolean exito = dbHelper.crearTarea(idUsuario, titulo, descripcion, prioridad, estado, fechaHoraFin, fechaHoraInicio);
        if (exito) {
            Toast.makeText(getContext(), "Tarea guardada correctamente", Toast.LENGTH_SHORT).show();
            limpiarCampos();
        } else {
            Toast.makeText(getContext(), "Error al guardar la tarea", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Una vez que se ha creado y guardado la tarea en la BBDD,
     * se limpiarán todos los campos para crear otra tarea por
     * si el usuario quiere crear otra tarea más.
     */
    private void limpiarCampos() {
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
    private void mostrarSelectorFechaHora(EditText campo, Calendar calendario) {
        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            calendario.set(Calendar.YEAR, y);
            calendario.set(Calendar.MONTH, m);
            calendario.set(Calendar.DAY_OF_MONTH, d);

            int hora = calendario.get(Calendar.HOUR_OF_DAY);
            int minuto = calendario.get(Calendar.MINUTE);

            new TimePickerDialog(requireContext(), (timeView, h, min) -> {
                calendario.set(Calendar.HOUR_OF_DAY, h);
                calendario.set(Calendar.MINUTE, min);
                SimpleDateFormat formatoCompleto = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault());
                campo.setText(formatoCompleto.format(calendario.getTime()));
            }, hora, minuto, true).show();

        }, anio, mes, dia).show();
    }
}
