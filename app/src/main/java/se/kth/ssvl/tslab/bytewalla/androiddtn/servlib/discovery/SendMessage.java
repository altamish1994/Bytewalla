package se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.discovery;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by altamisharif on 02/03/2016.
 */
public class SendMessage extends AsyncTask<Void,Void,Void> {
    private BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private InputStream mmInStream=null;
    private OutputStream mmOutStream=null;
    String msg ;
    se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BluetoothBundle bundle;
    public SendMessage(BluetoothDevice device, se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BluetoothBundle b) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        //this.msg = msg;
        bundle = b;
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            UUID uri=null;
            long uid = 7777;

            uri = new UUID(uid,uid);
          //  tmp = device.createRfcommSocketToServiceRecord(uri);
            tmp = device.createInsecureRfcommSocketToServiceRecord(uri);
        } catch (IOException e) { }

        mmSocket = tmp;


    }

    synchronized public void run() {
        // Cancel discovery because it will slow down the connection
        //mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
            try {
                mmInStream = mmSocket.getInputStream();
                mmOutStream = mmSocket.getOutputStream();
            } catch (IOException e) { }
          //  StringBuffer buff= new StringBuffer();

            byte [] bytes = serialize(bundle);
            write(bytes);

            //cancel();
        }
        catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        //manageConnectedSocket(mmSocket);
    }


    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
    public void read()
    {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        String msg = null;
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
                msg= new String (buffer);
            } catch (IOException e) {
                break;
            }
        }
    }
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    @Override
    protected Void doInBackground(Void... params) {
        run();
        return null;
    }
}