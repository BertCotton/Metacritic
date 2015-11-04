package com.treb.reviewsearcher.productsearcher;


import android.os.AsyncTask;
import android.util.Log;
import com.treb.reviewsearcher.Constants;
import com.treb.reviewsearcher.MainActivity;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

public class GoogleSearcher extends AsyncTask<String, Void, String>
{
	private MainActivity activity;
	private static final String GOOGLE_QUERY = "http://www.google.com/products?q=%s";

	public GoogleSearcher (MainActivity activity)
	{
		this.activity = activity;
	}


	@Override
	protected String doInBackground (String... params)
	{
		if (params == null || params.length != 1)
			return null;
		String productName = null;
		try
		{
			URLConnection conn = new URL(String.format(GOOGLE_QUERY, params[0])).openConnection();
			conn.connect();

			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode node = cleaner.clean(new InputStreamReader(conn.getInputStream()));
			Object[] resultNodes = node.evaluateXPath("//h3[@class='result-title']//a");

			// If the divs length is 0 then nothing was returned
			// Otherwise remove anything that is in square bracket.  This is often the media type (ie. [CD])
			if (resultNodes.length == 0)
				activity.showToast("Product name not found on google...strange...");
			else if (resultNodes[0] instanceof TagNode)
				productName = ((TagNode) resultNodes[0]).getText().toString().replaceAll("\\[(.*)\\]", "").trim();


			Log.i(Constants.LOG_NAME, "Product Found: " + productName);
			return productName;
		} catch (SocketTimeoutException ex)
		{
			Log.i(Constants.LOG_NAME, "Timeout getting product name from barcode.");
			activity.showToast("Product search from barcode timed out. Either try again or type in the name into the search box.");
			return null;
		} catch (Exception ex)
		{
			Log.e(Constants.LOG_NAME, "Error getting product from UPC.", ex);
			return null;
		}
	}

	@Override
	protected void onPostExecute (String result)
	{
		if (result != null)
			activity.searchForProduct(result);
		else
			activity.showToast("Error getting product from UPC.");
	}

}
