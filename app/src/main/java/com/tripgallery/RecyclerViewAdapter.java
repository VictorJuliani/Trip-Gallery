package com.tripgallery;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tripgallery.activity.ImageFullSizeActivity;

import java.util.List;

/**
 * Created by matheus on 10/26/15.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
{
	private Animator mCurrentAnimator;

	/**
	 * The system "short" animation time duration, in milliseconds. This duration is ideal for
	 * subtle animations or animations that occur very frequently.
	 */

	private List<Post> posts;

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public CardView cardView;
		public ImageView photoView;
        public TextView locationLabelView;
		public TextView hashtagsView;
		public Context context;
		public Button shareButton;

		public ViewHolder(View v)
		{
			super(v);
			context = v.getContext();
			cardView = (CardView) v.findViewById(R.id.cardView);
			photoView = (ImageView) v.findViewById(R.id.photo);
			hashtagsView = (TextView) v.findViewById(R.id.hashtags);
			shareButton = (Button) v.findViewById(R.id.shareButton);
            locationLabelView = (TextView) v.findViewById(R.id.locationLabel);
		}
	}

	public RecyclerViewAdapter(List<Post> posts)
	{
		this.posts = posts;
	}

	public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		final Post post = posts.get(position);

		Picasso.with(holder.context).load(post.url).into(holder.photoView);

		holder.hashtagsView.setText(post.hashtags);
		holder.locationLabelView.setText(post.location);

		holder.photoView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent intent = new Intent(holder.context, ImageFullSizeActivity.class);
				intent.putExtra("url", post.url);
				holder.context.startActivity(intent);
			}
		});

		holder.shareButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				holder.photoView.buildDrawingCache();
				Intent intent = App.sharePhoto(holder.context, holder.photoView.getDrawingCache());
				holder.context.startActivity(Intent.createChooser(intent, holder.context.getString(R.string.how_share)));
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return posts.size();
	}
}