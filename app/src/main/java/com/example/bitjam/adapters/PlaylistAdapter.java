package com.example.bitjam.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitjam.R;
import com.example.bitjam.models.Playlist;
import com.example.bitjam.utils.OnRecyclerClickListener;
import com.example.bitjam.utils.PopupBuilder;
import com.example.bitjam.viewmodels.PlaylistViewModel;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private final List<Playlist> mPlaylists;
    private final OnRecyclerClickListener<Playlist> mRecyclerClickListener;
    private final PlaylistViewModel mPlaylistVM;

    /**
     * The adapter for the {@link RecyclerView} in {@link com.example.bitjam.fragments.PlaylistsFragment}.
     *
     * @param listener          Requires the {@link OnRecyclerClickListener} interface to perform
     *                          click functionality
     * @param playlistViewModel A reference to {@link PlaylistViewModel}.
     */
    // We really shouldn't be holding a reference to PlaylistViewModel in the adapter. The adapter
    // is only supposed to structure the RecyclerView and add click functionality. However, I am
    // using this for now to add some convenience to Android's already difficult architecture :(
    public PlaylistAdapter(OnRecyclerClickListener<Playlist> listener, PlaylistViewModel playlistViewModel) {
        mPlaylists = new ArrayList<>();
        mRecyclerClickListener = listener;
        mPlaylistVM = playlistViewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.playlist_recycler_items, parent, false);

        return new ViewHolder(view);
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
        final ImageView popupMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleHolder = itemView.findViewById(R.id.dialogTitleHolder);
            sizeHolder = itemView.findViewById(R.id.dialogSizeHolder);
            popupMenu = itemView.findViewById(R.id.playlistMenu);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Playlist playlist) {
            int size = playlist.getSongRefs().size();
            sizeHolder.setText(size + (size == 1 ? " song" : " songs"));
            titleHolder.setText(playlist.getTitle());
            itemView.setOnClickListener(view -> mRecyclerClickListener.onItemClick(playlist));

            popupMenu.setOnClickListener(v ->
                    PopupBuilder.forPlaylist(v, new PopupBuilder.OnPlaylistMenuItemSelected() {
                        @Override
                        public void onEditPlaylistNameItemSelected() {
                            // Abandoned as it might be too much of a hassle
                        }

                        // We delete the playlist from Firestore as well as update the localised
                        // instance. We shouldn't be doing the latter and should actually observe
                        // the database instance of the playlists, and update this list based on
                        // any changes to the db.
                        @Override
                        public void onDeletePlaylistSelected() {
                            int position = getBindingAdapterPosition();
                            mPlaylistVM.deletePlaylistInDb(mPlaylists.get(position).getId());
                            mPlaylists.remove(position);
                            notifyItemRemoved(position);
                        }
                    })
            );
        }
    }

    // Callback to add playlists to the localised recycler list
    public void updateWith(List<Playlist> playlists) {
        int originalSize = mPlaylists.size();

        mPlaylists.clear();
        notifyItemRangeRemoved(0, originalSize);
        mPlaylists.addAll(playlists);
        notifyItemRangeInserted(0, playlists.size());
    }
}
