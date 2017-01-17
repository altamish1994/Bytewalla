package se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.discovery;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPIBinder;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundleSpec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNEndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BluetoothBundle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleDaemon;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.MultiHopStorage;

/**
 * Created by altamisharif on 02/03/2016.
 */
public class AcceptThread extends AsyncTask<Void,String,String> {
    private final BluetoothServerSocket mmServerSocket;
    ArrayAdapter<String> adapter;
    ArrayList<String> data;
    private InputStream mmInStream=null;
    private OutputStream mmOutStream=null;
    Context cont ;

    public static BluetoothAdapter mBluetoothAdapter;
    public static ArrayList<BluetoothBundle> bundles = new ArrayList<>();
    public AcceptThread( Context c ,BluetoothAdapter bluetoothAdapter, ArrayAdapter<String> adapter, ArrayList<String> data) {
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
            mBluetoothAdapter = bluetoothAdapter;
            tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(c.getPackageName(), uri);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
                Thread.sleep(500);

            } catch (Exception e) {
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
                    String msg="kuch b";
                    Object obj = null;
                    byte[] buffer = new byte[1024];  // buffer store for the stream
                    int bytes=1024; // bytes returned from read()

                    // Keep listening to the InputStream until an exception occurs
                    //String msg = "";
                    while (true) {
                        try {
                            // Read from the InputStream
                            if(mmInStream.available() > 0) {
                                bytes = mmInStream.read(buffer);
                                // Send the obtained bytes to the UI activity
                                msg = new String(buffer);

                                //data.clear();
                                // return msg;
                            }else{break;}
                        } catch (IOException e) {
                            Log.e("ReadError", e.getMessage());
                            break;
                        }

                    }
                    Log.d("MSG",msg);
                    byte[] buffer2 = new byte[bytes];  // buffer store for the stream
                    for(int i=0 ; i<bytes;i++)

                    {
                        buffer2[i] = buffer[i];
                    }
                    ByteArrayInputStream in = new ByteArrayInputStream(buffer2);
                    ObjectInputStream is = null;
                    try {
                        is = new ObjectInputStream(in);
                        obj= is.readObject();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    BluetoothBundle bundle=(BluetoothBundle) obj;
                    bundles.add(bundle);
                    if(bundle.getPayload() == null){
                        bundle.setPayload("ACK");
                    }
                    Log.i("BUNDLE RECV", ((BluetoothBundle) obj).getPayload());
                   publishProgress(bundle.getPayload());
                    if(bundle.isAck()){

                        // SEN 3 : RECV
                        MultiHopStorage.getInstance().add(bundle.getBundle_id(),bundle.getSource());

                        // SEN 4 : RECV
                        if(bundle.isFinal())
                        {
                            MultiHopStorage.getInstance().del(bundle.getBundle_id());
                            publishProgress("SEN 4");

                        }
                        else publishProgress("SEN 3");

                    }
                   else if(bundle.getDest().toString().contains(BundleDaemon.getInstance().local_eid().toString()))
                    {
                        // SEN 6 : RECV
                        publishProgress("mere liye hai : SEN 6");

//                        MultiHopStorage.getInstance().add(bundle.getBundle_id(),bundle.getSource(),bundle.getDest(),
//                                bundle.getPayload());
                        sendACK(bundle.getSourceBluetooth(),bundle.getBundle_id(),true);
                        //Add in forwards table
                        MultiHopStorage.getInstance().add(bundle.getBundle_id(), bundle.getSource());
                        //Add in message table
                        MultiHopStorage.getInstance().add_messages(bundle.getSource()+"/",bundle.getPayload());
                    }
                    else
                    {
                        //publishProgress("mere liye ni hai");
                        //Exists in bundle table
                        if( MultiHopStorage.getInstance().impt_sqlite_().get_records("Forwards", "bundle_id = "+bundle.getBundle_id(), "id").size() >0
                             && MultiHopStorage.getInstance().impt_sqlite_().get_records("bundle", "bundle_id = " + bundle.getBundle_id(), "id").size() > 0) {
                                // SEN 1 : RECV
                                // DISCARD
                                sendACK(bundle.getSourceBluetooth(),bundle.getBundle_id(),false);
                                publishProgress("SEN 1");


                        }
                        else if(MultiHopStorage.getInstance().impt_sqlite_().get_records("Forwards", "bundle_id = "+bundle.getBundle_id(), "id").size() >0
                                && MultiHopStorage.getInstance().impt_sqlite_().get_records("bundle", "bundle_id = " + bundle.getBundle_id(), "id").size() ==0){
                            // SEN 2 : RECV
                            sendACK(bundle.getSourceBluetooth(),bundle.getBundle_id(), true);
                            publishProgress("SEN 2");

                        }
                        else if(MultiHopStorage.getInstance().impt_sqlite_().get_records("Forwards", "bundle_id = "+bundle.getBundle_id(), "id").size() ==0
                                && MultiHopStorage.getInstance().impt_sqlite_().get_records("bundle", "bundle_id = " + bundle.getBundle_id(), "id").size() ==0) {

                            // SEN : 5 RECV
                            // ADDING TO PENDING BUNDLES
                            MultiHopStorage.getInstance().add(bundle.getBundle_id(), bundle.getSource(), bundle.getDest(),
                                    bundle.getPayload());

                            // ADDING TO FRWRD TABLE
                            MultiHopStorage.getInstance().add(bundle.getBundle_id(),bundle.getSource());
                            sendACK(bundle.getSourceBluetooth(), bundle.getBundle_id(),false);
                            publishProgress("SEN 5");

                        }


                    }
                 //   if (msg != null)

                    socket.close();

                    //}
//                            try {
//                                Thread.sleep(3000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
                } catch (Exception e) {
                    Log.e("Error",e.getMessage());

                }

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
    public Object read()
    {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes=1024; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        String msg = "";
       while (true) {
            try {
                // Read from the InputStream
                if(mmInStream.available() > 0) {
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    msg = new String(buffer);

                    //data.clear();
                    // return msg;
                }else{break;}
            } catch (IOException e) {
                Log.e("ReadError", e.getMessage());
                break;
            }

        }
        Log.d("MSG",msg);
        byte[] buffer2 = new byte[bytes];  // buffer store for the stream
        for(int i=0 ; i<bytes;i++)

        {
            buffer2[i] = buffer[i];
        }
        ByteArrayInputStream in = new ByteArrayInputStream(buffer2);
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(in);
            return is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
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
        Toast.makeText(cont,params[0],Toast.LENGTH_LONG).show();
//        data.add(params[0]);
//        adapter.notifyDataSetChanged();
    }
    void sendACK(String destAdr, int bundleID,boolean isFinal)
    {
        DTNBundleSpec spec = new DTNBundleSpec();
        spec.setSec_bundle_id(bundleID);



        // set destination from the user input
        spec.set_dest(new DTNEndpointID(destAdr));

        // set the source EID from the bundle Daemon
        spec.set_source(new DTNEndpointID(BundleDaemon.getInstance().local_eid().toString()));

        // Set expiration in seconds, default to 1 hour

        BluetoothDevice d= mBluetoothAdapter.getRemoteDevice(destAdr);
        DTNAPIBinder.send_via_bluetooth(d, spec,null,isFinal);
    }
}