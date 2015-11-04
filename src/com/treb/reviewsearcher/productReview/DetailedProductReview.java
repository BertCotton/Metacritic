package com.treb.reviewsearcher.productReview;

import android.graphics.Bitmap;
import com.treb.reviewsearcher.Review;

import java.util.ArrayList;
import java.util.List;

public class DetailedProductReview extends Review
{
	private Double userScore;
	private String summary;
	private Bitmap thumbnail;
	private String criticReviewUrl;
	private String userReviewUrl;
	private Integer criticReviewCount;
	private Integer userReviewCount;
	private List<SubmittedProductReview> criticReviews;
	private List<SubmittedProductReview> userReviews;

	public static DetailedProductReview FromReview (Review review)
	{
		DetailedProductReview detailedReview = new DetailedProductReview();
		detailedReview.setTitle(review.getTitle());
		detailedReview.setPlatform(review.getPlatform());
		detailedReview.setMetaScore(review.getMetaScore());

		detailedReview.criticReviewUrl = review.getUrl() + "/critic-reviews";
		detailedReview.userReviewUrl = review.getUrl() + "/user-reviews";


		detailedReview.setCriticReviews(new ArrayList<SubmittedProductReview>());
		detailedReview.setUserReviews(new ArrayList<SubmittedProductReview>());
		return detailedReview;

	}

	public Double getUserScore ()
	{
		return userScore;
	}

	public void setUserScore (Double userScore)
	{
		this.userScore = userScore;
	}

	public String getSummary ()
	{
		return summary;
	}

	public void setSummary (String summary)
	{
		this.summary = summary;
	}

	public List<SubmittedProductReview> getCriticReviews ()
	{
		return criticReviews;
	}

	public void setCriticReviews (List<SubmittedProductReview> criticReviews)
	{
		this.criticReviews = criticReviews;
	}

	public List<SubmittedProductReview> getUserReviews ()
	{
		return userReviews;
	}

	public void setUserReviews (List<SubmittedProductReview> userReviews)
	{
		this.userReviews = userReviews;
	}

	public void setThumbnailBitMap (Bitmap bitmap)
	{
		this.thumbnail = bitmap;
	}

	public Bitmap getThumbnail ()
	{
		return thumbnail;
	}

	public Integer getCriticReviewCount ()
	{
		return criticReviewCount;
	}

	public void setCriticReviewCount (Integer criticReviewCount)
	{
		this.criticReviewCount = criticReviewCount;
	}

	public Integer getUserReviewCount ()
	{
		return userReviewCount;
	}

	public void setUserReviewCount (Integer userReviewCount)
	{
		this.userReviewCount = userReviewCount;
	}
}
