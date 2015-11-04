package com.treb.reviewsearcher.productReview;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.treb.reviewsearcher.Constants;
import com.treb.reviewsearcher.Review;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.List;

public class DetailedProductReviewLoader extends AsyncTask<Review, DetailedProductReview, DetailedProductReview>
{
	private ProductReviewActivity activity;
	private DetailedProductReview detailedProductReview;
	private DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);

	public DetailedProductReviewLoader (ProductReviewActivity activity)
	{
		this.activity = activity;
	}

	@Override
	protected DetailedProductReview doInBackground (Review... reviews)
	{
		if (reviews == null || reviews.length != 1)
			return null;

		Review review = reviews[0];

		detailedProductReview = DetailedProductReview.FromReview(review);

		this.publishProgress(detailedProductReview);

		try
		{
			Log.i(Constants.LOG_NAME, "Getting detailed review from url:" + review.getUrl());
			URLConnection conn = new URL(review.getUrl()).openConnection();
			conn.connect();
			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode node = cleaner.clean(new InputStreamReader(conn.getInputStream()));
			Log.i(Constants.LOG_NAME, "Parsing results for review");
			Object[] mainNodes = node.evaluateXPath("//div[@id='main']");
			if (mainNodes.length == 1 && mainNodes[0] instanceof TagNode)
			{
				XPath xpath = XPathFactory.newInstance().newXPath();
				org.w3c.dom.Document doc = new DomSerializer(new CleanerProperties()).createDOM((TagNode) mainNodes[0]);

				String collapsedSummary = getValueOfNode(doc, xpath, "//li[contains(@class, 'summary_detail')]//span[contains(@class, 'blurb_collapsed')]");
				String expandedSummary = getValueOfNode(doc, xpath, "//li[contains(@class, 'summary_detail')]//span[contains(@class, 'blurb_expanded')]");

				detailedProductReview.setSummary(collapsedSummary + expandedSummary);
				publishProgress(detailedProductReview);

				String userScore = getValueOfNode(doc, xpath, "//div[contains(@class, 'avguserscore')]//span[contains(@class, 'score_value')]");
				if (userScore != null && userScore.length() > 0)
				{
					detailedProductReview.setUserScore(Double.parseDouble(userScore));
					publishProgress(detailedProductReview);
				}

				String thumbnail = getAttributeValueOfNode(doc, xpath, "//img[contains(@class, 'product_image')]", "src");
				if (thumbnail != null)
					new ImageLoader(this).execute(thumbnail);

				Integer criticReviewCount = getTotalReviews(doc, xpath, "//div[contains(@class, 'critic_reviews_module')]//li[contains(@class, 'score_count')]//span[contains(@class, 'count')]");
				Integer userReviewCount = getTotalReviews(doc, xpath, "//div[contains(@class, 'user_reviews_module')]//li[contains(@class, 'score_count')]//span[contains(@class, 'count')]");

				detailedProductReview.setCriticReviewCount(criticReviewCount);
				detailedProductReview.setUserReviewCount(userReviewCount);
			}
		} catch (Exception ex)
		{
			Log.e(Constants.LOG_NAME, "Error getting detailed info", ex);
		}

		return detailedProductReview;
	}


	private Integer getTotalReviews (Document doc, XPath xpath, String path) throws XPathException
	{
		Integer criticReviewCount = 0;

		Object nodeSet = xpath.evaluate(path, doc, XPathConstants.NODESET);
		if (nodeSet instanceof NodeList)
		{
			Log.i(Constants.LOG_NAME, "Walking NodeList");
			NodeList nodeList = (NodeList) nodeSet;
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				String countVal = nodeList.item(i).getTextContent();
				Log.i(Constants.LOG_NAME, "Count Val: " + countVal);
				if (countVal != null && countVal.length() > 0)
					criticReviewCount += Integer.parseInt(countVal);
			}
		}

		return criticReviewCount;
	}

	@Override
	protected void onProgressUpdate (DetailedProductReview... values)
	{
		activity.updateProgress(values[0], false);
	}

	@Override
	protected void onPostExecute (DetailedProductReview detailedProductReview)
	{
		activity.updateProgress(detailedProductReview, true);
	}

	private String getValueOfNode (org.w3c.dom.Document doc, XPath xpath, String xPath) throws XPathException
	{
		return (String) xpath.evaluate(xPath + "/text()", doc, XPathConstants.STRING);
	}

	private String getAttributeValueOfNode (org.w3c.dom.Document doc, XPath xpath, String xPath, String attribute) throws XPathException
	{
		return (String) xpath.evaluate(xPath + "/@" + attribute, doc, XPathConstants.STRING);
	}

	public void addCriticReviews (List<SubmittedProductReview> submittedProductReviews)
	{
		detailedProductReview.setCriticReviews(submittedProductReviews);
		this.publishProgress(detailedProductReview);
	}

	public void addUserReviews (List<SubmittedProductReview> submittedProductReviews)
	{
		detailedProductReview.setUserReviews(submittedProductReviews);
		this.publishProgress(detailedProductReview);
	}

	public void setThumbnailBitMap (Bitmap bitmap)
	{
		detailedProductReview.setThumbnailBitMap(bitmap);
		this.publishProgress(detailedProductReview);
	}
}
