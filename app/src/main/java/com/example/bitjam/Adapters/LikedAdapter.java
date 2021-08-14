package com.example.bitjam.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitjam.R;
import com.example.bitjam.Models.Song;
import com.example.bitjam.Utils.OnRecyclerClickListener;
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
    public LikedAdapter(OnRecyclerClickListener<Song> listener) {
        mLikedSongs = new ArrayList<>();
        mRecyclerClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.default_recycler_items, parent, false);

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
            songNameHolder = itemView.findViewById(R.id.displaySongHolder);
            artistNameHolder = itemView.findViewById(R.id.displayArtistHolder);
            coverArtHolder = itemView.findViewById(R.id.displayCoverArtHolder);
        }

        public void bind(Song song) {
            songNameHolder.setText(song.getTitle());
            artistNameHolder.setText(song.getArtist());
            Picasso.get().load(song.getCoverUrl()).into(coverArtHolder);
            itemView.setOnClickListener(v -> mRecyclerClickListener.onItemClick(song));
        }
    }

    @Override
    public int getItemCount() {
        return mLikedSongs.size();
    }

    public void updateWith(List<Song> likedSongs) {
        int originalSize = mLikedSongs.size();

        mLikedSongs.clear();
        notifyItemRangeRemoved(0, originalSize);
        mLikedSongs.addAll(likedSongs);
        notifyItemRangeInserted(0, likedSongs.size());
    }
}
