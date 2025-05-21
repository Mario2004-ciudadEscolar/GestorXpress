package com.example.gestorxpress.ui.Cuenta;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.GestionPerfiles.SelectorAvatarDialog;
import com.example.gestorxpress.ui.GestionPerfiles.SelectorPerfilActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CuentaFragment extends Fragment {

    private ImageView imgPerfil;
    private byte[] imagenEnBytes;
    private EditText editCorreo, editPassword, editNombre, editApellido;
    private Button btnEditarGuardar, btnEliminarCuenta;
    private boolean enModoEdicion = false;
    private DatabaseHelper dbHelper;
    private int usuarioId;

    private ActivityResultLauncher<Intent> galeriaLauncher;

    public CuentaFragment()
    {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_cuenta, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        usuarioId = dbHelper.obtenerIdUsuario();

        if (usuarioId == -1)
        {
            Toast.makeText(requireContext(), "No se ha iniciado sesión", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return view;
        }

        imgPerfil = view.findViewById(R.id.imgPerfil);
        editCorreo = view.findViewById(R.id.editCorreo);
        editPassword = view.findViewById(R.id.editPassword);
        editNombre = view.findViewById(R.id.editNombre);
        editApellido = view.findViewById(R.id.editApellido);
        btnEditarGuardar = view.findViewById(R.id.btnEditarGuardar);
        btnEliminarCuenta = view.findViewById(R.id.btnEliminarCuenta);

        cargarDatosUsuario();

        btnEditarGuardar.setOnClickListener(v ->
        {
            if (!enModoEdicion) {
                enModoEdicion = true;
                cambiarModoEdicion(true);
                btnEditarGuardar.setText("Guardar");
            } else {
                guardarCambios();
                enModoEdicion = false;
                cambiarModoEdicion(false);
                btnEditarGuardar.setText("Editar");
            }
        });

        btnEliminarCuenta.setOnClickListener(v ->
        {
            boolean eliminado = dbHelper.eliminarUsuarioPorId(usuarioId);

            if (eliminado)
            {
                Toast.makeText(requireContext(), "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(requireContext(), SelectorPerfilActivity.class));
                requireActivity().finish();
            }
        });

        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result ->
                {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null)
                    {
                        Uri imageUri = result.getData().getData();
                        imgPerfil.setImageURI(imageUri);
                        try
                        {
                            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                            imagenEnBytes = getBytes(inputStream);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
        );

        imgPerfil.setOnClickListener(v ->
        {
            if (!enModoEdicion) return;

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Selecciona nueva imagen de perfil")
                    .setItems(new CharSequence[]{"Desde galería", "Desde avatares predefinidos"}, (dialog, which) -> {
                        if (which == 0)
                        {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            galeriaLauncher.launch(intent);
                        }
                        else
                        {
                            SelectorAvatarDialog dialogo = new SelectorAvatarDialog(imagen -> {
                                imagenEnBytes = imagen;
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imagen, 0, imagen.length);
                                imgPerfil.setImageBitmap(bitmap);
                            });
                            dialogo.show(getChildFragmentManager(), "selector_avatar");
                        }
                    }).show();
        });

        return view;
    }

    private void cargarDatosUsuario()
    {
        if (usuarioId != -1)
        {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT nombre, apellido, correo, fotoPerfil FROM Usuario WHERE id = ?", new String[]{String.valueOf(usuarioId)});

            if (cursor != null && cursor.moveToFirst())
            {
                int nombreIndex = cursor.getColumnIndex("nombre");
                int apellidoIndex = cursor.getColumnIndex("apellido");
                int correoIndex = cursor.getColumnIndex("correo");
                int fotoIndex = cursor.getColumnIndex("fotoPerfil");

                if (nombreIndex != -1 && apellidoIndex != -1 && correoIndex != -1 && fotoIndex != -1)
                {
                    byte[] imagenBytes = cursor.getBlob(fotoIndex);
                    String nombre = cursor.getString(nombreIndex);
                    String apellido = cursor.getString(apellidoIndex);
                    String correo = cursor.getString(correoIndex);

                    if (imagenBytes != null)
                    {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                        imgPerfil.setImageBitmap(bitmap);
                        imagenEnBytes = imagenBytes;
                    }

                    editCorreo.setText(correo);
                    editNombre.setText(nombre);
                    editApellido.setText(apellido);
                    editPassword.setText("********");
                }

                cursor.close();
            }
            else
            {
                Log.d("Database", "No se encontraron datos para el usuario con ID: " + usuarioId);
            }
        }
    }

    private void cambiarModoEdicion(boolean habilitar)
    {
        editNombre.setEnabled(habilitar);
        editApellido.setEnabled(habilitar);
        editPassword.setEnabled(habilitar);
    }

    private void guardarCambios()
    {
        String nuevoCorreo = editCorreo.getText().toString().trim();
        String nuevoNombre = editNombre.getText().toString().trim();
        String nuevoApellido = editApellido.getText().toString().trim();
        String nuevaPassword = editPassword.getText().toString().trim();

        if (nuevaPassword.equals("********") || nuevaPassword.isEmpty())
        {
            nuevaPassword = null;
        }

        dbHelper.actualizarUsuario(usuarioId, nuevoNombre, nuevoApellido, nuevoCorreo, nuevaPassword, imagenEnBytes);
        Toast.makeText(requireContext(), "Datos actualizados", Toast.LENGTH_SHORT).show();
    }

    private byte[] getBytes(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;

        while ((len = inputStream.read(buffer)) != -1)
        {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }
}
