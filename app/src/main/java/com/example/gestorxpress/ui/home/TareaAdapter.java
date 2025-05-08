package com.example.gestorxpress.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import java.util.List;
import java.util.Map;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private final Context context;
    private final List<Map<String, String>> listaTareas;
    private final DatabaseHelper dbHelper;

    public TareaAdapter(Context context, List<Map<String, String>> listaTareas, DatabaseHelper dbHelper) {
        this.context = context;
        this.listaTareas = listaTareas;
        this.dbHelper = dbHelper;
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

        // Mostrar valores
        holder.textTitulo.setText(tarea.get("titulo"));
        holder.textDescripcion.setText("Descripción: " + tarea.get("descripcion"));
        holder.textPrioridad.setText("Prioridad: " + tarea.get("prioridad"));
        holder.textEstado.setText("Estado: " + tarea.get("estado"));
        holder.textFechaInicio.setText("Inicio: " + tarea.get("fechaHoraInicio"));
        holder.textFechaLimite.setText("Límite: " + tarea.get("fechaLimite"));

        // Rellenar campos de edición
        holder.editTitulo.setText(tarea.get("titulo"));
        holder.editDescripcion.setText(tarea.get("descripcion"));
        holder.editFechaInicio.setText(tarea.get("fechaHoraInicio"));
        holder.editFechaLimite.setText(tarea.get("fechaLimite"));

        // Adaptadores para los spinners
        ArrayAdapter<CharSequence> adapterPrioridad = ArrayAdapter.createFromResource(context, R.array.opciones_prioridad, android.R.layout.simple_spinner_item);
        adapterPrioridad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerPrioridad.setAdapter(adapterPrioridad);
        holder.spinnerPrioridad.setSelection(adapterPrioridad.getPosition(tarea.get("prioridad")));

        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(context, R.array.opciones_estado, android.R.layout.simple_spinner_item);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerEstado.setAdapter(adapterEstado);
        holder.spinnerEstado.setSelection(adapterEstado.getPosition(tarea.get("estado")));

        // Expandir/cerrar detalles
        holder.itemView.setOnClickListener(v -> {
            boolean visible = holder.layoutDetalles.getVisibility() == View.VISIBLE;
            holder.layoutDetalles.setVisibility(visible ? View.GONE : View.VISIBLE);
        });

        // Eliminar tarea
        holder.btnEliminar.setOnClickListener(v -> {
            String idTarea = tarea.get("id");
            Log.d("TAREA_ID_DEBUG", "ID tarea a eliminar: " + idTarea);  // ← Añade esta línea
            if (idTarea != null) {
                try {
                    int idInt = Integer.parseInt(idTarea);
                    boolean eliminada = dbHelper.eliminarTarea(idInt);
                    Log.d("TAREA_ELIMINAR", "¿Eliminada?: " + eliminada);  // ← Añade esta línea
                    if (eliminada) {
                        listaTareas.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, listaTareas.size());
                        Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error al eliminar tarea", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "ID de tarea inválido", Toast.LENGTH_SHORT).show();
                    Log.e("TAREA_ERROR", "ID inválido: " + idTarea, e);
                }
            } else {
                Toast.makeText(context, "ID de tarea nulo", Toast.LENGTH_SHORT).show();
                Log.e("TAREA_ERROR", "ID de tarea es null");
            }
        });


        // Activar modo edición
        holder.btnEditar.setOnClickListener(v -> {
            alternarEdicion(holder, true);
        });

        // Guardar cambios
        holder.btnGuardar.setOnClickListener(v -> {
            String idTarea = tarea.get("id");
            String nuevoTitulo = holder.editTitulo.getText().toString();
            String nuevaDescripcion = holder.editDescripcion.getText().toString();
            String nuevaPrioridad = holder.spinnerPrioridad.getSelectedItem().toString();
            String nuevoEstado = holder.spinnerEstado.getSelectedItem().toString();
            String nuevaFechaInicio = holder.editFechaInicio.getText().toString();
            String nuevaFechaLimite = holder.editFechaLimite.getText().toString();

            if (dbHelper.editarTarea(Integer.parseInt(idTarea), nuevoTitulo, nuevaDescripcion,
                    nuevaPrioridad, nuevoEstado, nuevaFechaLimite, nuevaFechaInicio)) {

                tarea.put("titulo", nuevoTitulo);
                tarea.put("descripcion", nuevaDescripcion);
                tarea.put("prioridad", nuevaPrioridad);
                tarea.put("estado", nuevoEstado);
                tarea.put("fechaHoraInicio", nuevaFechaInicio);
                tarea.put("fechaLimite", nuevaFechaLimite);
                notifyItemChanged(position);

                Toast.makeText(context, "Tarea actualizada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al actualizar tarea", Toast.LENGTH_SHORT).show();
            }

            alternarEdicion(holder, false);
        });
    }

    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    private void alternarEdicion(TareaViewHolder holder, boolean enEdicion) {
        int visEdicion = enEdicion ? View.VISIBLE : View.GONE;
        int visTexto = enEdicion ? View.GONE : View.VISIBLE;

        holder.textTitulo.setVisibility(visTexto);
        holder.textDescripcion.setVisibility(visTexto);
        holder.textPrioridad.setVisibility(visTexto);
        holder.textEstado.setVisibility(visTexto);
        holder.textFechaInicio.setVisibility(visTexto);
        holder.textFechaLimite.setVisibility(visTexto);

        holder.editTitulo.setVisibility(visEdicion);
        holder.editDescripcion.setVisibility(visEdicion);
        holder.spinnerPrioridad.setVisibility(visEdicion);
        holder.spinnerEstado.setVisibility(visEdicion);
        holder.editFechaInicio.setVisibility(visEdicion);
        holder.editFechaLimite.setVisibility(visEdicion);

        holder.btnGuardar.setVisibility(visEdicion);
        holder.btnEditar.setVisibility(visTexto);
    }

    static class TareaViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textDescripcion, textPrioridad, textEstado, textFechaInicio, textFechaLimite;
        EditText editTitulo, editDescripcion, editFechaInicio, editFechaLimite;
        Spinner spinnerPrioridad, spinnerEstado;
        ImageButton btnEditar, btnEliminar;
        Button btnGuardar;
        View layoutDetalles;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_titulo);
            textDescripcion = itemView.findViewById(R.id.text_descripcion);
            textPrioridad = itemView.findViewById(R.id.text_prioridad);
            textEstado = itemView.findViewById(R.id.text_estado);
            textFechaInicio = itemView.findViewById(R.id.text_fecha_inicio);
            textFechaLimite = itemView.findViewById(R.id.text_fecha_limite);
            editTitulo = itemView.findViewById(R.id.edit_titulo);
            editDescripcion = itemView.findViewById(R.id.edit_descripcion);
            editFechaInicio = itemView.findViewById(R.id.edit_fecha_inicio);
            editFechaLimite = itemView.findViewById(R.id.edit_fecha_limite);
            spinnerPrioridad = itemView.findViewById(R.id.spinner_prioridad);
            spinnerEstado = itemView.findViewById(R.id.spinner_estado);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
            btnGuardar = itemView.findViewById(R.id.btn_guardar);
            layoutDetalles = itemView.findViewById(R.id.layout_detalles);
        }
    }
}
