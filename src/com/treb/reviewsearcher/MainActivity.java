package com.treb.reviewsearcher;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.treb.reviewsearcher.productsearcher.GoogleSearcher;
import com.treb.reviewsearcher.productsearcher.MetacriticSearcher;
import com.treb.reviewsearcher.scanner.IntentIntegrator;
import com.treb.reviewsearcher.scanner.IntentResult;

import java.util.ArrayList;

public class MainActivity extends Activity
{

	private static final String PREFS_NAME = "ReviewSearcherPrefsFile";

	private SearchResultArrayAdapter searchResultArrayAdapter;
	private ProgressDialog progressDialog;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Barcode scanner button
		ImageButton scan = (ImageButton) findViewById(R.id.btnScan);
		scan.setOnClickListener(new View.OnClickListener()
		{

			public void onClick (View v)
			{
				// Onclick start the barcode scanner
				IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
				integrator.initiateScan();
			}
		});

		// Search by text 
		ImageButton search = (ImageButton) findViewById(R.id.btnSearch);
		search.setOnClickListener(new View.OnClickListener()
		{

			public void onClick (View arg0)
			{
				startSearchFromTextBox();
			}
		});

		// Handle the case of the enter key being pressed
		TextView input = (TextView) findViewById(R.id.searchBox);
		input.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey (View view, int i, KeyEvent keyEvent)
			{
				// Only fire event on KeyUp to prevent double happenings when holding a key down.
				if (keyEvent.getAction() == KeyEvent.ACTION_UP)
				{
					switch (i)
					{
						// On enter or the DPAD Center action, initiate a search by text
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:
							startSearchFromTextBox();
							return true;
					}
				}
				return false;
			}
		});

		Spinner refinmentSpinner = (Spinner) findViewById(R.id.refinmentSpinner);
		ArrayAdapter<RefinmentSelection> refinementAdapter = new ArrayAdapter<RefinmentSelection>(this, android.R.layout.simple_spinner_item);
		for(RefinmentSelection refinmentSelection : RefinmentSelection.values())
		{
			refinementAdapter.add(refinmentSelection);
		}
		refinementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		refinmentSpinner.setAdapter(refinementAdapter);


		// Create the search results adapter and set it to an empty list
		ListView resultsView = (ListView) findViewById(R.id.resultsListView);
		searchResultArrayAdapter = new SearchResultArrayAdapter(this, new ArrayList<Review>());
		resultsView.setAdapter(searchResultArrayAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menuOpenInBrowser:
				TextView input = (TextView) findViewById(R.id.searchBox);
				String searchTerm = input.getText().toString();
				RefinmentSelection refinmentSelection = (RefinmentSelection)((Spinner)findViewById(R.id.refinmentSpinner)).getSelectedItem();
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MetacriticSearcher.getSearchURL(refinmentSelection, searchTerm)));
				startActivity(browserIntent);
				break;
		}

		return true;
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case IntentIntegrator.REQUEST_CODE:
				IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
				if (scanResult != null)
				{
					String upc = scanResult.getContents();
					if (upc != null)
						searchFromUPC(upc);
					else
						showToast("Unable to get UPC from scan.");
				}
				break;
		}
	}

	private void startSearchFromTextBox ()
	{
		TextView input = (TextView) findViewById(R.id.searchBox);
		String searchTerm = input.getText().toString();
		// Only search if something is in the textbox
		if (searchTerm != null && searchTerm.length() > 0)
		{
			searchForProduct(searchTerm);
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
		} else
			showToast("Enter a product to search for.");
	}

	public void displayProgressMessage (final String message, final AsyncTask cancellableTask)
	{
		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.hide();
		progressDialog = new ProgressDialog(MainActivity.this);
		progressDialog.setMessage(message);
		if (cancellableTask != null)
		{
			progressDialog.setButton("Cancel", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick (DialogInterface dialogInterface, int i)
				{
					cancellableTask.cancel(true);
				}
			});
		}
		progressDialog.show();
	}

	public void hideProgressDialog ()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run ()
			{
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
			}
		});

	}

	public void searchFromUPC (String searchTerm)
	{
		GoogleSearcher searcher = new GoogleSearcher(MainActivity.this);
		// Update the search box with the UPC 
		((TextView) findViewById(R.id.searchBox)).setText(searchTerm);
		displayProgressMessage("Searching for product from barcode.", searcher);
		searcher.execute(searchTerm);
	}

	public void showSearchResults (ArrayList<Review> reviews)
	{
		displayProgressMessage("Loading results...", null);
		if (reviews == null || reviews.size() == 0)
		{
			Review emptyReview = new Review();
			emptyReview.setTitle("No reviews found");
			reviews.add(emptyReview);
		} else
		{
			for (Review review : reviews)
			{
				searchResultArrayAdapter.add(review);
			}
		}

		runOnUiThread(new Runnable()
		{
			public void run ()
			{
				searchResultArrayAdapter.notifyDataSetChanged();
			}
		});
		hideProgressDialog();
	}

	public void searchForProduct (String productName)
	{

		// Clear everything from the list and start the MetaCritic search
		searchResultArrayAdapter.clear();

		// Update the text box 
		((TextView) findViewById(R.id.searchBox)).setText(productName);
		RefinmentSelection refinmentSelection = (RefinmentSelection)((Spinner)findViewById(R.id.refinmentSpinner)).getSelectedItem();
		MetacriticSearcher searcher = new MetacriticSearcher(MainActivity.this, refinmentSelection);
		displayProgressMessage("Searching for reviews for '" + productName + "'", searcher);
		searcher.execute(productName);
	}

	public void showToast (final String message)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run ()
			{
				hideProgressDialog();
				Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

			}
		});

	}
}


