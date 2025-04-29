package com.example.gestorxpress.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.gestorxpress.R;
import com.example.gestorxpress.ui.Tarea.Tarea;
import com.example.gestorxpress.TareaView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TareaView tareaView;
    private TextView textHome;
    private ListView listView;
    private int idUsuario; // ID del usuario logueado

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        if (getArguments() != null) {
            idUsuario = getArguments().getInt("idUsuario", -1);
        }

        textHome = root.findViewById(R.id.text_home);
        listView = new ListView(requireContext());
        ((ViewGroup) root).addView(listView);

        tareaView = new ViewModelProvider(requireActivity()).get(TareaView.class);

        // 🔽 Aquí cargas las tareas desde SQLite
        tareaView.cargarTareasDesdeBD(requireContext(), idUsuario);

        tareaView.getTareas().observe(getViewLifecycleOwner(), tareas -> {
            List<Tarea> tareasUsuario = new ArrayList<>();
            for (Tarea tarea : tareas) {
                if (tarea.getIdUsuario() == idUsuario) {
                    tareasUsuario.add(tarea);
                }
            }

            if (tareasUsuario.isEmpty()) {
                textHome.setText("No hay tareas creadas.");
                listView.setVisibility(View.GONE);
            } else {
                textHome.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                ArrayAdapter<Tarea> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, tareasUsuario);
                listView.setAdapter(adapter);
            }
        });

        return root;
    }

}
