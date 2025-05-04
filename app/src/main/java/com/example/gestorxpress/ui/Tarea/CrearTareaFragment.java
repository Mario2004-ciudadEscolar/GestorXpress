package com.example.gestorxpress.ui.Tarea;

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

public class CrearTareaFragment extends Fragment {

    private EditText editTitulo, editDescripcion, editFechaLimite;
    private Spinner spinnerPrioridad, spinnerEstado;
    private Button btnGuardar;
    private DatabaseHelper dbHelper;
    private int idUsuario;

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
        editFechaLimite = view.findViewById(R.id.editFechaLimite);
        spinnerPrioridad = view.findViewById(R.id.spinnerPrioridad);
        spinnerEstado = view.findViewById(R.id.spinnerEstado);
        btnGuardar = view.findViewById(R.id.btnGuardar);

        dbHelper = new DatabaseHelper(requireContext());

        // Obtener el ID del usuario desde los argumentos
        Bundle args = getArguments();
        if (args != null) {
            idUsuario = args.getInt("idUsuario", -1);
        }

        Log.d("DEBUG_ID", "idUsuario recibido: " + idUsuario);

        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configurar los Spinners con adaptadores desde strings.xml
        ArrayAdapter<CharSequence> adapterPrioridad = ArrayAdapter.createFromResource(
                requireContext(), R.array.opciones_prioridad, android.R.layout.simple_spinner_item);
        adapterPrioridad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrioridad.setAdapter(adapterPrioridad);

        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(
                requireContext(), R.array.opciones_estado, android.R.layout.simple_spinner_item);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstado);

        btnGuardar.setOnClickListener(v -> guardarTarea());
    }

    private void guardarTarea() {
        String titulo = editTitulo.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();
        String prioridad = spinnerPrioridad.getSelectedItem().toString();
        String estado = spinnerEstado.getSelectedItem().toString();
        String fechaLimite = editFechaLimite.getText().toString().trim();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(fechaLimite)) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DEBUG_INSERT", "Insertando tarea: " + titulo + ", usuario_id=" + idUsuario);

        boolean insertado = dbHelper.insertarTarea(idUsuario, titulo, descripcion, fechaLimite, prioridad, estado);

        if (insertado) {
            Toast.makeText(getContext(), "Tarea guardada correctamente", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed(); // Volver atrás
        } else {
            Toast.makeText(getContext(), "Error al guardar la tarea", Toast.LENGTH_SHORT).show();
        }
    }
}
