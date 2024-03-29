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
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class WebActivity extends Activity {

	static final public String WEBPAGE_NOTHING = "about:blank";
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
	    	if (nextDomain[0].equals(currDomain[0])) {
	    		// for urls of the form http://blah.com, where domain is first token
	    		return false;
	    	}
	        if (nextDomain[1].equals(currDomain[1])) {
	            // for urls of the form http://([www]|[m]).blah.com, where domain is second token
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
	
   @Override
   public void onPause() {
     	Method pause = null;
     	
     	try {
     		pause = WebView.class.getMethod("onPause");
     	} catch (SecurityException e) {
     		// Nothing
     	} catch (NoSuchMethodException e) {
     		// Nothing
     	}
    	if (pause != null) {
    		try {
    			pause.invoke(myWebView);
    		} catch (InvocationTargetException e) {
			} catch (IllegalAccessException e) {
			}
    	} else {
    		/// No such method.  Stores the current URL.
			String suspendUrl = myWebView.getUrl();
			SharedPreferences settings = getSharedPreferences(MainActivity.MYPREFS, 0);
			Editor ed = settings.edit();
			ed.putString(MainActivity.PREF_URL, suspendUrl);
			ed.commit();
			// And loads a URL without any processing.
			myWebView.loadUrl(WEBPAGE_NOTHING);
    	}
    	super.onPause();
   } 

   @Override
   public void onResume() {
       	super.onResume();

       	SharedPreferences settings = getSharedPreferences(MainActivity.MYPREFS, 0);
       	String suspendUrl = settings.getString(MainActivity.PREF_URL, "");
       	Method resume = null;
       	// Resumes the webview.
       	try {
       		resume = WebView.class.getMethod("onResume");
       	} catch (SecurityException e) {
       		// Nothing
       	} catch (NoSuchMethodException e) {
       		// Nothing
       	}
    	if (resume != null) {
    		try {
    			resume.invoke(myWebView);
    		} catch (InvocationTargetException e) {
			} catch (IllegalAccessException e) {
			}
    	} else {
    		// No such method.  Restores the suspended URL.
    		if (suspendUrl == null) {
    			// load original Json url
    			myWebView.loadUrl(MainActivity.MY_WEBPAGE);
    		} else {
    			myWebView.loadUrl(suspendUrl);
    		}
    	}
   }

	
	// handler for forward navigation button 
	public void clickForward(View v) {
		if (myWebView.canGoForward()) {
			myWebView.goForward();
		}
	}

	// handler for Backwards navigation button 
	public void clickBack(View v) {
		if (myWebView.canGoBack()) {
			myWebView.goBack();
		}
	}
	
	// handler for switching back to Main activity 
	public void backChoices(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
    // The back button should go back in page history, not in app history.
    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
        	// check for false positive due to redirects
        	SharedPreferences settings = getSharedPreferences(MainActivity.MYPREFS, 0);
        	// ogUrl == url dled from Json site in MainActivity
        	String ogUrl = settings.getString(MainActivity.MY_WEBPAGE, "");
        	String unRedirect = myWebView.getOriginalUrl();
        	if (ogUrl.equals(unRedirect)) {
        		super.onBackPressed();
        	} else {
        		myWebView.goBack();
        	}
        } else {
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        	super.onBackPressed();
        }
    }

}
