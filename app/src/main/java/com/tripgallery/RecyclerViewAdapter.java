package com.tripgallery;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
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
		public TextView hashtagsView;
		public Context context;
		public ImageView full_size;
		public Button shareButton;

		public ViewHolder(View v)
		{
			super(v);
			context = v.getContext();
			cardView = (CardView) v.findViewById(R.id.cardView);
			photoView = (ImageView) v.findViewById(R.id.photo);
			hashtagsView = (TextView) v.findViewById(R.id.hashtags);
			shareButton = (Button) v.findViewById(R.id.button);
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

		Picasso p = Picasso.with(holder.context);
		//p.setIndicatorsEnabled(true);
		p.load(post.url).into(holder.photoView);

		holder.hashtagsView.setText(post.hashtags);

		holder.photoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(holder.context, ImageFullSizeActivity.class);
				intent.putExtra("url", post.url);
				holder.context.startActivity(intent);

				//zoomImageFromThumb(holder.photoView, post.url, holder.context, expandedImageView, holder.cardView);
			}
		});

		holder.shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				shareOnFacebook(holder);

			}
		});
	}

	@Override
	public int getItemCount()
	{
		return posts.size();
	}

	public void shareOnFacebook(final ViewHolder holder){

		holder.photoView.buildDrawingCache();
		Bitmap image = holder.photoView.getDrawingCache();
		SharePhoto photo = new SharePhoto.Builder()
				.setBitmap(image)
				.build();
		SharePhotoContent content = new SharePhotoContent.Builder()
				.addPhoto(photo)
				.build();

	}
}
