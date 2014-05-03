package com.example.homework2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class WebActivity extends Activity {

	static final public String WEBPAGE_NOTHING = "about:blank";
	static final public String MY_WEBPAGE = "http://users.soe.ucsc.edu/~luca/android.html";
	static final public String LOG_TAG = "WebActivity";
	
	WebView myWebView;
	
	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	// grab the original (non-redirected/mobile) url from prefs
	    	SharedPreferences settings = getSharedPreferences(MainActivity.MYPREFS, 0);
	    	String currentUrl = settings.getString(MainActivity.PREF_URL, "");
	    	
	    	String[] nextDomain = url.split("\\.");
	    	String[] currDomain = currentUrl.split("\\.");
	    	// check whether the url to be loaded is in original domain of webview
	        if (nextDomain[1].equals(currDomain[1])) {
	            // This is my web site, so do not override; let my WebView load the page
	            return false;
	        }
	        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
	        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	        startActivity(intent);
	        return true;
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		myWebView = (WebView) findViewById(R.id.webView1);
		myWebView.setWebViewClient(new MyWebViewClient());
		// grab news site url to load from shared prefs
		SharedPreferences settings = getSharedPreferences(MainActivity.MYPREFS, 0);
		String url = settings.getString(MainActivity.PREF_URL, "");
		myWebView.loadUrl(url);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web, menu);
		return true;
	}


}