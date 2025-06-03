package com.example.gestorxpress.ui.home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.Notificacion.NotificacionHelper;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Autor: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private final Context context;

    // Colección donde vamos a guardar las tareas y mostrarlos en el Home
    private final List<Map<String, String>> listaTareas;

    // Instancia a la clase DatabaseHelper
    private final DatabaseHelper dbHelper;

    // Variable que usaremos para comprobar si es padre o no
    private boolean esPadre = false;

    // Constructor con parametros
    public TareaAdapter(Context context, List<Map<String, String>> listaTareas, DatabaseHelper dbHelper, boolean esPadre)
    {
        this.context = context;
        this.listaTareas = listaTareas;
        this.dbHelper = dbHelper;
        this.esPadre = esPadre;
    }

    /**
     * Crea una nueva vista para cada item del RecyclerView
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return TareaViewHolder Devuelve el nuevo View con esa vista
     */
    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tarea_expandible, parent, false);
        return new TareaViewHolder(view);
    }

    /**
     * Asocia los datos con las vistas del ViewHolder para cada posición
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position)
    {
        // Obtenemos la tarea actual de la lista de tareas, según la posición del ViewHolder
        Map<String, String> tarea = listaTareas.get(position);

        // Obtenemos el contenido y establecemos el texto visible en el modo lectura (no editable)
        holder.textTitulo.setText(tarea.get("titulo"));
        
        // Establecer el color del título según la prioridad
        String prioridad = tarea.get("prioridad");
        if (prioridad != null) {
            int colorResId;
            switch (prioridad.toLowerCase()) {
                case "baja":
                    colorResId = R.color.prioridad_baja;
                    break;
                case "media":
                    colorResId = R.color.prioridad_media;
                    break;
                case "alta":
                    colorResId = R.color.prioridad_alta;
                    break;
                default:
                    colorResId = android.R.color.white;
                    break;
            }
            holder.textTitulo.setTextColor(context.getResources().getColor(colorResId));
        }

        holder.textDescripcion.setText("Descripción: " + tarea.get("descripcion"));
        holder.textPrioridad.setText("Prioridad: " + tarea.get("prioridad"));
        holder.textEstado.setText("Estado: " + tarea.get("estado"));
        holder.textFechaInicio.setText("Inicio: " + tarea.get("fechaHoraInicio"));
        holder.textFechaLimite.setText("Límite: " + tarea.get("fechaLimite"));

        // Hacemos una comprobación, donde si es padre (administrador) vera el nombre del hijo
        // Que tiene esa tarea.
        if (esPadre)
        {
            // Obtenemos el ID del usuario asignado a la tarea
            int idUsuarioTarea = Integer.parseInt(tarea.get("usuario_id"));

            // Llamamos al metodo que esta en la bbdd para obtener el nombre del usuario a partir del id
            String nombreUsuario = dbHelper.obtenerNombreUsuarioPorId(idUsuarioTarea);

            // Mostramos el nombre del usuario en el campo correspondiente
            holder.textNombreUsuario.setText(nombreUsuario);

            // Hacemos visible este campo en la vista (ya que solo lo vera el administrador para saber de quien es quien la tarea)
            holder.textNombreUsuario.setVisibility(View.VISIBLE);
        }
        // Si no, no mostramos el nombre del hijo, ya que el hijo solo vera sus propias tareas.
        else
        {
            // Ocultamos el campo
            holder.textNombreUsuario.setVisibility(View.GONE);
        }

        // Establecemos los valores en los campos editables (modo edición)
        holder.editTitulo.setText(tarea.get("titulo"));
        holder.editDescripcion.setText(tarea.get("descripcion"));
        holder.editFechaInicio.setText(tarea.get("fechaHoraInicio"));
        holder.editFechaLimite.setText(tarea.get("fechaLimite"));

        // Creamos un adaptador para el 'Spinner de prioridad usando los valores del array definido en el xml (String.xml)
        ArrayAdapter<CharSequence> adapterPrioridad = ArrayAdapter.createFromResource(context, R.array.opciones_prioridad, android.R.layout.simple_spinner_item);

        // Definimos como se mostrara la lista desplegable del 'Spinner' <-- Desplegable de selección
        adapterPrioridad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asignamos el adaptador al Spinner de prioridad
        holder.spinnerPrioridad.setAdapter(adapterPrioridad);

        // Seleccionamos la opción actual en el spinner de prioridad, basada en el valor de la tarea
        holder.spinnerPrioridad.setSelection(adapterPrioridad.getPosition(tarea.get("prioridad")));

        // Hacemos lo mismo para el spinner de estado (pendiente, en progreso, completado, etc.)
        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(context, R.array.opciones_estado, android.R.layout.simple_spinner_item);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerEstado.setAdapter(adapterEstado);
        holder.spinnerEstado.setSelection(adapterEstado.getPosition(tarea.get("estado")));

        holder.itemView.setOnClickListener(v ->
        {
            // Comprobamos si el layout de detalles actualmente está visible
            boolean visible = holder.layoutDetalles.getVisibility() == View.VISIBLE;

            // Si está visible lo ocultamos, si está ocultando lo mostramos
            holder.layoutDetalles.setVisibility(visible ? View.GONE : View.VISIBLE);
        });

        // Si el usuario le da el boton eliminar, elimina la tarea
        holder.btnEliminar.setOnClickListener(v ->
        {
            String idTarea = tarea.get("id");
            String tituloTarea = tarea.get("titulo");

            if (idTarea != null)
            {
                try
                {
                    int idInt = Integer.parseInt(idTarea);

                    if (dbHelper.eliminarTarea(idInt))
                    {
                        Toast.makeText(context, "Por favor, elimina el evento '" + tituloTarea + "' de tu calendario", Toast.LENGTH_LONG).show();
                        
                        // Abrir el calendario
                        abrirCalendario();
                        
                        listaTareas.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, listaTareas.size());
                        Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                    }
                    else
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

        // Cuando el usuario le dal al boton guardar, hara lo siguiente...
        holder.btnGuardar.setOnClickListener(v ->
        {
            String idTarea = tarea.get("id");
            String nuevoTitulo = holder.editTitulo.getText().toString();
            String nuevaDescripcion = holder.editDescripcion.getText().toString();
            String nuevaPrioridad = holder.spinnerPrioridad.getSelectedItem().toString();
            String nuevoEstado = holder.spinnerEstado.getSelectedItem().toString();
            String nuevaFechaInicio = holder.editFechaInicio.getText().toString();
            String nuevaFechaLimite = holder.editFechaLimite.getText().toString();

            // Guardamos las fechas y título antiguos para comparar
            String fechaInicioAntigua = tarea.get("fechaHoraInicio");
            String fechaLimiteAntigua = tarea.get("fechaLimite");
            String tituloAntiguo = tarea.get("titulo");

            // Verificar si se modificaron las fechas o el título
            boolean fechasModificadas = !nuevaFechaInicio.equals(fechaInicioAntigua) ||
                    !nuevaFechaLimite.equals(fechaLimiteAntigua);
            boolean tituloModificado = !nuevoTitulo.equals(tituloAntiguo);

            // Comprobamos si se modifica el estado, para poder guardar la tarea sin importar
            // en la fecha que se creo
            boolean estadoCompletado = nuevoEstado.equalsIgnoreCase("Completada");

            try {
                // Solo validamos fechas si NO se está completando la tarea o si se han modificado las fechas
                if (!estadoCompletado || fechasModificadas)
                {
                    // Convertir las fechas a Calendar para validación
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault());
                    Calendar fechaInicioCalendar = Calendar.getInstance();
                    Calendar fechaFinCalendar = Calendar.getInstance();
                    Calendar ahora = Calendar.getInstance();

                    fechaInicioCalendar.setTime(formato.parse(nuevaFechaInicio));
                    fechaFinCalendar.setTime(formato.parse(nuevaFechaLimite));

                    // Validar que la fecha de inicio no sea posterior a la fecha de fin
                    if (fechaInicioCalendar.after(fechaFinCalendar)) {
                        Toast.makeText(context, "Error: La fecha de inicio no puede ser posterior a la fecha de fin", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Validar que las fechas no sean anteriores a la fecha actual
                    if (fechaInicioCalendar.before(ahora)) {
                        Toast.makeText(context, "Error: La fecha de inicio no puede ser anterior a la fecha actual", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (fechaFinCalendar.before(ahora)) {
                        Toast.makeText(context, "Error: La fecha de fin no puede ser anterior a la fecha actual", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            } catch (Exception e) {
                Toast.makeText(context, "Error al validar las fechas", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.editarTarea(Integer.parseInt(idTarea), nuevoTitulo, nuevaDescripcion,
                    nuevaPrioridad, nuevoEstado, nuevaFechaLimite, nuevaFechaInicio))
            {
                if (nuevoEstado.equalsIgnoreCase("completada"))
                {
                    listaTareas.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaTareas.size());
                    Toast.makeText(context, "Tarea completada y oculta", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    tarea.put("titulo", nuevoTitulo);
                    tarea.put("descripcion", nuevaDescripcion);
                    tarea.put("prioridad", nuevaPrioridad);
                    tarea.put("estado", nuevoEstado);
                    tarea.put("fechaHoraInicio", nuevaFechaInicio);
                    tarea.put("fechaLimite", nuevaFechaLimite);

                    notifyItemChanged(position);
                    Toast.makeText(context, "Tarea actualizada", Toast.LENGTH_SHORT).show();

                    // Si se modificaron las fechas o el título, mostrar los mensajes correspondientes
                    if (fechasModificadas || tituloModificado) {
                        if (tituloModificado) {
                            Toast.makeText(context, "Por favor, cambia el título del evento de '" + tituloAntiguo + "' a '" + nuevoTitulo + "' en tu calendario", Toast.LENGTH_LONG).show();
                        }
                        if (fechasModificadas) {
                            Toast.makeText(context, "Por favor, actualiza las fechas del evento '" + nuevoTitulo + "' en tu calendario", Toast.LENGTH_LONG).show();
                        }
                        // Abrir el calendario
                        abrirCalendario();
                    }
                }

                // Comprobamos si se ha cambiado la fecha limite de la tarea
                // Para reprogramar la alarma
                if (!nuevaFechaLimite.equals(fechaLimiteAntigua))
                {
                    try
                    {
                        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(formato.parse(nuevaFechaLimite));

                        // Llamamos a la clase NotificaciónHelper para reprogramar la alarma
                        NotificacionHelper.programarNotificacionDesdeBD(
                                context,
                                Integer.parseInt(idTarea),
                                nuevoTitulo,
                                calendar,
                                dbHelper
                        );
                        Log.d("TareaAdapter", "Notificación reprogramada correctamente tras la edición de la tarea.");
                    }
                    catch (Exception e)
                    {
                        Log.e("TareaAdapter", "Error al reprogramar la notificación: " + e.getMessage());
                    }
                }
            }
            else
            {
                // Si no, mostramos que a ocurrido un error al actualizar la tarea
                Toast.makeText(context, "Error al actualizar tarea", Toast.LENGTH_SHORT).show();
            }
            // Volvemos al modo de solo lectura (ocultamos campos editables)
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
     * @param holder .
     * @param enEdicion TRUE/FALSE segun si va a editar o no la tarea.
     */
    private void alternarModoEdicion(TareaViewHolder holder, boolean enEdicion)
    {
        // Si estamos en modo edición, los campos editables serán VISIBLES, los de solo texto se ocultan.
        // Si no estamos en modo edición, se hace lo contrario.
        int visEdicion = enEdicion ? View.VISIBLE : View.GONE;
        int visTexto = enEdicion ? View.GONE : View.VISIBLE;

        // Mostramos u ocultamos los TextViews (modo solo lectura)
        holder.textTitulo.setVisibility(visTexto);
        holder.textDescripcion.setVisibility(visTexto);
        holder.textPrioridad.setVisibility(visTexto);
        holder.textEstado.setVisibility(visTexto);
        holder.textFechaInicio.setVisibility(visTexto);
        holder.textFechaLimite.setVisibility(visTexto);

        // Mostramos u ocultamos los campos editables
        holder.editTitulo.setVisibility(visEdicion);
        holder.editDescripcion.setVisibility(visEdicion);
        holder.spinnerPrioridad.setVisibility(visEdicion);
        holder.spinnerEstado.setVisibility(visEdicion);
        holder.editFechaInicio.setVisibility(visEdicion);
        holder.editFechaLimite.setVisibility(visEdicion);

        // Mostramos el botón "Guardar" solo en modo edición
        holder.btnGuardar.setVisibility(visEdicion);

        // Mostramos el botón "Editar" solo en modo visualización
        holder.btnEditar.setVisibility(visTexto);
    }

    private void abrirCalendario() {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_APP_CALENDAR);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No se pudo abrir el calendario", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Clase donde obtenemos los textModel para visualizar las tareas que recuperamos.
     * .
     * Esta clase contiene referencias a los elementos visuales (TextView, EditText, Spinner, botones, etc.)
     * utilizados para mostrar y editar la información de una tarea en el RecyclerView.
     */
    static class TareaViewHolder extends RecyclerView.ViewHolder
    {
        TextView textTitulo, textDescripcion, textPrioridad, textEstado, textFechaInicio, textFechaLimite;
        TextView textNombreUsuario;
        EditText editTitulo, editDescripcion, editFechaInicio, editFechaLimite;
        Spinner spinnerPrioridad, spinnerEstado;
        MaterialButton btnEditar, btnEliminar;
        Button btnGuardar;
        View layoutDetalles;

        // Constructor que inicializa las vistas referenciándolas desde el XML del item
        public TareaViewHolder(@NonNull View itemView)
        {
            super(itemView);

            // Referencias a los TextView (modo solo lectura)
            textTitulo = itemView.findViewById(R.id.text_titulo);
            textNombreUsuario = itemView.findViewById(R.id.text_nombre_usuario);
            textDescripcion = itemView.findViewById(R.id.text_descripcion);
            textPrioridad = itemView.findViewById(R.id.text_prioridad);
            textEstado = itemView.findViewById(R.id.text_estado);
            textFechaInicio = itemView.findViewById(R.id.text_fecha_inicio);
            textFechaLimite = itemView.findViewById(R.id.text_fecha_limite);

            // Referencias a los campos EditText (modo edición)
            editTitulo = itemView.findViewById(R.id.edit_titulo);
            editDescripcion = itemView.findViewById(R.id.edit_descripcion);
            editFechaInicio = itemView.findViewById(R.id.edit_fecha_inicio);
            editFechaLimite = itemView.findViewById(R.id.edit_fecha_limite);

            // Referencias a los Spinners
            spinnerPrioridad = itemView.findViewById(R.id.spinner_prioridad);
            spinnerEstado = itemView.findViewById(R.id.spinner_estado);

            // Botones de acción
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
            btnGuardar = itemView.findViewById(R.id.btn_guardar);

            // Layout que contiene los detalles de la tarea
            layoutDetalles = itemView.findViewById(R.id.layout_detalles);
        }
    }
}
