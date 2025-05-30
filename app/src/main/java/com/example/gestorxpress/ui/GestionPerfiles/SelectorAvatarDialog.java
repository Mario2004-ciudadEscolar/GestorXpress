package com.example.gestorxpress.ui.GestionPerfiles;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gestorxpress.R;

import java.io.ByteArrayOutputStream;

/**
 * Autor: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class SelectorAvatarDialog extends DialogFragment
{

    /**
     * Interfaz de devolución de llamada para notificar cuando un avatar (imagen) ha sido seleccionado
     */
    public interface OnAvatarSelected
    {
        // Se invoca el metodo cuando el usuario selecciona un avatar (imagen)
        void onAvatar(byte[] imagen);
    }

    // Atributo que lo utilizaremos al seleccionar un avatar (imagen)
    private OnAvatarSelected callback;

    /**
     * Constructor que recibe un callback para manejar la imagen seleccionado
     * @param callback Interfaz para manejar la selección del avatar
     */
    public SelectorAvatarDialog(OnAvatarSelected callback)
    {
        this.callback = callback;
    }

    /**
     * Crea e inicializa el diálogo que contiene las opciones de avatar.
     *.
     * Se genero automaticamente al crear el javaDocs.
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return El diálogo personalizado listo para mostrarse.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        // Crea un nuevo diálogo sin título
        Dialog dialog = new Dialog(requireContext());

        // Inflar la vista del diseño personalizado
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_selector_avatar, null);

        // Asignar la vista inflada al contenido del diálogo
        dialog.setContentView(view);

        // Configura cada imagen de avatar con su respectivos recursos
        setupAvatar(view.findViewById(R.id.avatar1), R.drawable.avatar1);
        setupAvatar(view.findViewById(R.id.avatar2), R.drawable.avatar2);
        setupAvatar(view.findViewById(R.id.avatar3), R.drawable.avatar3);
        setupAvatar(view.findViewById(R.id.avatar4), R.drawable.avatar4);

        return dialog;
    }

    /**
     * Configura un ImageView para representar un avatar, y gestiona su selección.
     *
     * @param view La vista de la imagen del avatar
     * @param resId El ID del recurso drawable del avatar
     */
    private void setupAvatar(ImageView view, int resId)
    {
        view.setOnClickListener(v ->
        {
            // Decodifica el recurso de imagen a un objeto Bitmap
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);

            // Convertir el Bitmap a un arreglo de bytes (PNG)
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            // Ejecuta el callback con la imagen seleccionada
            callback.onAvatar(stream.toByteArray());

            // Cierra el diálogo tras la selección de imagen
            dismiss();
        });
    }
}
