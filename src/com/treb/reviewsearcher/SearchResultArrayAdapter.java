package com.treb.reviewsearcher;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.treb.reviewsearcher.productReview.ProductReviewActivity;

import java.text.DateFormat;
import java.util.List;

public class SearchResultArrayAdapter extends ArrayAdapter<Review>
{
	private final MainActivity activity;
	private DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);

	public SearchResultArrayAdapter (MainActivity activity, List<Review> reviews)
	{
		super(activity, R.layout.review_row);

		this.activity = activity;
		this.clear();
		for (Review review : reviews)
			this.add(review);

	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent)
	{
		View rowView = convertView;

		if (rowView == null)
		{
			rowView = activity.getLayoutInflater().inflate(R.layout.review_row, null, true);
			ViewHolder holder = new ViewHolder();
			holder.score = (TextView) rowView.findViewById(R.id.reviewRowScore);
			holder.title = (TextView) rowView.findViewById(R.id.reviewRowName);
			holder.platform = (TextView) rowView.findViewById(R.id.reviewRowPlatform);
			holder.releaseDate = (TextView) rowView.findViewById(R.id.reviewRowReleaseDate);
			rowView.setClickable(true);
			rowView.setTag(holder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		Review review = this.getItem(position);
		rowView.setOnClickListener(new View.OnClickListener()
		{

			public void onClick (View v)
			{
				ViewHolder holder = (ViewHolder) v.getTag();
				Intent intent = new Intent(activity, ProductReviewActivity.class);
				intent.putExtra("Review", holder.review);
				activity.startActivity(intent);
			}
		});

		holder.score.setText(review.getMetaScore() != null ? Integer.toString(review.getMetaScore()) : "");
		GradientDrawable shape = (GradientDrawable) holder.score.getBackground();
		shape.setColor(getColorForMetaScore(review.getMetaScore()));
		holder.title.setText(review.getTitle());
		holder.releaseDate.setText(review.getReleaseDate() != null ? df.format(review.getReleaseDate()) : "");
		holder.platform.setText(review.getPlatform());
		holder.review = review;

		return rowView;

	}

	private static class ViewHolder
	{
		public TextView score;
		public TextView title;
		public TextView releaseDate;
		public TextView platform;
		public Review review;

	}

	private int getColorForMetaScore (Integer score)
	{
		if (score == null)
			return Color.GRAY;
		else if (score > 80)
			return Color.GREEN;
		else if (score > 40)
			return Color.YELLOW;
		else
			return Color.RED;
	}
}
