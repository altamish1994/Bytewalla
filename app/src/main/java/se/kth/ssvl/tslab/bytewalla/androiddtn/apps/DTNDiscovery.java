package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.ContactManager;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.Link;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.LinkSet;

/**
 * Created by Ahmad on 01-Nov-15.
 */
public class DTNDiscovery extends Activity{

    ListView listView ;
    public static ArrayList<String> Eid_list = new ArrayList<>();
    public static ArrayList<String> Ip_list = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dtnapps_dtndiscovery);
        initt();
    }

    /**
     * Initialiazing function for DTNApps. Bind the object to runtime button, add event listeners
     */
    public static ArrayList<String> getNodes(){
        if(Eid_list==null) {
            Eid_list=new ArrayList<>();
            Ip_list = new ArrayList<>();
        }
        else Eid_list.clear();
        LinkSet EntriesList = ContactManager.getInstance().links();

        Iterator<Link> i = EntriesList.iterator();
        Link.set_link_counter(0);

        while (i.hasNext()) {

            Link element = i.next();
            String dest_ip = element.dest_ip().toString();
        //    Ip_list.add(dest_ip);
            String Eid = element.remote_eid().toString();
            if(!Eid_list.contains(Eid+"/"))
            Eid_list.add(Eid+"/");

        }
        return  Eid_list;
    }
    private void initt()
    { // Get ListView object from xml
        listView = (ListView) findViewById(R.id.discoveryList);
        getNodes();

        ArrayList<String> Ip_list = new ArrayList<String>();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, Eid_list);




    // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        Eid_list.get(position), Toast.LENGTH_LONG)
                        .show();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                ClipData clip = ClipData.newPlainText("eid",Eid_list.get(position));
                clipboard.setPrimaryClip(clip);

            }

        });
    }
}
