package com.example.bitjam.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitjam.Fragments.Dialogs.DialogAddToPlaylist;
import com.example.bitjam.R;
import com.example.bitjam.Models.Song;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class LikedAdapter extends RecyclerView.Adapter<LikedAdapter.ViewHolder> {
    private final List<Song> mLikedSongs;
    private final OnRecyclerClickListener<Song> mRecyclerClickListener;

    /**
     * An adapter for the {@link RecyclerView} in {@link LikedAdapter}.
     *
     * @param listener Requires the {@link OnRecyclerClickListener} interface to perform click
     *                 functionality
     */
    // We instead of passing a reference to the original liked songs, we instantiate a new local
    // ArrayList as we want to be able to update the adapter's songs independently.
    public LikedAdapter(OnRecyclerClickListener<Song> listener) {
        mLikedSongs = new ArrayList<>();
        mRecyclerClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.search_recycler_items, parent, false);

        return new ViewHolder(v);
    }

    // Bind details of each song at each recycler list index to the ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull LikedAdapter.ViewHolder holder, int index) {
        Song likedSongInfo = mLikedSongs.get(index);
        holder.bind(likedSongInfo);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView songNameHolder;
        final TextView artistNameHolder;
        final ImageView coverArtHolder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songNameHolder = itemView.findViewById(R.id.recycler_row_song);
            artistNameHolder = itemView.findViewById(R.id.recycler_row_artist);
            coverArtHolder = itemView.findViewById(R.id.recycler_row_cover_art);
        }

        public void bind(Song song) {
            songNameHolder.setText(song.getSongName());
            artistNameHolder.setText(song.getArtistName());
            Picasso.get().load(song.getCoverArtLink()).into(coverArtHolder);
            itemView.setOnClickListener(v -> mRecyclerClickListener.onItemClick(song));
        }
    }

    @Override
    public int getItemCount() {
        return mLikedSongs.size();
    }

    public void updateAdapter(List<Song> likedSongs) {
        mLikedSongs.clear();
        notifyItemRangeRemoved(0, likedSongs.size());
        mLikedSongs.addAll(likedSongs);
        notifyItemRangeInserted(0, likedSongs.size());
    }
}
