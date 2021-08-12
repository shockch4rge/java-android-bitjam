package com.example.bitjam.Fragments.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.bitjam.R;

public class DialogCreatePlaylist extends DialogFragment {
    private DialogCreatePlaylistListener mDialogListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.misc_dialog_create_playlist, null);

        EditText titleField = view.findViewById(R.id.dialogEnterNameField);

        // Constructs the layout of the dialog.
        // Includes 2 buttons: 'Create' & 'Cancel'.
        builder.setView(view)
                .setPositiveButton("CREATE",
                        (dialog, which) -> mDialogListener.onCreateSelected(titleField))
                .setNegativeButton("CANCEL",
                        (dialog, which) -> dialog.dismiss())
                .setOnDismissListener(dialog -> titleField.getText().clear());

        return builder.create();
    }

    // Interface for the parent fragment to implement. An EditText must be passed in to allow
    // the interaction.
    public interface DialogCreatePlaylistListener {
        void onCreateSelected(EditText titleField);
    }

    public void setDialogListener(DialogCreatePlaylistListener listener) {
        this.mDialogListener = listener;
    }
}
