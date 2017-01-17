package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNManager;
import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.MultiHopStorage;

/**
 * Created by Ahsan on 01-Nov-15.
 */
public class DTNMessage extends Activity{

    ListView listView ;
    /**
     * AddContactButton reference object
     */

    final public static ArrayList<String> Eid_list = new ArrayList<String>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dtnapps_dtnmessage);
        Toast.makeText(this,"Yo "+ MultiHopStorage.getInstance().getTableSize(getApplicationContext()),Toast.LENGTH_LONG).show();
        Toast.makeText(this,"Yoyo "+ MultiHopStorage.getInstance().getForwardsTableSize(getApplicationContext()),Toast.LENGTH_LONG).show();
        Log.v("TAG","Yothisis "+ MultiHopStorage.getInstance().getTableSize(getApplicationContext()) );
       if (!getIntent().getBooleanExtra("debug",false))
        initt();
        else init_debug();
    }

    /**
     * Initialiazing function for DTNApps. Bind the object to runtime button, add event listeners
     */

    private void initt()
    { // Get ListView object from xml
        listView = (ListView) findViewById(R.id.messageList);

        final ArrayList<String> message_list = new ArrayList<String>();
        final ArrayList<String> dummy_list = new ArrayList<String>();
        message_list.clear();
//        for(BluetoothBundle bundle :AcceptThread.bundles)
//        {
//            message_list.add(bundle.getPayload());
//        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, message_list);
        Iterator<String > i = MultiHopStorage.getInstance().get_messages_contacts();
        while (i.hasNext()) {
            String t_name=i.next();
            boolean con=true;
            for(int a=0;a< DTNManager.contactEid.size();a++){
                if(t_name.equals(DTNManager.contactEid.get(a))){
                    message_list.add(DTNManager.contactName.get(a));
                    con=false;
                }
            }
            if(con){
                message_list.add(t_name);
            }
            dummy_list.add(t_name);
            // here it is adding in forwards table
        }

//         message_list.add("------------------");
//          i = MultiHopStorage.getInstance().get_forwards();
//         while (i.hasNext()) {
//
//
//           message_list.add(i.next());
//         //here it is adding in forwards table
//        }

//        message_list.add("Altamish");
//        message_list.add("Ahsan");
//        message_list.add("Ahmed");

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Intent i = new Intent(DTNMessage.this, DTNMessageView.class);
                i.putExtra("Name",dummy_list.get(position));
                startActivity(i);
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//
//                ClipData clip = ClipData.newPlainText("eid",Eid_list.get(position));
//                clipboard.setPrimaryClip(clip);

            }

        });
    }
    void init_debug(){
        listView = (ListView) findViewById(R.id.messageList);

        final ArrayList<String> message_list = new ArrayList<String>();
        final ArrayList<String> dummy_list = new ArrayList<String>();
        message_list.clear();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, message_list);
        Iterator<String > i = MultiHopStorage.getInstance().get_pendings();
        while (i.hasNext()) {


            message_list.add(i.next());
            //here it is adding in forwards table
        }

         message_list.add("------------------");
        i = MultiHopStorage.getInstance().get_forwards();
         while (i.hasNext()) {


           message_list.add(i.next());
         //here it is adding in forwards table
        }
        listView.setAdapter(adapter);

    }
}
