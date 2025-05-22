package com.example.gestorxpress.ui.Cuenta.Hijo;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.Cuenta.Padre.CuentaPadreActivity;
import com.example.gestorxpress.ui.GestionPerfiles.SelectorAvatarDialog;
import com.example.gestorxpress.ui.GestionPerfiles.SelectorPerfilActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CuentaFragment extends Fragment {

    private ImageView imgPerfil;
    private byte[] imagenEnBytes;
    private EditText editCorreo, editPassword, editNombre, editApellido;
    private Button btnEditarGuardar;
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
        //View view = inflater.inflate(R.layout.fragment_cuenta, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Llamamos el metodo que tenemos en nuestra clase de la bbdd para obtener el id
        // del usuario que esta logeado.
        usuarioId = dbHelper.obtenerIdUsuario();

        // Verificamos si se obtuvo un ID válido
        if (usuarioId == -1)
        {
            // Si el ID es -1, significa que no hay ningún usuario logueado
            Toast.makeText(requireContext(), "No se ha iniciado sesión", Toast.LENGTH_SHORT).show();
            requireActivity().finish(); // Terminamos la actividad si no hay usuario logueado
            return null;
        }

        // Solo inflamos el layout si NO es padre
        View view = inflater.inflate(R.layout.fragment_cuenta, container, false);

        /**
         * Verificamos si el usuario que esta ahora mismo logeado es el padre
         * y si es el padre se abrira una pagina que solo lo puede ver el padre (administrador)
          */
        if (dbHelper.esUsuarioPadrePorId(usuarioId))
        {
            Intent intent = new Intent(requireContext(), CuentaPadreActivity.class); // Reemplaza con tu actividad real
            startActivity(intent);
            requireActivity().finish();
            return null;
        }

        imgPerfil = view.findViewById(R.id.imgPerfil);
        editCorreo = view.findViewById(R.id.editCorreo);
        editPassword = view.findViewById(R.id.editPassword);
        editNombre = view.findViewById(R.id.editNombre);
        editApellido = view.findViewById(R.id.editApellido);
        btnEditarGuardar = view.findViewById(R.id.btnEditarGuardar);


        /**
         * Llamamos a un metodo donde vamos a recuperar los datos del usuario que esta logeado en este
         * momento en nuestra aplicación y cargarlo en la información de "cuenta", ya que ahi puede editar
         * la cuenta ya sea el correo, nombre, apellido o contraseña,
         */
        cargarDatosUsuario();

        /**
         * Lo que hacemos aqui es que cuando el usuario de el boton de editar, ese boton pase
         * ser a guardar, ya que cambiamos el nombre y el modo de edición que lo logramos
         * haciendolo con un boolena.
         */
        btnEditarGuardar.setOnClickListener(v ->
        {
            if (!enModoEdicion)
            {
                // Activar edición
                enModoEdicion = true;
                cambiarModoEdicion(true);
                btnEditarGuardar.setText("Guardar");
            }
            else
            {
                // Guardar cambios
                guardarCambios();
                enModoEdicion = false;
                cambiarModoEdicion(false);
                btnEditarGuardar.setText("Editar");
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

        /**
         * Configura un listener para el clic en la imagen de perfil (imgPerfil) que permite al usuario
         * seleccionar una nueva imagen solo si está en modo edición.
         *.
         * Al hacer clic, se muestra un diálogo con dos opciones:
         * 1. Seleccionar una imagen desde la galería.
         * 2. Elegir un avatar predefinido desde un diálogo personalizado.
         *.
         * La imagen seleccionada se convierte a {@code byte[]} y se muestra en el {@code ImageView}.
         */
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

    /**
     * Lo que hacemos en este metodo es cargar la información del usuario y mostrarlo en la pagina,
     * ya que aqui el usuario puede ver su información personal y lo puede editar o actualizar si asi lo desea
     * el usuario o tambien puede eliminar su cuenta (pero eso ya se hace con otro metodo que tenemos
     * definifo).
     */
    private void cargarDatosUsuario()
    {
        // Si el usuario está logueado (es decir, el ID es válido)
        if (usuarioId != -1)
        {
            // Obtener los datos del usuario directamente desde la base de datos
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
                    // Comprobar si las columnas existen en el cursor
                    byte[] imagenBytes = cursor.getBlob(fotoIndex); // para el cambio de foto
                    String nombre = cursor.getString(nombreIndex);
                    String apellido = cursor.getString(apellidoIndex);
                    String correo = cursor.getString(correoIndex);

                    if (imagenBytes != null)
                    {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                        imgPerfil.setImageBitmap(bitmap);
                        imagenEnBytes = imagenBytes;
                    }

                    // Mostramos la información personal sacada al usuario y lo ponemos en los editTest
                    editCorreo.setText(correo);
                    editNombre.setText(nombre);
                    editApellido.setText(apellido);
                    editPassword.setText("********"); // No mostrar la contraseña real
                }

                cursor.close();
            }
            else
            {
                Log.d("Database", "No se encontraron datos para el usuario con ID: " + usuarioId);
            }
        }
    }

    /**
     * Lo que hace este metodo es que cuando el usuario quiere editar su información, es que
     * habilitamos los editTest para que puede editarlo o actualizar su información.
     * @param habilitar Para habilitar la información.
     */
    private void cambiarModoEdicion(boolean habilitar)
    {
        editNombre.setEnabled(habilitar);
        editApellido.setEnabled(habilitar);
        editPassword.setEnabled(habilitar);
    }

    /**
     * Si el usuario realiza algún cambio, lo que hacemos es un UPDATE en la bbdd
     * que para ello ya tenemos un metodo en nuestra bbdd.
     * .
     * NOTA: TENGO QUE DARLE UNA VUELTA ESTE METODO, SE PUEDE HACER MEJOR.
     */
    private void guardarCambios()
    {
        String nuevoCorreo = editCorreo.getText().toString().trim();
        String nuevoNombre = editNombre.getText().toString().trim();
        String nuevoApellido = editApellido.getText().toString().trim();
        String nuevaPassword = editPassword.getText().toString().trim();

        // Si el campo sigue con los ****** no cambiamos la contraseña
        // que me daba error y si cambio solo la foto me cambiaba la contraseña y no podia entrar una liada
        if (nuevaPassword.equals("********") || nuevaPassword.isEmpty())
        {
            nuevaPassword = null;
        }

        dbHelper.actualizarUsuario(usuarioId, nuevoNombre, nuevoApellido, nuevoCorreo, nuevaPassword, imagenEnBytes);
        Toast.makeText(requireContext(), "Datos actualizados", Toast.LENGTH_SHORT).show();
    }


    /**
     * Lee todos los datos de un {@link InputStream} y los convierte en un arreglo de bytes.
     *.
     * Este método es útil cuando necesitas convertir archivos, imágenes u otros flujos de datos
     * en memoria para almacenarlos o procesarlos como un arreglo de bytes (byte[]).
     *.
     * @param inputStream El flujo de entrada desde el cual se leerán los datos.
     * @return Un arreglo de bytes que contiene todos los datos leídos del InputStream.
     * @throws IOException Si ocurre un error de lectura durante el proceso.
     */
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
