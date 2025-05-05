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
        private int idUsuario = -1;

        public CrearTareaFragment()
        {
            // Constructor vacío requerido
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_crear__tarea, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
        {
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
            idUsuario = dbHelper.obtenerIdUsuario();  // Método en tu DatabaseHelper
            if (idUsuario == -1)
            {
                Toast.makeText(getContext(), "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
                btnGuardar.setEnabled(false); // Desactiva el botón de guardar
                return;
            }

            Log.d("DEBUG_ID", "idUsuario recibido: " + idUsuario);

            // Si idUsuario es inválido (menos que 0), desactivar el botón de guardar
            if (idUsuario <= 0)
            {
                Toast.makeText(getContext(), "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
                Log.e("CREAR_TAREA", "Usuario inválido, idUsuario = " + idUsuario);
                btnGuardar.setEnabled(false); // Desactiva el botón
                return;
            }

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

            // Configurar el listener del botón de guardar
            btnGuardar.setOnClickListener(v -> guardarTarea());
        }

        private void guardarTarea()
        {
            String titulo = editTitulo.getText().toString().trim();
            String descripcion = editDescripcion.getText().toString().trim();
            String prioridad = spinnerPrioridad.getSelectedItem().toString();
            String estado = spinnerEstado.getSelectedItem().toString();
            String fechaLimite = editFechaLimite.getText().toString().trim();

            // Validación de campos vacíos
            if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(fechaLimite))
            {
                Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verifica si el usuario está logueado antes de proceder a crear la tarea
            if (idUsuario == -1)
            {
                Toast.makeText(getContext(), "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Intentar crear la tarea
            boolean exito = dbHelper.crearTarea(idUsuario, titulo, descripcion, prioridad, estado, fechaLimite);

            if (exito)
            {
                Toast.makeText(getContext(), "Tarea guardada correctamente", Toast.LENGTH_SHORT).show();
                limpiarCampos();
            }
            else
            {
                Toast.makeText(getContext(), "Error al guardar la tarea", Toast.LENGTH_SHORT).show();
            }
        }

        private void limpiarCampos()
        {
            editTitulo.setText("");
            editDescripcion.setText("");
            editFechaLimite.setText("");
            spinnerPrioridad.setSelection(0);
            spinnerEstado.setSelection(0);
        }

    }
