package com.treb.reviewsearcher.productReview;

import android.os.AsyncTask;
import com.treb.reviewsearcher.Review;

import java.util.List;

public class UserReviewLoader extends AsyncTask<Review, Void, List<SubmittedProductReview>>
{
	private DetailedProductReviewLoader loader;

	public UserReviewLoader (DetailedProductReviewLoader loader)
	{
		this.loader = loader;
	}

	@Override
	protected void onPostExecute (List<SubmittedProductReview> submittedProductReviews)
	{
		loader.addUserReviews(submittedProductReviews);
	}

	@Override
	protected List<SubmittedProductReview> doInBackground (Review... reviews)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
