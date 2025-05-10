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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, Object> perfil = lista.get(position);
        String nombre = (String) perfil.get("nombre");
        holder.nombre.setText(nombre);

        if ((int) perfil.get("id") == -1) {
            holder.imagen.setImageResource(R.drawable.baseline_person_24);
        } else {
            holder.imagen.setImageBitmap((Bitmap) perfil.get("foto"));
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
