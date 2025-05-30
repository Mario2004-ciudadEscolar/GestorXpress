package com.example.gestorxpress.ui.GestionPerfiles;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorxpress.R;

import java.util.HashMap;
import java.util.List;

public class PerfilAdapterSinClase extends RecyclerView.Adapter<PerfilAdapterSinClase.ViewHolder> {

    // Interfaz para manejar los clics en los perfiles (osea cuando selecionas un usuario)
    public interface OnPerfilClick
    {
        void onClick(HashMap<String, Object> perfil);
    }

    // Colección de perfiles a mostrar (una lista de usuarios)
    private final List<HashMap<String, Object>> lista;

    // Listener para los clics sobre los perfiles (usuarios)
    private final OnPerfilClick listener;

    // Constructor con parametros
    public PerfilAdapterSinClase(List<HashMap<String, Object>> lista, OnPerfilClick listener)
    {
        this.lista = lista;
        this.listener = listener;
    }

    /**
     * Método que se llama cuando RecyclerView necesita crear un nuevo ViewHolder.
     * Infla la vista del layout item_perfil y la envuelve en un ViewHolder.
     * .
     * (Lo de aqui abajo me lo ha generado automaticamente al poner un comentario javaDocs)
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return ViewHolder Devuelve el nuevo ViewHolder con esa vista
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_perfil, parent, false);
        return new ViewHolder(vista);
    }

    /**
     * Enlaza los datos del perfil a la vista del ViewHolder para mostrar en la posición especificada.
     * .
     * Obtiene el perfil de la lista según la posición y configura la vista del holder dependiendo del contenido:
     * .
     *  Si el ID es -1, muestra un ícono de añadir sin texto y sin borde redondo.
     *  Si el ID es distinto de -1, muestra el nombre, la foto con borde redondo y ajusta la escala de la imagen.
     * .
     * Además, configura un listener para manejar el clic sobre el ítem, pasando el perfil correspondiente.
     *
     * @param holder   el ViewHolder que contiene las vistas que se van a actualizar
     * @param position la posición del elemento dentro de la lista de datos
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        // Obtiene el perfil en la posición correspondiente
        HashMap<String, Object> perfil = lista.get(position);

        // Obtenemos el id del perfil (usuario) y lo guardamos en una variable
        int id = (int) perfil.get("id");

        // Si el ID es -1, representa la opción de 'Añadir perfil'
        if (id == -1)
        {
            // Icono de "añadir perfil"
            holder.nombre.setText("Añadir perfil");
            holder.imagen.setImageResource(R.drawable.ic_add_circle);
            holder.imagen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
        else
        {
            // Si obtiene un id existente en la bbdd, mostramos su nombre e imagen
            holder.nombre.setText((String) perfil.get("nombre"));
            holder.imagen.setImageBitmap((Bitmap) perfil.get("foto"));
            holder.imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        // Esto maneja el clics sobre el ítem (perfiles 'usuarios')
        holder.itemView.setOnClickListener(v -> listener.onClick(perfil));
    }

    // Devuelve la cantidad total de ítems (perfiles) en la lista
    @Override
    public int getItemCount() {return lista.size();}

    // ViewHolder interno que representa una fila del RecyclreView
    // 'RecyclreView' se utiliza para mostrar listas o cuadrículas de
    // elementos de forma eficiente y flexible.
    static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imagen;
        TextView nombre;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            // Obtiene referencias a los componentes visuales dentro del layout del ítem
            imagen = itemView.findViewById(R.id.imagenPerfil);
            nombre = itemView.findViewById(R.id.nombrePerfil);
        }
    }
}
