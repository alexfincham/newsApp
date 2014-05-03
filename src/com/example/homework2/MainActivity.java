package com.example.homework2;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	static final public String MYPREFS = "myprefs";
	static final public String PREF_URL = "restore_url";
	static final public String MY_WEBPAGE = "http://users.soe.ucsc.edu/~luca/android.html";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		aList = new ArrayList<ListElement>();
		aa = new MyAdapter(this, R.layout.li_element, aList);
		ListView myListView = (ListView) findViewById(R.id.listView1);
		myListView.setAdapter(aa);
		aa.notifyDataSetChanged();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private static final String LOG_TAG = "MainActivity";
	private static final int MAX_SETUP_DOWNLOAD_TRIES = 3;
	
	// ****Change this.****
	private static final String DOWNLOAD_URL = "http://luca-ucsc.appspot.com/jsonnews/default/news_sources.json";
	
	// Background downloader.
	private BackgroundDownloader downloader = null;
	
	@Override
	protected void onResume() {
		super.onResume();
		downloader = new BackgroundDownloader();
		downloader.execute(DOWNLOAD_URL);	
	}
	
    // This class downloads from the net the camera setup instructions.
    private class BackgroundDownloader extends AsyncTask<String, String, String> {
    	
    	protected String doInBackground(String... urls) {
    		Log.d(LOG_TAG, "Starting the download.");
    		String downloadedJson = null;
    		String urlString = urls[0];
    		URI url = URI.create(urlString);
    		int numTries = 0;
    		while (downloadedJson == null && numTries < MAX_SETUP_DOWNLOAD_TRIES && !isCancelled()) {
    			numTries++;
    			HttpGet request = new HttpGet(url);
    			DefaultHttpClient httpClient = new DefaultHttpClient();
    			HttpResponse response = null;
    			try {
    				response = httpClient.execute(request);
    			} catch (ClientProtocolException ex) {
    				Log.e(LOG_TAG, ex.toString());
    			} catch (IOException ex) {
    				Log.e(LOG_TAG, ex.toString());
    			}
    			if (response != null) {
    				// Checks the status code.
    				int statusCode = response.getStatusLine().getStatusCode();
    				Log.d(LOG_TAG, "Status code: " + statusCode);

    				if (statusCode == HttpURLConnection.HTTP_OK) {
    					// Correct response. Reads the real result.
    					// Extracts the string content of the response.
    					HttpEntity entity = response.getEntity();
    					InputStream iStream = null;
    					try {
    						iStream = entity.getContent();
    					} catch (IOException ex) {
    						Log.e(LOG_TAG, ex.toString());
    					}
    					if (iStream != null) {
    						downloadedJson = ConvertStreamToString(iStream);
    						Log.d(LOG_TAG, "Received string: " + downloadedJson);
    				    	return downloadedJson;
    					}
    				}
    			}
    		}
    		// Returns the instructions, if any.
    		return downloadedJson;
    	}
    	
    	protected void onPostExecute(String s) {
    		// This is executed in the UI thread.
    		parseJson(s);
    	}
    	
    }
    
    @Override
    // This stops the downloader as soon as possible.
    public void onStop() {
    	if (downloader != null) {
    		downloader.cancel(false);
    	}
    	aa.clear();
    	super.onStop();
    }
    
    public static String ConvertStreamToString(InputStream is) {
    	
    	if (is == null) {
    		return null;
    	}
    	
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append((line + "\n"));
	        }
	    } catch (IOException e) {
	        Log.d(LOG_TAG, e.toString());
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            Log.d(LOG_TAG, e.toString());
	        }
	    }
	    return sb.toString();
	}

	private class ListElement {
		ListElement() {};
		
		public String siteUrl;
		public String textLabel;
		public String buttonLabel;
	}
	
	private ArrayList<ListElement> aList;
	
	private class MyAdapter extends ArrayAdapter<ListElement>{

		int resource;
		Context context;
		
		public MyAdapter(Context _context, int _resource, List<ListElement> items) {
			super(_context, _resource, items);
			resource = _resource;
			context = _context;
			this.context = _context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout newView;
			
			ListElement w = getItem(position);
			
			// Inflate a new view if necessary.
			if (convertView == null) {
				newView = new LinearLayout(getContext());
				String inflater = Context.LAYOUT_INFLATER_SERVICE;
				LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
				vi.inflate(resource,  newView, true);
			} else {
				newView = (LinearLayout) convertView;
			}
			
			// Fills in the view.
			TextView tv = (TextView) newView.findViewById(R.id.listText);
			Button b = (Button) newView.findViewById(R.id.listButton);
			tv.setText(w.textLabel);
			b.setText(w.buttonLabel);
			
			String url = w.siteUrl;

			// Sets a listener for the button, and a tag for the button as well.
			b.setTag(Integer.toString(position));
			b.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//*** Code to start "Read" Activity
					// Gets the integer tag of the button.
					String s = (String) v.getTag();
					int pos = Integer.parseInt(s);
					ListElement el = getItem(pos);
					
					// Save corresponding news site url in shared preferences
					SharedPreferences settings = getSharedPreferences(MYPREFS, 0);
					Editor ed = settings.edit();
					ed.putString(PREF_URL, el.siteUrl);
					ed.putString(MY_WEBPAGE, el.siteUrl);
					ed.commit();
					
					// Create intent and Go to Read activity
					Intent intent = new Intent(getContext(), WebActivity.class);
					startActivity(intent);
				}
			});

			return newView;
		}		
	}
	
	private MyAdapter aa;
	
	public void parseJson(String json) {
		Gson gson = new Gson();
		MySites dlSitesObj = null;
		try {
			dlSitesObj = gson.fromJson(json, MySites.class);
		} catch (JsonSyntaxException ex) {
			Log.e(LOG_TAG, ex.toString());
		}
		
		if (dlSitesObj != null) {
			// add sites to Array Adapter
			for (int i = 0; i < dlSitesObj.sites.length; ++i) {
				enterItem(dlSitesObj.sites[i].url, dlSitesObj.sites[i].title);
			}
		}
	}
	
	public void enterItem(String url, String title) {
		Log.d(LOG_TAG, "site: "+ title +" Added");
		ListElement el = new ListElement();
		el.textLabel = title;
		el.buttonLabel = "Read";
		el.siteUrl = url;
		aList.add(el);
		Log.d(LOG_TAG, "The length of the list now is " + aList.size());
		aa.notifyDataSetChanged();
	}
	
	class MySites {
		MySites(){};
		public Site[] sites;
	}

	class Site {
		Site(){};
		public String url;
		public String title;
	}
}
