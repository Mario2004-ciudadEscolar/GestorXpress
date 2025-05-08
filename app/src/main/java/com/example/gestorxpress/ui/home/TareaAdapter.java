package com.example.gestorxpress.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gestorxpress.R;
import java.util.List;
import java.util.Map;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private final Context context;
    private final List<Map<String, String>> listaTareas;

    public TareaAdapter(Context context, List<Map<String, String>> listaTareas) {
        this.context = context;
        this.listaTareas = listaTareas;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tarea_expandible, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Map<String, String> tarea = listaTareas.get(position);

        holder.textTitulo.setText(tarea.get("titulo"));
        holder.textDescripcion.setText("Descripción: " + tarea.get("descripcion"));
        holder.textPrioridad.setText("Prioridad: " + tarea.get("prioridad"));
        holder.textEstado.setText("Estado: " + tarea.get("estado"));
        holder.textFechaInicio.setText("Inicio: " + tarea.get("fechaHoraInicio"));
        holder.textFechaLimite.setText("Límite: " + tarea.get("fechaLimite"));

        holder.itemView.setOnClickListener(v -> {
            if (holder.layoutDetalles.getVisibility() == View.GONE) {
                holder.layoutDetalles.setVisibility(View.VISIBLE);
            } else {
                holder.layoutDetalles.setVisibility(View.GONE);
            }
        });

        // Botones (editar/eliminar): a implementar después
        holder.btnEditar.setOnClickListener(v -> {
            // Aquí pondremos lógica de edición en línea
        });

        holder.btnEliminar.setOnClickListener(v -> {
            // Aquí pondremos lógica para eliminar
        });
    }

    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    static class TareaViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textDescripcion, textPrioridad, textEstado, textFechaInicio, textFechaLimite;
        View layoutDetalles;
        ImageButton btnEditar, btnEliminar;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_titulo);
            textDescripcion = itemView.findViewById(R.id.text_descripcion);
            textPrioridad = itemView.findViewById(R.id.text_prioridad);
            textEstado = itemView.findViewById(R.id.text_estado);
            textFechaInicio = itemView.findViewById(R.id.text_fecha_inicio);
            textFechaLimite = itemView.findViewById(R.id.text_fecha_limite);
            layoutDetalles = itemView.findViewById(R.id.layout_detalles);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
        }
    }
}
