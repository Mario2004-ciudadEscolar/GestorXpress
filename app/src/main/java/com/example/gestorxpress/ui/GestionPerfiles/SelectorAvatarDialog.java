package com.example.gestorxpress.ui.GestionPerfiles;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gestorxpress.R;

import java.io.ByteArrayOutputStream;

public class SelectorAvatarDialog extends DialogFragment {

    public interface OnAvatarSelected {
        void onAvatar(byte[] imagen);
    }

    private OnAvatarSelected callback;

    public SelectorAvatarDialog(OnAvatarSelected callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_selector_avatar, null);
        dialog.setContentView(view);

        setupAvatar(view.findViewById(R.id.avatar1), R.drawable.avatar1);
        setupAvatar(view.findViewById(R.id.avatar2), R.drawable.avatar2);
        setupAvatar(view.findViewById(R.id.avatar3), R.drawable.avatar3);
        setupAvatar(view.findViewById(R.id.avatar4), R.drawable.avatar4);

        return dialog;
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);
        
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);
        
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        
        // Calcular el rectángulo de origen para centrar la imagen
        int left = (width - size) / 2;
        int top = (height - size) / 2;
        Rect srcRect = new Rect(left, top, left + size, top + size);
        
        canvas.drawBitmap(bitmap, srcRect, rect, paint);
        
        return output;
    }

    private void setupAvatar(ImageView view, int resId) {
        view.setOnClickListener(v -> {
            try {
                // Decodificar la imagen con opciones para obtener dimensiones
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(getResources(), resId, options);

                // Calcular el factor de escala
                int maxDimension = 512; // Tamaño máximo para cualquier dimensión
                int scale = 1;
                while (options.outWidth / scale > maxDimension || options.outHeight / scale > maxDimension) {
                    scale *= 2;
                }

                // Decodificar la imagen con el factor de escala
                options.inJustDecodeBounds = false;
                options.inSampleSize = scale;
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId, options);

                if (bitmap != null) {
                    // Convertir a circular
                    Bitmap circularBitmap = getCircularBitmap(bitmap);
                    
                    // Comprimir la imagen
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] imageData = stream.toByteArray();

                    // Verificar el tamaño final
                    if (imageData.length > 500 * 1024) { // Si es mayor a 500KB
                        stream.reset();
                        circularBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
                        imageData = stream.toByteArray();
                        
                        int quality = 85;
                        while (imageData.length > 500 * 1024 && quality > 50) {
                            stream.reset();
                            quality -= 5;
                            circularBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
                            imageData = stream.toByteArray();
                        }
                    }

                    // Liberar memoria
                    if (bitmap != circularBitmap) {
                        bitmap.recycle();
                    }
                    circularBitmap.recycle();

                    callback.onAvatar(imageData);
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error al procesar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
