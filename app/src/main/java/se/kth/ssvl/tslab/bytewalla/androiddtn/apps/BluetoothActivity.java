package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import se.kth.ssvl.tslab.bytewalla.androiddtn.R;


public class BluetoothActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 666;
    public static ArrayList<String> discoverableDevicesList = new ArrayList<>();
    public static ArrayList<BluetoothDevice> devices = new ArrayList<>();
    BroadcastReceiver mReceiver;
    ListView lv;
    ArrayAdapter<String> adpt;
    BluetoothAdapter mBluetoothAdapter;
    EditText msgBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.listView);

        adpt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, discoverableDevicesList);
        msgBox = (EditText) findViewById(R.id.msg);

        lv.setAdapter(adpt);
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
                        discoverableDevicesList.add(device.getName());
                    adpt.notifyDataSetChanged();

                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    Log.v(" ", "discovery Finished ");
                    if(discoverableDevicesList.size() != 0)
                    {
                        //deviceList.invalidateViews();
                        adpt.notifyDataSetChanged();
                    }
                    else
                    {
                        Toast.makeText(BluetoothActivity.this, "No New Devices Found", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
       // filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy



      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
       else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Intent discoverableIntent = new
        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 55555555);
        startActivity(discoverableIntent);




        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
           //    (new SendMessage(devices.get(position), msgBox.getText().toString())).execute();
            }
        });



        ((Button) findViewById(R.id.discover)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices

                discoverableDevicesList.clear();
                mBluetoothAdapter.startDiscovery();
                if (pairedDevices.size() > 0) {

                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        devices.add(device);
                        discoverableDevicesList.add(device.getName() );
                    }
                    Toast.makeText(getApplicationContext(), pairedDevices.size() + " Device(s) Found", Toast.LENGTH_LONG).show();
                }
                adpt.notifyDataSetChanged();

            }
        });
        ((Button) findViewById(R.id.listen)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new AcceptThread(getApplicationContext(),adpt, discoverableDevicesList)).execute();
            }
        });

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {


        }
        else if(id==R.id.accept) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class AcceptThread extends AsyncTask<Void,String,String> {
        private final BluetoothServerSocket mmServerSocket;
        ArrayAdapter<String> adapter;
        ArrayList<String> data;
        private  InputStream mmInStream=null;
        private  OutputStream mmOutStream=null;
        Context cont ;
        public AcceptThread( Context c , ArrayAdapter<String> adapter, ArrayList<String> data) {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            this.data = data;
            this.adapter=adapter;
            cont = c;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                UUID uri=null;
                long uid = 7777;

                    uri = new UUID(uid,uid);

                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(c.getPackageName(), uri);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();


                } catch (IOException e) {
                    Log.e("Accept",e.getMessage());
                    break;

                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    //manageConnectedSocket(socket);
                    try {
                        mmInStream = socket.getInputStream();
                        mmOutStream = socket.getOutputStream();
//                        while (true) {
//                            if (mmInStream.available() > 0) {
                                String msg = read();
                                if (msg != null)
                                    publishProgress(msg);
socket.close();

                            //}
//                            try {
//                                Thread.sleep(3000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
                    } catch (IOException e) { }


//                    try {
//                        mmServerSocket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                //cancel();
                   // break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }
        public String read()
        {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            String msg = null;
            while (true) {
                try {
                    // Read from the InputStream
                    if(mmInStream.available() > 0) {
                        bytes = mmInStream.read(buffer);
                        // Send the obtained bytes to the UI activity
                        msg = new String(buffer);
                        //data.clear();
                       return msg;
                    }
                } catch (IOException e) {
                    Log.e("ReadError",e.getMessage());
                    break;
                }
            }
            return null;
        }

        @Override
        protected String doInBackground(Void... params) {
            run();
            return null;
        }
        @Override
        protected void onProgressUpdate(String... params) {
            super.onProgressUpdate(params);
            data.add(params[0]);
            adapter.notifyDataSetChanged();
        }
    }

}
