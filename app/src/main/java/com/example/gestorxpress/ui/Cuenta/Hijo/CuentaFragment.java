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

/**
 * Autor: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class CuentaFragment extends Fragment
{
    private ImageView imgPerfil;
    private byte[] imagenEnBytes;
    private EditText editCorreo, editPassword, editNombre, editApellido, editPassword2;
    private Button btnEditarGuardar;
    private boolean enModoEdicion = false;

    // Clase DatabaseHelper (Donde realizamos todas las interacciones con la bbdd SLQite)
    private DatabaseHelper dbHelper;
    private int usuarioId;

    private ActivityResultLauncher<Intent> galeriaLauncher;

    // Constructor sin parametros
    public CuentaFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Instancia de la base de datos
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
        editPassword2 = view.findViewById(R.id.editPassword2);
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
         * Lo que hacemos aqui es que cuando el usuario de el boton de editar, hacemos
         * una comprobación mediante un boolean (que aprincipio va estar falso, ya que no se
         * podra editar de momento), se active el modo 'edición' y llamamos un metodo que
         * que deja al usuario editar sus datos personales y por ultimo le cambiamos el
         * nombre del boton.
         * .
         * Luego guardamos todos los cambios que hace el usuario, ya no estara en modo
         * 'Edición' y cambiaremos el nombre del boton.
         */
        btnEditarGuardar.setOnClickListener(v ->
        {
            // Comprueba si actualmente no estamos en modo edición
            if (!enModoEdicion)
            {
                // Entra en modo edición
                // Indicamos que ahora sí estamos en modo edición
                enModoEdicion = true;

                // Llamamos a un metodo donde habilitamos EditText para que el usuario
                // pueda editar el contenido que esta en los EditText.
                cambiarModoEdicion(true);

                // Cambiamos el nombre del boton a 'Guardar'
                btnEditarGuardar.setText("Guardar");
            }
            else
            {
                // Llamamos el metodo donde vamos a guardar los cambios que a realizado el
                // usuario a la bbdd.
                guardarCambios();

                // Ahora el modo edición pasa a ser falso
                enModoEdicion = false;

                // Cambiamos el modo edición, osea que ya no se puede editar los EditText
                cambiarModoEdicion(false);

                // Cambiamos el nombre del boton a 'Editar'
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
        if (usuarioId != -1)
        {
            //Instancia a la BBDD y permite Lectura (Read)
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // El resultado se guarda en el 'Cursor', que permite recorrer los resultados fila por fila.
            Cursor consulta = db.rawQuery("SELECT nombre, apellido, correo, contrasenia, fotoPerfil FROM Usuario WHERE id = ?", new String[]{String.valueOf(usuarioId)});

            if (consulta != null && consulta.moveToFirst())
            {
                int nombreIndex = consulta.getColumnIndex("nombre");
                int apellidoIndex = consulta.getColumnIndex("apellido");
                int correoIndex = consulta.getColumnIndex("correo");
                int fotoIndex = consulta.getColumnIndex("fotoPerfil");

                if (nombreIndex == -1 || apellidoIndex == -1 || correoIndex == -1 || fotoIndex == -1)
                {
                    Toast.makeText(requireContext(), "Error: columnas no encontradas en la base de datos", Toast.LENGTH_SHORT).show();
                    consulta.close();
                    return;
                }

                byte[] imagenBytes = consulta.getBlob(fotoIndex);
                String nombre = consulta.getString(nombreIndex);
                String apellido = consulta.getString(apellidoIndex);
                String correo = consulta.getString(correoIndex);

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
                editPassword2.setText("");
            }

            if (consulta != null) consulta.close();
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
        editPassword.setEnabled(habilitar);
        editPassword2.setEnabled(habilitar);
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
        String nuevaPassword2 = editPassword2.getText().toString().trim();

        // Validamos si el usuario no cambió la contraseña (dejó la que estaba o vacío)
        if (nuevaPassword.equals("********") || nuevaPassword.isEmpty())
        {
            nuevaPassword = null; // No cambiamos la contraseña
        }
        else
        {
            if(!esContraseniaValida(nuevaPassword))
            {
                Toast.makeText(requireContext(), "Contraseña no válida: debe contener una mayúscula, " +
                        "un carácter especial y tener más de 6 caracteres.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!nuevaPassword.equals(nuevaPassword2))
            {
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Si no hay nueva imagen seleccionada, obtener la imagen actual
        if (imagenEnBytes == null)
        {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor consulta = db.rawQuery("SELECT fotoPerfil FROM Usuario WHERE id = ?", new String[]{String.valueOf(usuarioId)});

            if (consulta.moveToFirst())
            {
                int fotoIndex = consulta.getColumnIndex("fotoPerfil");
                if (fotoIndex != -1)
                {
                    imagenEnBytes = consulta.getBlob(fotoIndex);
                }
            }
            if (consulta != null) consulta.close();
            db.close();
        }

        // Validar que la imagen sea válida
        if (imagenEnBytes == null || imagenEnBytes.length == 0)
        {
            Toast.makeText(requireContext(), "Error con la imagen de perfil. Por favor selecciona una válida.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que la imagen no sea demasiado grande (nuevo límite 5MB)
        if (imagenEnBytes.length > 5 * 1024 * 1024)
        {
            Toast.makeText(requireContext(), "La imagen es demasiado grande. Se intentará comprimir.", Toast.LENGTH_SHORT).show();
            try
            {
                // Intentar comprimir la imagen existente
                Bitmap bitmap = BitmapFactory.decodeByteArray(imagenEnBytes, 0, imagenEnBytes.length);
                ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, compressedStream);
                imagenEnBytes = compressedStream.toByteArray();
            }
            catch (Exception e)
            {
                Toast.makeText(requireContext(), "Error al procesar la imagen. Por favor, selecciona otra.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Intentar actualizar el usuario
        try
        {
            dbHelper.actualizarUsuario(usuarioId, nuevoNombre, nuevoApellido, nuevoCorreo, nuevaPassword, imagenEnBytes);
            Toast.makeText(requireContext(), "Datos actualizados", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(requireContext(), "Error al actualizar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

        // Convertir el stream a bitmap para poder comprimirlo
        byte[] imageData = byteBuffer.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

        // Si el bitmap es null, retornar null
        if (bitmap == null)
        {
            return null;
        }

        // Redimensionar si la imagen es muy grande
        int maxWidth = 2048;
        int maxHeight = 2048;
        if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight)
        {
            float ratio = Math.min(
                    (float) maxWidth / bitmap.getWidth(),
                    (float) maxHeight / bitmap.getHeight()
            );
            bitmap = Bitmap.createScaledBitmap(bitmap,
                    (int)(bitmap.getWidth() * ratio),
                    (int)(bitmap.getHeight() * ratio),
                    true);
        }

        // Comprimir la imagen con calidad adaptativa
        ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();
        int quality = 95; // Empezamos con calidad alta
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, compressedStream);

        // Si la imagen es muy grande, reducimos la calidad gradualmente
        while (compressedStream.size() > 5 * 1024 * 1024 && quality > 50)
        { // 5MB límite
            compressedStream.reset();
            quality -= 5;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, compressedStream);
        }

        return compressedStream.toByteArray();
    }


    /**
     * Valida que la contraseña cumpla con los requisitos:
     * - Mínimo 7 caracteres
     * - Que contenga al menos una letra mayúscula
     * - Que contenga al menos un carácter especial
     *
     * @param contrasenia La contraseña a validar
     * @return true si cumple, false si no cumple
     */
    public boolean esContraseniaValida(String contrasenia)
    {
        if (contrasenia == null) return false;
        if (contrasenia.length() <= 6) return false;

        // Para que al menos contenga una mayúscula
        boolean tieneMayuscula = contrasenia.matches(".*[A-Z].*");

        // Para que al menos contenga un carácter especial
        boolean tieneCaracterEspecial = contrasenia.matches(".*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>?/].*");

        return tieneMayuscula && tieneCaracterEspecial;
    }
}
