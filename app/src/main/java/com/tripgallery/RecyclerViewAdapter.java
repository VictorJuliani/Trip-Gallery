package com.tripgallery;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by matheus on 10/26/15.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Post> posts;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ImageView photoView;
        public TextView hashtagsView;
        public Context context;
        public ViewHolder(View v) {
            super(v);
            context      = v.getContext();
            cardView     = (CardView) v.findViewById(R.id.cardView);
            photoView    = (ImageView) v.findViewById(R.id.photo);
            hashtagsView = (TextView) v.findViewById(R.id.hashtags);
        }
    }

    public RecyclerViewAdapter(List<Post> posts) {
        this.posts = posts;
    }

    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Post post = posts.get(position);

        Picasso p = Picasso.with(holder.context);
        p.setIndicatorsEnabled(true);
        p.load(post.url).into(holder.photoView) ;

        holder.hashtagsView.setText(post.hashtags);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
