// HomeFragment.java
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
import com.example.gestorxpress.Tarea;
import com.example.gestorxpress.TareaView;

import java.util.List;
import android.widget.ArrayAdapter;

public class HomeFragment extends Fragment {

    private TareaView tareaView;
    private TextView textHome;
    private ListView listView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        textHome = root.findViewById(R.id.text_home);
        listView = new ListView(requireContext());
        ((ViewGroup) root).addView(listView); // Añade ListView al layout

        tareaView = new ViewModelProvider(requireActivity()).get(TareaView.class);

        tareaView.getTareas().observe(getViewLifecycleOwner(), tareas -> {
            if (tareas.isEmpty()) {
                textHome.setText("No hay tareas creadas.");
                listView.setVisibility(View.GONE);
            } else {
                textHome.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                ArrayAdapter<Tarea> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, tareas);
                listView.setAdapter(adapter);
            }
        });

        return root;
    }
}
