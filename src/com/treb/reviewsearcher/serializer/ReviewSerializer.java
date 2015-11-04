package com.treb.reviewsearcher.serializer;

import com.treb.reviewsearcher.Review;

import java.util.ArrayList;

public class ReviewSerializer
{
	public static String serialize (ArrayList<Review> reviews)
	{
		StringBuilder sb = new StringBuilder();
		for (Review review : reviews)
		{
			sb.append(review.toString()).append(";");
		}
		return sb.toString();

	}

	public static ArrayList<Review> deSerialize (String reviews)
	{

		ArrayList<Review> reviewList = new ArrayList<Review>();
		if (reviews == null || reviews.equals("null"))
			return reviewList;
		String[] reviewStrings = reviews.split(";");
		for (String reviewString : reviewStrings)
		{
			Review review = Review.fromString(reviewString);
			if (review != null && review.getTitle() != null)
				reviewList.add(review);
		}
		return reviewList;
	}
}
