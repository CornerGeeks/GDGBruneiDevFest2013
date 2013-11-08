package org.thewheatfield.gdgbruneidevfest2013;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class LocalData {
	private static final String ID = "org.thewheatfield.gdgbruneidevfest2013.saveddata";
	private static final String data_names = "names";
	private SharedPreferences data;
	public LocalData(Context c)
	{
		data = c.getSharedPreferences( LocalData.ID, Context.MODE_PRIVATE);
	}
	
	private String encode(String data){
		return data.replace("|", "\\|");
	}
	private String decode(String data){
		return data.replace("\\|", "|");
	}
	public boolean deleteAll(){
		return saveNames(new String[]{});
	}
	public boolean deleteName(int index){
		String[] names = readNames();
		List<String> dataToSave = new ArrayList<String>();
		for(int i = 0; names != null && i < names.length; i++){
			if(i == index) continue;
			dataToSave.add(encode(names[i]));
		}
		return saveNames(dataToSave);
	}
	public boolean addName(String name){
		List<String> dataToSave = new ArrayList<String>();
		String[] names = readNames();
		dataToSave.add(name);
		for(int i = 0; names != null && i < names.length; i++){
			dataToSave.add(encode(names[i]));
		}
		return saveNames(dataToSave);
	}
	public boolean saveNames(List<String> names){
		return saveNames((String[]) names.toArray(new String[names.size()]));
	}
	public boolean saveNames(String[] names){
		SharedPreferences.Editor editor = data.edit();
		String dataToSave = "";
		for(int i = 0; i < names.length; i++){			
			dataToSave += ((i > 0) ? "||" : "") + encode(names[i]);
		}
		editor.putString(data_names, dataToSave);
		return editor.commit();		
	}
	public String[] readNames(){
		String allnames = data.getString(data_names, "");
		if(allnames == null || allnames.trim().equals("")) return null;
		String[] names = allnames.split("\\|\\|");
		List<String> namesList = new ArrayList<String>();
		for(int i = 0; i < names.length; i++){
			namesList.add(decode(names[i]));
		}
		return (String[]) namesList.toArray(new String[namesList.size()]);
	}
}
