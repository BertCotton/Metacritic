package com.treb.reviewsearcher.productsearcher;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.treb.reviewsearcher.Constants;
import com.treb.reviewsearcher.MainActivity;
import com.treb.reviewsearcher.RefinmentSelection;
import com.treb.reviewsearcher.Review;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetacriticSearcher extends AsyncTask<String, String, ArrayList<Review>>
{
	private static final Pattern pattern = Pattern.compile("[:\\-\\+\\*\\!\\/@#\\$%\\^\\&\\(\\)=_]");
	private static final String SearchURL = "http://www.metacritic.com/search/%s/%s/results";

	private MainActivity activity;
	private RefinmentSelection refinmentSelection;

	public MetacriticSearcher (MainActivity activity, RefinmentSelection refinmentSelection)
	{
		this.activity = activity;
		this.refinmentSelection = refinmentSelection;
	}

	private static String cleanupSearchName (String name)
	{
		Matcher matcher = pattern.matcher(name);
		String ret = matcher.replaceAll("");
		ret = Uri.encode(ret);
		ret = ret.replace("%20", "+");
		return ret;
	}
	
	public static String getSearchURL( RefinmentSelection refinmentSelection, String searchTerm)
	{
		return String.format(SearchURL, refinmentSelection.getSearchQuery(),  cleanupSearchName(searchTerm));
	}

	@Override
	protected ArrayList<Review> doInBackground (String... name)
	{

		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		try
		{
			if (name == null || name.length != 1)
				return null;
			String searchTerm = cleanupSearchName(name[0]);
			String url = String.format(SearchURL, refinmentSelection.getSearchQuery(), searchTerm);
			URLConnection conn = new URL(url).openConnection();
			conn.connect();

			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode node = cleaner.clean(new InputStreamReader(conn.getInputStream()));
			Object[] searchModuleNodes = node.evaluateXPath("//ul[@class='search_results module']");
			List<Object> resultNodes = new ArrayList<Object>();
			if(searchModuleNodes != null && searchModuleNodes.length ==  1 && searchModuleNodes[0] instanceof TagNode)
			{
				TagNode moduleNode = (TagNode)searchModuleNodes[0];
				resultNodes.addAll(moduleNode.getChildren());
				
			}
			
			Object[] queryResults = node.evaluateXPath("//p[@class='query_results']");
			String resultsString  = "";
			if(queryResults != null && queryResults.length > 0)
			{
				resultsString = ((TagNode)queryResults[0]).getText().toString() + "\n";
			}
			publishProgress(resultsString + "Parsing search results...");
			ArrayList<Review> reviews = new ArrayList<Review>(resultNodes.size());
			XPath xpath = XPathFactory.newInstance().newXPath();
			for (Object o : resultNodes)
			{
				if (o instanceof TagNode)
				{
					Document doc = new DomSerializer(new CleanerProperties()).createDOM((TagNode) o);
					Review review = new Review();
					String mediaType = getValueOfNode(doc, xpath, ".//div[contains(@class, 'result_type')]/strong");

					String platform = getValueOfNode(doc, xpath, ".//div[contains(@class, 'result_type')]//span[@class='platform']");

					String platformDisplay = "";
					if (platform == null || platform.length() == 0)
						platformDisplay = mediaType;
					else
						platformDisplay = mediaType + " [" + platform + "]";
					review.setPlatform(platformDisplay);

					review.setTitle(getValueOfNode(doc, xpath, ".//h3[contains(@class, 'product_title')]//a"));
					review.setUrl("http://www.metacritic.com" + getAttributeValueOfNode(doc, xpath, ".//h3[contains(@class, 'product_title')]//a", "href") + "?full_summary=1");
					String scoreString = getValueOfNode(doc, xpath, ".//span[contains(@class, 'score_favorable')]");
					if (scoreString == null || scoreString.isEmpty())
						scoreString = getValueOfNode(doc, xpath, ".//span[contains(@class, 'metascore')]");
					if (scoreString == null || scoreString.length() == 0)
						scoreString = getValueOfNode(doc, xpath, ".//span[contains(@class, 'score_outstanding')]");
					if (scoreString != null && scoreString.length() > 0 && !scoreString.equalsIgnoreCase("null"))
						try
						{
							review.setMetaScore(Integer.parseInt(scoreString));
						} catch (NumberFormatException nfe)
						{
							Log.e(Constants.LOG_NAME, "Error parsing score from " + scoreString);
						}
					String releaseDate = getValueOfNode(doc, xpath, ".//li[contains(@class, 'release_date')]//span[contains(@class, 'data')]");
					if (releaseDate != null && releaseDate.length() > 0)
						try {
							review.setReleaseDate(df.parse(releaseDate));
						}
						catch(ParseException ex)
						{
							Log.e(Constants.LOG_NAME, "Error parsing release date from " + releaseDate);
						}
					reviews.add(review);
				}

			}

			return reviews;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;

	}

	private String getValueOfNode (Document doc, XPath xpath, String xPath) throws XPathException
	{
		return (String) xpath.evaluate(xPath + "/text()", doc, XPathConstants.STRING);
	}

	private String getAttributeValueOfNode (Document doc, XPath xpath, String xPath, String attribute) throws XPathException
	{
		return (String) xpath.evaluate(xPath + "/@" + attribute, doc, XPathConstants.STRING);
	}

	@Override
	protected void onProgressUpdate (String... values)
	{
		activity.displayProgressMessage(values[0], null);
	}

	@Override
	protected void onPostExecute (ArrayList<Review> result)
	{
		try
		{
			activity.showSearchResults(result);
		} catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(activity, "Error retriving the reviews", Toast.LENGTH_SHORT);
		}
	}


}
