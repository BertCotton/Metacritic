package com.treb.reviewsearcher.productReview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import com.treb.reviewsearcher.Constants;

import java.net.URL;

public class ImageLoader extends AsyncTask<String, Void, Bitmap>
{
	private DetailedProductReviewLoader loader;

	public ImageLoader (DetailedProductReviewLoader loader)
	{
		this.loader = loader;
	}

	@Override
	protected Bitmap doInBackground (String... strings)
	{
		try
		{

			URL url = new URL(strings[0]);
			return BitmapFactory.decodeStream(url.openConnection().getInputStream());
		} catch (Exception ex)
		{
			Log.e(Constants.LOG_NAME, "Error getting thumbnail", ex);
		}
		return null;

	}

	@Override
	protected void onPostExecute (Bitmap bitmap)
	{
		loader.setThumbnailBitMap(bitmap);
	}
}
