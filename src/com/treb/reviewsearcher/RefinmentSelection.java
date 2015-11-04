package com.treb.reviewsearcher;

public enum RefinmentSelection
{
	ALL("All", "all"),
	MOVIES("Movies", "movie"),
	GAMES("Games", "game"),
	ALBUMS("Albums", "album"),
	TV_SHOWS("TV Shows", "tv"),
	PERSON("Person", "person"),
	TRAILERS("Trailers", "video"),
	COMPANIES("Companies", "company");



	private String value;
	private String searchQuery;
	
	private RefinmentSelection(String value, String searchQuery)
	{
		this.value = value;
		this.searchQuery = searchQuery;
	}
	
	public String getSearchQuery()
	{
		return searchQuery;
	}

	@Override
	public String toString ()
	{
		return value;
	}
}
