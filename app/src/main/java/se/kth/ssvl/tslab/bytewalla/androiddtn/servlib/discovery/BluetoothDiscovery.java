package se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.discovery;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPIBinder;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundleSpec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNEndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.MultiHopStorage;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.TimeHelper;

/**
 * Created by altamisharif on 02/03/2016.
 */
public class BluetoothDiscovery {
    public static ArrayList<BluetoothDevice> devices = new ArrayList<>();
    static BroadcastReceiver mReceiver;
    public static  BluetoothAdapter mBluetoothAdapter;

    static Context cont;
    public static ArrayList<String> discoverableDevicesList = new ArrayList<>();
    public static void init(Context c){

        mReceiver  = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    //if( !PairedDeviceNames.contains(device) && !newDevices.contains(device))
                    if(!devices.contains(device)) {
                        discoverableDevicesList.add(device.getName());
                        devices.add(device);
                      //  Toast.makeText(context, device.getName() + " is UP", Toast.LENGTH_LONG).show();

                    }
                    sendPendingBundle(device);


                    //adpt.notifyDataSetChanged();

                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    Log.v(" ", "discovery Finished ");
                    if(discoverableDevicesList.size() != 0)
                    {
                        //deviceList.invalidateViews();
                      //  adpt.notifyDataSetChanged();
                    }
                    else
                    {
                      //  Toast.makeText(BluetoothActivity.this, "No New Devices Found", Toast.LENGTH_LONG).show();
                    }
                    //start discovery again
                  //  BluetoothAdapter.getDefaultAdapter().startDiscovery();
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        c. registerReceiver(mReceiver, filter);
         // Don't forget to unregister during onDestroy
       mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBluetoothAdapter == null) {
//            // Device does not support Bluetooth
//        }
//        else if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        c. startActivity(discoverableIntent);

        discover();


    }

    public static void clear()
    {
        devices.clear();
        discoverableDevicesList.clear();
    }
    public static void discover()
    {
        devices.clear();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices

        discoverableDevicesList.clear();
        mBluetoothAdapter.startDiscovery();
        if (pairedDevices.size() > 0) {

            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if(!devices.contains(device)){
                devices.add(device);
                discoverableDevicesList.add(device.getName());
                }
            }
           // Toast.makeText(getApplicationContext(), pairedDevices.size() + " Device(s) Found", Toast.LENGTH_LONG).show();
        }
    }
    private static void sendPendingBundle(BluetoothDevice link)
    {

        // SEN 2 : Sending

        try{
            //	Cursor cursor = MultiHopStorage.getInstance().impt_sqlite_().get_records("bundle", "destination = '" + link+"/"+"'");
            Cursor cursor = MultiHopStorage.getInstance().impt_sqlite_().get_records("bundle", null);



            String expiredBundlesId = "";
            int currentTime= (int) TimeHelper.current_seconds_from_ref();

            int bundle_id_col = cursor.getColumnIndex("bundle_id");
            int bundle_source_col = cursor.getColumnIndex("source");
            int bundle_destination_col = cursor.getColumnIndex("destination");
            int bundle_message_col = cursor.getColumnIndex("msg");
            //int bundle_life_col = cursor.getColumnIndex("life");


            if(cursor!=null){
                if (cursor.moveToFirst()){

                    do {

                        //Thread.sleep(2000);

                        String message = cursor.getString(bundle_message_col);
                        String dest = cursor.getString(bundle_destination_col);
                        String bundle_id = cursor.getString(bundle_id_col);
                        String bundle_source = cursor.getString(bundle_source_col);
                        DTNBundleSpec spec = new DTNBundleSpec();
                        spec.setSec_bundle_id(Integer.parseInt(bundle_id));
                        //


                        // set destination from the user input
                        spec.set_dest(new DTNEndpointID(dest));

                        // set the source EID from the bundle Daemon
                        spec.set_source(new DTNEndpointID(bundle_source));


                        //dtn_api_binder_.BSend(device, spec, message);

                        DTNAPIBinder.send_via_bluetooth(link, spec, message, false);
                    }while(cursor.moveToNext());
                }
            }
        }catch (Exception e)
        {
            Log.e("BlutoothError",e.getMessage());
        }
    }
}
