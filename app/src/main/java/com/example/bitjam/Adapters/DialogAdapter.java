package com.example.bitjam.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitjam.Dialogs.DialogAddToPlaylist;
import com.example.bitjam.Models.Playlist;
import com.example.bitjam.R;
import com.example.bitjam.Utils.OnRecyclerClickListener;

import java.util.ArrayList;
import java.util.List;

public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.ViewHolder> {
    private final List<Playlist> mPlaylists;
    private final OnRecyclerClickListener<Playlist> mOnRecyclerClickListener;

    /**
     * An adapter for the {@link RecyclerView} in {@link DialogAddToPlaylist}.
     *
     * @param listener Requires the {@link OnRecyclerClickListener} interface to perform click
     *                 functionality
     */
    public DialogAdapter(OnRecyclerClickListener<Playlist> listener) {
        mPlaylists = new ArrayList<>();
        mOnRecyclerClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.dialog_recycler_items, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist playlistInfo = mPlaylists.get(position);
        holder.bind(playlistInfo);
    }

    @Override
    public int getItemCount() {
        return mPlaylists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titleHolder;
        final TextView sizeHolder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleHolder = itemView.findViewById(R.id.dialogTitleHolder);
            sizeHolder = itemView.findViewById(R.id.dialogSizeHolder);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Playlist playlist) {
            int size = playlist.getSongRefs().size();
            sizeHolder.setText(size + (size == 1 ? " song" : " songs"));
            titleHolder.setText(playlist.getTitle());
            itemView.setOnClickListener(v -> mOnRecyclerClickListener.onItemClick(playlist));
        }
    }

    public void updateAdapter(List<Playlist> playlists) {
        int originalSize = mPlaylists.size();

        mPlaylists.clear();
        notifyItemRangeRemoved(0, originalSize);
        mPlaylists.addAll(playlists);
        notifyItemRangeInserted(0, playlists.size());
    }
}
