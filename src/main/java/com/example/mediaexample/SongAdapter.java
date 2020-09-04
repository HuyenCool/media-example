package com.example.mediaexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongItemViewHolder> {
    private ArrayList<Song> songs;
    private Context context;

    public SongAdapter(ArrayList<Song> songs, Context context){
        this.songs = songs;
        this.context = context;
    }
    @NonNull
    @Override
    public SongAdapter.SongItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_name, parent, false);

        return new SongItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.SongItemViewHolder holder, int position) {
        Song a = songs.get(position);
        holder.title.setText(a.getTitle());
        holder.artist.setText(a.getArtist());
        holder.imageView.setImageBitmap(a.getImage());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class SongItemViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView artist;
        public ImageView imageView;
        public SongItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            artist = itemView.findViewById(R.id.artist);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
