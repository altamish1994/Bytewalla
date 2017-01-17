package se.kth.ssvl.tslab.bytewalla.androiddtn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPIBinder;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundleID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundlePayload;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundleSpec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNEndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNHandle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.apps.DTNAPIFailException;
import se.kth.ssvl.tslab.bytewalla.androiddtn.apps.DTNOpenFailException;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.List;

/**
 * Created by altamisharif on 17/12/2015.
 */
public class MultiHopForwarding {
    private DTNAPIBinder dtn_api_binder_;
    private ServiceConnection conn_;
    //  Default DTN send parameters
    /**
     * Default expiration time in seconds, set to 1 hour
     */
    private static final int EXPIRATION_TIME = 1*60*60;

    /**
     * Set delivery options to don't flag at all
     */
    private static final int DELIVERY_OPTIONS = 0;

    /**
     * Set priority to normal sending
     */
    private static final DTNAPICode.dtn_bundle_priority_t PRIORITY = DTNAPICode.dtn_bundle_priority_t.COS_NORMAL;

    Context c;
    private static String TAG = "MULTIHOP";

    public MultiHopForwarding(Context _c)
    {
     this.c=_c;
        bindDTNService();
    }

   public static List<Bundle>  pendings = new List<>();
    private void bindDTNService()
    {
        conn_ = new ServiceConnection() {

            public void onServiceConnected(ComponentName arg0, IBinder ibinder) {
                Log.i(TAG, "DTN Service is bound");
                dtn_api_binder_ = (DTNAPIBinder) ibinder;

		try {

            for(int i=0;i<pendings.size();i++)
            sendMessage(pendings.get(i).getString("source"),pendings.get(i).getString("dest"),pendings.get(i).getString("act"), pendings.get(i).getString("msg"));



            pendings.clear();
            unbindDTNService();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (DTNOpenFailException e) {
			e.printStackTrace();
		} catch (DTNAPIFailException e) {
			e.printStackTrace();
		}

            }



            public void onServiceDisconnected(ComponentName arg0) {
                Log.i(TAG, "DTN Service is Unbound");
                dtn_api_binder_ = null;
            }

        };

        Intent i = new Intent(c, DTNService.class);
        c.bindService(i, conn_, c.BIND_AUTO_CREATE);
    }
    public void sendMessage(String source, String Eid,String act, String data) throws UnsupportedEncodingException, DTNOpenFailException, DTNAPIFailException
    {
        // Getting values from user interface
        String message = data;
        byte[] message_byte_array = message.getBytes("US-ASCII");

        String dest_eid = Eid;
        //String dest_eid = "dtn://"
        //		+ ((WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress().replace(":", "")
        //		+ ".bytewalla.com";
        //Setting DTNBundlePayload according to the values
        DTNBundlePayload dtn_payload = new DTNBundlePayload(DTNAPICode.dtn_bundle_payload_location_t.DTN_PAYLOAD_MEM);
        dtn_payload.set_buf(message_byte_array);

        //	DTNBundlePayload dtn_payload = new DTNBundlePayload(dtn_bundle_payload_location_t.DTN_PAYLOAD_FILE);
        //dtn_payload.set_file(new File("/sdcard/test3MB.zip"));
        //	dtn_payload.set_file(new File("/sdcard/test.htm"));

        // Start the DTN Communication
        DTNHandle dtn_handle = new DTNHandle();
        DTNAPICode.dtn_api_status_report_code open_status = dtn_api_binder_.dtn_open(dtn_handle);
        if (open_status != DTNAPICode.dtn_api_status_report_code.DTN_SUCCESS) throw new DTNOpenFailException();
        try
        {
            DTNBundleSpec spec = new DTNBundleSpec();
           //

            spec.set_actual_des(new DTNEndpointID(act));

            // set destination from the user input
            spec.set_dest(new DTNEndpointID(dest_eid));


            // set the source EID from the bundle Daemon
            //spec.set_source(new DTNEndpointID(BundleDaemon.getInstance().local_eid().toString()));
            //spec.set_source(new DTNEndpointID());
            spec.set_source(new DTNEndpointID(source));

            // Set expiration in seconds, default to 1 hour
            spec.set_expiration(EXPIRATION_TIME);
            // no option processing for now
            spec.set_dopts(DELIVERY_OPTIONS);
            // Set prority
            spec.set_priority(PRIORITY);

            // Data structure to get result from the IBinder
            DTNBundleID dtn_bundle_id = new DTNBundleID();

            DTNAPICode.dtn_api_status_report_code api_send_result =  dtn_api_binder_.dtn_send(dtn_handle,
                    spec,
                    dtn_payload,
                    dtn_bundle_id);

            // If the API fail to execute throw the exception so user interface can catch and notify users
            if (api_send_result != DTNAPICode.dtn_api_status_report_code.DTN_SUCCESS)
            {
                throw new DTNAPIFailException();
            }

        }
        finally
        {
            dtn_api_binder_.dtn_close(dtn_handle);

        }



    }
    public void unbindDTNService()
    {
       c. unbindService(conn_);
    }
}
