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

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	TextView lblName;
	TextView iptName;
	Button btnChangeName;
	Button btnDeleteNames;
	ListView listNames;
	LocalData data;
	String[] names;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		interactWithUserInput();
		saveNamesToList();
		addContextMenuToListItems();
		
		// show intent
		// internationalization
		// custom adapter
		// network call
		// new activity
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	
	// take user interaction and update the screen
	private void interactWithUserInput(){
		this.lblName = (TextView) findViewById(R.id.lblName);
		this.iptName = (TextView) findViewById(R.id.iptName);
		this.btnChangeName = (Button) findViewById(R.id.btnChangeName);
		if(btnChangeName != null){
			btnChangeName.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(lblName != null && iptName != null){
						String newName = iptName.getText().toString();
						lblName.setText(getString(R.string.hello_world) + " " + newName);
						if(data != null) {
							if(saveName(newName)){
								showMessage(getString(R.string.name_added));
								loadData();							
							}
							else
								showMessage(getString(R.string.name_added_error));
						}
						iptName.setText("");
					}
					else{
						lblName.setText("-");
					}					
				}
			});
		}
	}
	
	// saving data
	private void saveNamesToList(){
		this.listNames = (ListView) findViewById(R.id.listNames);
		this.btnDeleteNames = (Button) findViewById(R.id.btnDeleteNames);
		
		if(btnDeleteNames != null){
			btnDeleteNames.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(data != null) {
						if(deleteAll()){
							loadData();
							showMessage(getString(R.string.names_deleted));
						}
						else
							showMessage(getString(R.string.names_deleted_error));
							
					}
				}
			});
		}
		if(listNames != null){
			listNames.setOnLongClickListener(new OnLongClickListener() {				
				@Override
				public boolean onLongClick(View v) {
					
					return false;
				}
			});
		}		
		initAndLoadData();
	}
	
	// should only be called once
	private void initAndLoadData(){
		this.data = new LocalData(getApplicationContext());
		loadData();
	}
	private void loadData(){
		this.names = this.data.readNames();
		loadNames(this.names);
	}

	// load names into the list
	private void loadNames(String[] names){
		ArrayAdapter<String> adapter = null;
		if(names != null){
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
		}
		else{
		}
		listNames.setAdapter(adapter);
		
	}	

	// show the toasty message
	private void showMessage(String str){
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
	}

	
	private boolean saveName(String name){
		return data.addName(name);
	}
	private boolean deleteName(int index){		
		return data.deleteName(index);
	}
	private boolean deleteAll(){
		return data.deleteAll();
	}
	
	
	// add context menu to list items
	// file:///Applications/dev/android-sdk-macosx/docs/guide/topics/ui/menus.html#FloatingContextMenu
	private void addContextMenuToListItems(){
		if(listNames != null)
			registerForContextMenu(listNames);
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.deleteName:
	        	handleDeleteName(info.position);
	            return true;
	        case R.id.shareName:
	        	shareName(this.names[info.position]);
	        	return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	private void handleDeleteName(int position){
        if(deleteName(position)){
        	showMessage(getString(R.string.names_deleted));
        	loadData();
        }
    	else
        	showMessage(getString(R.string.names_deleted_error));
	}
	private void shareName(String name){
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, name);
		shareIntent.setType("text/plain");
		startActivity(Intent.createChooser(shareIntent, getResources() .getText(R.string.share)));		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_download_names:
			downloadNames();
			break;
		case R.id.action_download_names_bad:
			downloadNamesBad();
			break;
		case R.id.action_load_custom_list:
			loadNamesCustomList(names);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	// String url = "http://10.0.2.2/modules/presentation/reveal.js-master/names.json"; // testing in emulator. download mongoose (https://code.google.com/p/mongoose/) or use any web server application
	String url = "http://thewheatfield.org/gdg/names.json";
	
	public void downloadNames(){
		(new DownloadNames()).execute(url); //
	}
	public void downloadNamesBad(){
		try {
			String result = DownloadNamesTask.downloadUrl(url);
			List<String> names = DownloadNamesTask.processNames(result);
	        data.saveNames(names);
			postDownload();
		} catch (Exception e) {
			if(e.getClass().equals(NetworkOnMainThreadException.class))
				showMessage("NetworkOnMainThreadException thrown");
			else
				showMessage("Error: " + e.getMessage());
		}
	}
	public void postDownload(){
		loadData();
	}

	// http://developer.android.com/training/basics/network-ops/connecting.html#AsyncTask
	private class DownloadNames extends AsyncTask<String, Integer, String> {
		
		public DownloadNames(){
		}
		
		@Override
	    protected String doInBackground(String... urls) {
	        try {
	            return downloadUrl(urls[0]);
	        } catch (IOException e) {
	            return null;
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
		        data.saveNames(names);
				postDownload();
			} catch (JSONException e) {
			}
			
		}
		public List<String> processNames(String data) throws JSONException{
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
		public String downloadUrl(String myurl) throws IOException {
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
		public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
		    Reader reader = null;
		    reader = new InputStreamReader(stream, "UTF-8");        
		    char[] buffer = new char[len];
		    reader.read(buffer);
		    return new String(buffer);
		}	
	}
	
	
	// custom layout	
	private void loadNamesCustomList(String[] names){
		ArrayAdapter<String> adapter = null;
		if(names != null){
		//	adapter = new ArrayAdapter<String>(this, R.layout.list_item, names);
		}
		else{
		}
		listNames.setAdapter(adapter);
	}	
	
}
