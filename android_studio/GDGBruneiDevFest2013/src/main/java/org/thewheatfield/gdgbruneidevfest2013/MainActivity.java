package org.thewheatfield.gdgbruneidevfest2013;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

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








    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        TextView lblName;
        TextView iptName;
        Button btnChangeName;
        Button btnDeleteNames;
        ListView listNames;
        LocalData data;
        String[] names;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            setHasOptionsMenu(true);

            interactWithUserInput(rootView);
            saveNamesToList(rootView);
            addContextMenuToListItems();

            return rootView;
        }

        // take user interaction and update the screen
        private void interactWithUserInput(View rootView){
            this.lblName = (TextView) rootView.findViewById(R.id.lblName);
            this.iptName = (TextView) rootView.findViewById(R.id.iptName);
            this.btnChangeName = (Button) rootView.findViewById(R.id.btnChangeName);
            if(btnChangeName != null){
                btnChangeName.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(lblName != null && iptName != null){
                            String newName = iptName.getText().toString();
                            lblName.setText(getString(R.string.hello_world) + " " + newName);
                            if(data != null) {
                                if(saveName(newName)){
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
        private void saveNamesToList(View rootView){
            this.listNames = (ListView) rootView.findViewById(R.id.listNames);
            this.btnDeleteNames = (Button) rootView.findViewById(R.id.btnDeleteNames);

            if(btnDeleteNames != null){
                btnDeleteNames.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(data != null) {
                            if(deleteAll()){
                                loadData();
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
        private void initAndLoadData(){
            this.data = new LocalData(getActivity().getApplicationContext());
            loadData();
        }
        private void loadData(){
            this.names = this.data.readNames();
            loadNames(this.names);
        }

        private void loadNames(String[] names){
            ArrayAdapter<String> adapter = null;
            if(names != null){
                adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, names);
            }
            else{
            }
            listNames.setAdapter(adapter);

        }

        private void showMessage(String str){
            Toast.makeText(getActivity().getApplicationContext(), str, Toast.LENGTH_LONG).show();
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
            MenuInflater inflater = getActivity().getMenuInflater();
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
            shareIntent.setType("ascii/text");
            startActivity(Intent.createChooser(shareIntent, getResources() .getText(R.string.share)));
        }

        @Override
        public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.fragment_main, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }
//
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch(item.getItemId()){
                case R.id.action_download_names:
                    downloadNames();
                    break;
                case R.id.action_download_names_bad:
                    downloadNamesBad();
                    break;
            }
            return super.onOptionsItemSelected(item);
        }

        // String url = "http://localhost/modules/presentation/reveal.js-master/names.json";
        //String url = "http://10.0.2.2/modules/presentation/reveal.js-master/names.json";
        String url = "http://thewheatfield.org/gdg/names.json";

        public void downloadNames(){
            (new DownloadNames(this.data, this)).execute(url); //
        }
        public void downloadNamesBad(){
            try {
                String result = DownloadNames.downloadUrl(url);
                List<String> names = DownloadNames.processNames(result);
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

    }

}
