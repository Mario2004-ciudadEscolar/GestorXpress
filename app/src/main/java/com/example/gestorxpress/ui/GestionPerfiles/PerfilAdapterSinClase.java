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

    public interface OnPerfilClick {
        void onClick(HashMap<String, Object> perfil);
    }

    private final List<HashMap<String, Object>> lista;
    private final OnPerfilClick listener;

    public PerfilAdapterSinClase(List<HashMap<String, Object>> lista, OnPerfilClick listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, Object> perfil = lista.get(position);
        int id = (int) perfil.get("id");

        if (id == -1)
        {
            // Icono de "añadir perfil"
            holder.nombre.setText("Añadir perfil");
            holder.imagen.setImageResource(R.drawable.ic_add_circle);
            holder.imagen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
        else
        {
            holder.nombre.setText((String) perfil.get("nombre"));
            holder.imagen.setImageBitmap((Bitmap) perfil.get("foto"));
            holder.imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(perfil));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imagenPerfil);
            nombre = itemView.findViewById(R.id.nombrePerfil);
        }
    }
}
