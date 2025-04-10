package com.example.gestorxpress.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.gestorxpress.R;
import com.example.gestorxpress.Tarea;
import com.example.gestorxpress.TareaView;

import java.util.Date;

public class GalleryFragment extends Fragment {

    private TareaView tareaView;

    private EditText editTitulo, editDescripcion, editPrioridad, editEstado;
    private Button btnGuardar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        tareaView = new ViewModelProvider(requireActivity()).get(TareaView.class);

        editTitulo = root.findViewById(R.id.editTitulo);
        editDescripcion = root.findViewById(R.id.editDescripcion);
        editPrioridad = root.findViewById(R.id.editPrioridad);
        editEstado = root.findViewById(R.id.editEstado);
        btnGuardar = root.findViewById(R.id.btnGuardar);

        btnGuardar.setOnClickListener(v -> {
            String titulo = editTitulo.getText().toString().trim();
            String descripcion = editDescripcion.getText().toString().trim();
            String prioridad = editPrioridad.getText().toString().trim();
            String estado = editEstado.getText().toString().trim();

            if (titulo.isEmpty() || descripcion.isEmpty() || prioridad.isEmpty() || estado.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Tarea nueva = new Tarea(titulo, descripcion, new Date(), prioridad, estado);
            tareaView.agregarTarea(nueva);

            Toast.makeText(getContext(), "Tarea guardada", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigate(R.id.nav_home);
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (editTitulo != null) editTitulo.setText("");
        if (editDescripcion != null) editDescripcion.setText("");
        if (editPrioridad != null) editPrioridad.setText("");
        if (editEstado != null) editEstado.setText("");
    }
}
