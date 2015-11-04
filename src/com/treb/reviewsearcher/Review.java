package com.treb.reviewsearcher;

import java.io.Serializable;
import java.util.Date;

public class Review implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String title;
	private Integer metaScore;
	private String platform;
	private Date releaseDate;
	private String url;

	public void setTitle (String title)
	{
		this.title = title;
	}

	public String getTitle ()
	{
		return title;
	}

	public void setMetaScore (Integer metaScore)
	{
		this.metaScore = metaScore;
	}

	public Integer getMetaScore ()
	{
		return metaScore;
	}

	public void setPlatform (String platform)
	{
		this.platform = platform;
	}

	public String getPlatform ()
	{
		return platform;
	}

	public String getUrl ()
	{
		return url;
	}

	public void setUrl (String url)
	{
		this.url = url;
	}

	@Override
	public String toString ()
	{
		return "title:" + title + ", metaScore:" + metaScore + "url:" + url;
	}

	public static Review fromString (String input)
	{
		Review review = new Review();
		String[] parts = input.split(",");
		for (String part : parts)
		{
			String[] items = part.split(":");
			if (items.length == 2)
			{
				String key = items[0];
				if (key.equals("title"))
					review.title = items[1];
				else if (key.equals("metaScore") && items[1].length() > 0)
					review.metaScore = Integer.parseInt(items[1]);
				else if (key.equals("url"))
					review.url = items[1];
			}

		}
		return review;
	}


	public Date getReleaseDate ()
	{
		return releaseDate;
	}

	public void setReleaseDate (Date releaseDate)
	{
		this.releaseDate = releaseDate;
	}
}
