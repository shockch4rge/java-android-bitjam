package com.example.bitjam.Fragments.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitjam.Adapters.DialogAdapter;
import com.example.bitjam.Adapters.OnRecyclerClickListener;
import com.example.bitjam.Models.Playlist;
import com.example.bitjam.Models.Song;
import com.example.bitjam.R;
import com.example.bitjam.Utils.Anims;
import com.example.bitjam.Utils.Misc;
import com.example.bitjam.ViewModels.PlaylistViewModel;

import java.util.List;

public class DialogAddToPlaylist extends DialogFragment {
    private final String TAG = "(DialogFragment)";
    private PlaylistViewModel playlistVM;
    private RecyclerView mDialogRecycler;
    private DialogAdapter mDialogAdapter;
    private final OnRecyclerClickListener<Playlist> mOnRecyclerClickListener;

    /**
     * Inflates the dialog's view.
     * @param listener an interface that should be implemented in a parent fragment of
     *                 this {@link DialogFragment}.
     */
    public DialogAddToPlaylist(OnRecyclerClickListener<Playlist> listener) {
        mOnRecyclerClickListener = listener;
    }

    // Just like a Fragment's onCreateView, only we can't use view binding as the container to hold
    // this DialogFragment is already predefined. We can't use view groups either as view hierarchy
    // gets incredibly messy, and we have to return a Dialog whereas in a Fragment we can return
    // the view binder's root view.
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Starts building an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.misc_dialog_add_to_playlist, null);

        // A lot of assignments relate to this fragment's parent. This is because a dialog fragment
        // is a child of the fragment from whence it came from.
        playlistVM = new ViewModelProvider(requireParentFragment()
                .requireActivity())
                .get(PlaylistViewModel.class);

        // Set the layout of the dialog, one 'close' button included
        builder.setView(v)
                .setNegativeButton("CLOSE",
                        (dialog, which) -> dialog.dismiss());

        mDialogRecycler = v.findViewById(R.id.dialogRecycler);

        // Standard RecyclerView setup. We pass in the listener that we implemented in
        // the parent fragment.
        mDialogAdapter = new DialogAdapter(mOnRecyclerClickListener);
        mDialogRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mDialogRecycler.setAdapter(mDialogAdapter);

        // If there is a change in playlists, update DialogAdapter
        playlistVM.getPlaylists().observe(requireParentFragment().getViewLifecycleOwner(), playlists -> {
            mDialogAdapter.updateAdapter(playlists);
            Anims.setLayoutAnimFall(mDialogRecycler);
        });

        return builder.create();
    }
}
