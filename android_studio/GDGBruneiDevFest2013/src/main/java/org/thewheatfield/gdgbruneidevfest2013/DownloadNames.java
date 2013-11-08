package org.thewheatfield.gdgbruneidevfest2013;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;
import android.util.Log;

// file:///Applications/dev/android-sdk-macosx/docs/training/basics/network-ops/connecting.html
public class DownloadNames extends AsyncTask<String, Integer, String> {
	LocalData database;
    MainActivity.PlaceholderFragment activity;
	
	public DownloadNames(LocalData database, MainActivity.PlaceholderFragment activity){
		this.database = database;
		this.activity = activity;
	}
	
	@Override
    protected String doInBackground(String... urls) {
        // params comes from the execute() call: params[0] is the url.
        try {
        	Log.d("org", "url:" + urls[0]);
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            return "Unable to retrieve web page. URL may be invalid." + e.getMessage();
        }
    }
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		Log.d("org", ""+values);
	}

	@Override
    protected void onPostExecute(String result) {
		try {
			List<String> names  = processNames(result);
	        this.database.saveNames(names);
			Log.d("org", "Got names: "+ names.size());
			this.activity.postDownload();
		} catch (JSONException e) {
			Log.d("org", "Exception: " + e.getMessage() + "|" + e.getStackTrace());	        
		}
		
	}
	public static List<String> processNames(String data) throws JSONException{
		List<String> names = new ArrayList<String>();
		Log.d("org", "downloaded data:" +  data);
		JSONArray items = (JSONArray) new JSONTokener(data).nextValue();
		for(int i = 0; i < items.length(); i++){
			JSONObject item = (JSONObject) items.get(i);
			String name = item.getString("name");
			names.add(name);
		}
		return names;
	}
	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	public static String downloadUrl(String myurl) throws IOException {
	    InputStream is = null;
	    // Only display the first 500 characters of the retrieved
	    // web page content.
	    int len = 500;
	        
	    try {
	        URL url = new URL(myurl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        // Starts the query
	        conn.connect();
	        int response = conn.getResponseCode();
	        is = conn.getInputStream();

	        // Convert the InputStream into a string
	        String contentAsString = readIt(is, len);
	        return contentAsString;
	        
	    // Makes sure that the InputStream is closed after the app is
	    // finished using it.
	    } finally {
	        if (is != null) {
	            is.close();
	        } 
	    }
	}
	// Reads an InputStream and converts it to a String.
	public static String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
	    Reader reader = null;
	    reader = new InputStreamReader(stream, "UTF-8");        
	    char[] buffer = new char[len];
	    reader.read(buffer);
	    return new String(buffer);
	}	
}