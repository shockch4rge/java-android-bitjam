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

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.ViewHolder> {
    private final List<Song> mSongs;
    private final OnRecyclerClickListener<Song> mRecyclerClickListener;

    public PlayerAdapter(OnRecyclerClickListener<Song> listener) {
        mSongs = new ArrayList<>();
        mRecyclerClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.player_recycler_items, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        Song song = mSongs.get(index);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
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

        // bind to ViewHolder
        public void bind(Song song) {
            songNameHolder.setText(song.getTitle());
            artistNameHolder.setText(song.getArtist());
            Picasso.get().load(song.getCoverUrl()).into(coverArtHolder);
            itemView.setOnClickListener(v -> mRecyclerClickListener.onItemClick(song));
        }
    }

    // notifyDataSetChanged is an incredibly expensive and slow method. Instead, we should manually
    // call notifyItemRangeRemoved and notifyItemRangeInserted. This also preserves any animations,
    // should we pass in any.
    public void updateSongs(List<Song> songs) {
        mSongs.clear();
        notifyItemRangeRemoved(0, songs.size());
        mSongs.addAll(songs);
        notifyItemRangeInserted(0, songs.size());
    }
}
