package com.treb.reviewsearcher.productReview;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.treb.reviewsearcher.Constants;
import com.treb.reviewsearcher.R;
import com.treb.reviewsearcher.Review;

import java.text.DateFormat;


public class ProductReviewActivity extends Activity
{
	private DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
	private Review review;

	private TextView userScore;
	private TextView summary;
	private TextView platform;
	private TextView criticReviews;
	private TextView userReviews;
	private ImageView thumbnail;

	private ProgressDialog progressDialog;

	public void onCreate (Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.product_review);
		review = (Review) getIntent().getSerializableExtra("Review");

		progressDialog = ProgressDialog.show(this, "", "Loading Details...");

		TextView title = ((TextView) findViewById(R.id.productReviewTitle));
		title.setText(review.getTitle());
		title.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick (View view)
			{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(review.getUrl()));
				startActivity(intent);
			}
		});
		if (review.getMetaScore() != null)
		{
			TextView score = ((TextView) findViewById(R.id.productReviewMetaScore));
			score.setText(Integer.toString(review.getMetaScore()));
			GradientDrawable shape = (GradientDrawable) score.getBackground();
			shape.setColor(getColorForMetaScore(review.getMetaScore()));
		}

		((TextView) findViewById(R.id.productReviewReleaseDate)).setText((review.getReleaseDate() != null ? df.format(review.getReleaseDate()) : ""));

		userScore = (TextView) findViewById(R.id.productReviewUserScore);
		summary = (TextView) findViewById(R.id.productReviewSummary);

		platform = (TextView) findViewById(R.id.productReviewPlatform);
		thumbnail = (ImageView) findViewById(R.id.productReviewThumbnail);
		criticReviews = (TextView) findViewById(R.id.productReviewCriticReviews);
		userReviews = (TextView) findViewById(R.id.productReviewUserReviews);

		criticReviews.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick (View view)
			{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(review.getUrl() + "/critic-reviews"));
				startActivity(intent);
			}
		});


		userReviews.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick (View view)
			{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(review.getUrl() + "/user-reviews"));
				startActivity(intent);
			}
		});

		new DetailedProductReviewLoader(this).execute(review);
	}

	public void updateProgress (DetailedProductReview progress, boolean hideProgressBar)
	{
		if (progress.getSummary() != null && summary.getText().length() == 0)
		{
			summary.setText(Html.fromHtml(progress.getSummary()));
			summary.setMovementMethod(new ScrollingMovementMethod());
		}
		if (progress.getUserScore() != null && userScore.getText().length() == 0)
		{
			userScore.setText(Double.toString(progress.getUserScore()));
			GradientDrawable shape = (GradientDrawable) userScore.getBackground();
			shape.setColor(getColorForUserScore(progress.getUserScore()));
		}
		if (progress.getPlatform() != null && platform.getText().length() == 0)
			platform.setText(progress.getPlatform());
		if (progress.getThumbnail() != null && !thumbnail.isShown())
		{
			thumbnail.setImageBitmap(progress.getThumbnail());
			thumbnail.setVisibility(View.VISIBLE);
		}
		if (progress.getCriticReviewCount() != null)
			criticReviews.setText(getUnderlinedString("Critic Reviews (" + progress.getCriticReviewCount() + ")"));
		if (progress.getUserReviewCount() != null)
			userReviews.setText(getUnderlinedString("User Reviews (" + progress.getUserReviewCount() + ")"));

		Log.i(Constants.LOG_NAME, "Progress Update");
		if (hideProgressBar)
			hideDialog();
	}

	private SpannableString getUnderlinedString (String string)
	{
		SpannableString content = new SpannableString(string);
		content.setSpan(new UnderlineSpan(), 0, string.length(), 0);
		return content;
	}

	private int getColorForMetaScore (Integer score)
	{
		if (score > 80)
			return Color.GREEN;
		if (score > 40)
			return Color.YELLOW;
		else
			return Color.RED;
	}

	private int getColorForUserScore (Double score)
	{
		if (score > 8)
			return Color.GREEN;
		if (score > 4)
			return Color.YELLOW;
		else
			return Color.RED;
	}

	public void hideDialog ()
	{
		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.dismiss();
	}


}
