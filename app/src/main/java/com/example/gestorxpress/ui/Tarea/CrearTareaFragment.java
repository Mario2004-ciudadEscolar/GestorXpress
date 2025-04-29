package com.example.gestorxpress.ui.Tarea;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;

public class CrearTareaFragment extends Fragment {

    private EditText editTitulo, editDescripcion, editPrioridad, editEstado, editFechaLimite;
    private Button btnGuardar;
    private DatabaseHelper dbHelper;
    private int idUsuario; // ID del usuario logueado

    public CrearTareaFragment() {
        // Constructor vacío
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate el layout para este fragment
        return inflater.inflate(R.layout.fragment_crear__tarea, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        editTitulo = view.findViewById(R.id.editTitulo);
        editDescripcion = view.findViewById(R.id.editDescripcion);
        editPrioridad = view.findViewById(R.id.editPrioridad);
        editEstado = view.findViewById(R.id.editEstado);
        editFechaLimite = view.findViewById(R.id.editFechaLimite);
        btnGuardar = view.findViewById(R.id.btnGuardar);

        dbHelper = new DatabaseHelper(requireContext());

        // Obtener el idUsuario desde los argumentos
        Bundle args = getArguments();
        if (args != null) {
            idUsuario = args.getInt("idUsuario", -1); // Obtener el idUsuario
        }

        Log.d("DEBUG_ID", "idUsuario recibido: " + idUsuario);

        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setOnClickListener(v -> guardarTarea());
    }

    private void guardarTarea() {
        String titulo = editTitulo.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();
        String prioridad = editPrioridad.getText().toString().trim();
        String estado = editEstado.getText().toString().trim();
        String fechaLimite = editFechaLimite.getText().toString().trim();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion) ||
                TextUtils.isEmpty(prioridad) || TextUtils.isEmpty(estado) || TextUtils.isEmpty(fechaLimite)) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("DEBUG_INSERT", "Insertando tarea: " + titulo + ", usuario_id=" + idUsuario);

        // Ahora solo se pasa la fecha límite, la fecha de creación se maneja en la base de datos
        boolean insertado = dbHelper.insertarTarea(idUsuario, titulo, descripcion, fechaLimite, prioridad, estado);

        if (insertado) {
            Toast.makeText(getContext(), "Tarea guardada correctamente", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed(); // Opcional: volver atrás
        } else {
            Toast.makeText(getContext(), "Error al guardar la tarea", Toast.LENGTH_SHORT).show();
        }
    }
}
