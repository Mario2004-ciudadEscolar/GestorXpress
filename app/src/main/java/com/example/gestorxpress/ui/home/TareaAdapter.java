package com.example.gestorxpress.ui.home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private final Context context;
    private final List<Map<String, String>> listaTareas;
    private final DatabaseHelper dbHelper;
    private boolean esPadre = false;

    public TareaAdapter(Context context, List<Map<String, String>> listaTareas, DatabaseHelper dbHelper, boolean esPadre) {
        this.context = context;
        this.listaTareas = listaTareas;
        this.dbHelper = dbHelper;
        this.esPadre = esPadre;
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

        // Hacemos una comprobación, donde si es padre (administrador) vera el nombre del hijo
        // Que tiene esa tarea.
        if (esPadre)
        {
            int idUsuarioTarea = Integer.parseInt(tarea.get("usuario_id"));
            String nombreUsuario = dbHelper.obtenerNombreUsuarioPorId(idUsuarioTarea);
            holder.textNombreUsuario.setText(nombreUsuario);
            holder.textNombreUsuario.setVisibility(View.VISIBLE);
        }
        // Si no, no mostramos el nombre del hijo, ya que el hijo solo vera sus propias tareas.
        else
        {
            holder.textNombreUsuario.setVisibility(View.GONE);
        }

        holder.editTitulo.setText(tarea.get("titulo"));
        holder.editDescripcion.setText(tarea.get("descripcion"));
        holder.editFechaInicio.setText(tarea.get("fechaHoraInicio"));
        holder.editFechaLimite.setText(tarea.get("fechaLimite"));

        ArrayAdapter<CharSequence> adapterPrioridad = ArrayAdapter.createFromResource(context, R.array.opciones_prioridad, android.R.layout.simple_spinner_item);
        adapterPrioridad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerPrioridad.setAdapter(adapterPrioridad);
        holder.spinnerPrioridad.setSelection(adapterPrioridad.getPosition(tarea.get("prioridad")));

        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(context, R.array.opciones_estado, android.R.layout.simple_spinner_item);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerEstado.setAdapter(adapterEstado);
        holder.spinnerEstado.setSelection(adapterEstado.getPosition(tarea.get("estado")));

        holder.itemView.setOnClickListener(v -> {
            boolean visible = holder.layoutDetalles.getVisibility() == View.VISIBLE;
            holder.layoutDetalles.setVisibility(visible ? View.GONE : View.VISIBLE);
        });

        // Si el usuario le da el boton eliminar, elimina la tarea
        holder.btnEliminar.setOnClickListener(v -> {
            String idTarea = tarea.get("id"); // Obtenemos el id de la tarea que vamos a eliminar

            //Comprobamos que esa tarea no sea nula
            if (idTarea != null) {
                // Lo usamos para controlar posibles errores que nos da al eliminar o obtener el id de la tarea a borrar
                try
                {
                    int idInt = Integer.parseInt(idTarea); // Guardamos el id de la tarea a borrar.

                    if (dbHelper.eliminarTarea(idInt)) // Llamamos el metodo donde borramos la tarea, y obtenemos un boolean según si se borro o no.
                    {
                        listaTareas.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, listaTareas.size());
                        Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                    }
                    else // Por si surgui un error.
                    {
                        Toast.makeText(context, "Error al eliminar tarea", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (NumberFormatException e)
                {
                    Toast.makeText(context, "ID de tarea inválido", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(context, "ID de tarea nulo", Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * Al darle el boton de editar, se activira el modo edición.
         * Tambien se activara un boton que es el guardar.
         * Dentro de ahi podremos editar lo que quiera el usuario.
         * .
         * Cuando le demos al boton de guardar, llamaremos un metodo donde hara un UPDATE sobre la
         * tarea modificada, tambien haremos un put para que el usuario vea en la aplicación
         * lo que ha modificado.
         */
        holder.btnEditar.setOnClickListener(v -> alternarModoEdicion(holder, true));

        holder.btnGuardar.setOnClickListener(v -> {
            String idTarea = tarea.get("id");
            String nuevoTitulo = holder.editTitulo.getText().toString();
            String nuevaDescripcion = holder.editDescripcion.getText().toString();
            String nuevaPrioridad = holder.spinnerPrioridad.getSelectedItem().toString();
            String nuevoEstado = holder.spinnerEstado.getSelectedItem().toString();
            String nuevaFechaInicio = holder.editFechaInicio.getText().toString();
            String nuevaFechaLimite = holder.editFechaLimite.getText().toString();

            if (dbHelper.editarTarea(Integer.parseInt(idTarea), nuevoTitulo, nuevaDescripcion,
                    nuevaPrioridad, nuevoEstado, nuevaFechaLimite, nuevaFechaInicio))
            {
                tarea.put("titulo", nuevoTitulo);
                tarea.put("descripcion", nuevaDescripcion);
                tarea.put("prioridad", nuevaPrioridad);
                tarea.put("estado", nuevoEstado);
                tarea.put("fechaHoraInicio", nuevaFechaInicio);
                tarea.put("fechaLimite", nuevaFechaLimite);
                notifyItemChanged(position);

                Toast.makeText(context, "Tarea actualizada", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(context, "Error al actualizar tarea", Toast.LENGTH_SHORT).show();
            }

            alternarModoEdicion(holder, false);
        });

        /**
         * Aqui hacemos un control de calendario, donde el usuario puede seleccionar la fecha y hora.
         * Que esta muy bien visualmente.
         */
        // Dialogs para seleccionar fecha y hora
        Calendar calendario = Calendar.getInstance();
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault());

        View.OnClickListener selectorFechaHora = v -> {
            EditText editText = (EditText) v;
            new DatePickerDialog(context, (view, año, mes, dia) -> {
                calendario.set(Calendar.YEAR, año);
                calendario.set(Calendar.MONTH, mes);
                calendario.set(Calendar.DAY_OF_MONTH, dia);

                new TimePickerDialog(context, (view2, hora, minuto) -> {
                    calendario.set(Calendar.HOUR_OF_DAY, hora);
                    calendario.set(Calendar.MINUTE, minuto);
                    editText.setText(formato.format(calendario.getTime()));
                }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), true).show();

            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show();
        };

        holder.editFechaInicio.setOnClickListener(selectorFechaHora);
        holder.editFechaLimite.setOnClickListener(selectorFechaHora);
    }

    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    /**
     * Metodo donde alternamos el modo edición, ya que se activa los texto de la tarea a modificar.
     * @param holder
     * @param enEdicion TRUE/FALSE segun si va a editar o no la tarea.
     */
    private void alternarModoEdicion(TareaViewHolder holder, boolean enEdicion)
    {
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

    /**
     * Clase donde obtenemos los textModel para visualizar las tareas que recuperamos.
     */
    static class TareaViewHolder extends RecyclerView.ViewHolder
    {
        TextView textTitulo, textDescripcion, textPrioridad, textEstado, textFechaInicio, textFechaLimite;
        TextView textNombreUsuario;
        EditText editTitulo, editDescripcion, editFechaInicio, editFechaLimite;
        Spinner spinnerPrioridad, spinnerEstado;
        ImageButton btnEditar, btnEliminar;
        Button btnGuardar;
        View layoutDetalles;

        public TareaViewHolder(@NonNull View itemView)
        {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_titulo);
            textNombreUsuario = itemView.findViewById(R.id.text_nombre_usuario);
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
