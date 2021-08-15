package com.example.bitjam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitjam.R;
import com.example.bitjam.models.Song;
import com.example.bitjam.utils.OnRecyclerClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {
    private final List<Song> mSongs;
    private final OnRecyclerClickListener<Song> mOnRecyclerClickListener;

    /**
     * The adapter for the {@link RecyclerView} in {@link LibraryAdapter}.
     *
     * @param listener Requires the {@link OnRecyclerClickListener} interface to perform click
     *                 functionality
     */
    public LibraryAdapter(OnRecyclerClickListener<Song> listener) {
        mSongs = new ArrayList<>();
        mOnRecyclerClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflates rows to be visible on screen
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.default_recycler_items, parent, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        Song songInfo = mSongs.get(index);
        holder.bind(songInfo);
    }

    // inner class to hold a reference to each item of RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView songNameHolder;
        final TextView artistNameHolder;
        final ImageView coverArtHolder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coverArtHolder = itemView.findViewById(R.id.displayCoverArtHolder);
            songNameHolder = itemView.findViewById(R.id.displaySongHolder);
            artistNameHolder = itemView.findViewById(R.id.displayArtistHolder);
        }

        // binds row details based on the song
        // also binds an onClickListener to the item view
        public void bind(Song song) {
            songNameHolder.setText(song.getTitle());
            artistNameHolder.setText(song.getArtist());
            Picasso.get().load(song.getCoverUrl()).into(coverArtHolder);
            itemView.setOnClickListener(v -> mOnRecyclerClickListener.onItemClick(song));
        }
    }

    // invoked by layout manager
    // without this, there will not be any rows visible
    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    // used as a callback in live observers
    public void updateWith(List<Song> songs) {
        int originalSize = mSongs.size();

        mSongs.clear();
        notifyItemRangeRemoved(0, originalSize);
        mSongs.addAll(songs);
        notifyItemRangeInserted(0, songs.size());
    }
}
