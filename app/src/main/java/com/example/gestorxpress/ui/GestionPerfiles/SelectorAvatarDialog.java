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

    private void setupAvatar(ImageView view, int resId) {
        view.setOnClickListener(v -> {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            callback.onAvatar(stream.toByteArray());
            dismiss();
        });
    }
}
