/*
 *	  This file is part of the Bytewalla Project
 *    More information can be found at "http://www.tslab.ssvl.kth.se/csd/projects/092106/".
 *    
 *    Copyright 2009 Telecommunication Systems Laboratory (TSLab), Royal Institute of Technology, Sweden.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 */
package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNManager;
import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNService;
import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPIBinder;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_api_status_report_code;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_bundle_payload_location_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_bundle_priority_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundleID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundlePayload;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundleSpec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNEndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNHandle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleDaemon;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.discovery.BluetoothDiscovery;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.naming.EndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.MultiHopStorage;

/**
 * The DTNSend Activity. This Activity allows user to send Text message from DTN
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 */
public class DTNSend extends Activity  {

	// string which have all discovered nodes
	 public static ArrayList<String> Eid_list = new ArrayList<String>();

	/**
	 * Logging TAG for supporting Android logging mechanism
	 */
	private static String TAG = "DTNSend";

	public static int SERVER_PORT = 5454;
	
	/**
	 * SendButton reference object
	 */
	private Button SendButton;
	
	/**
	 * CloseButton reference object
	 */
	private Button closeButton;
	
	/**
	 * Destination EndpointID reference object
	 */
	private EditText DestEIDEditText;
	
	/**
	 * Message Textview reference object
	 */
	private TextView MessageTextView;
	
	/**
	 * DTNAPIBinder object
	 */
	private DTNAPIBinder dtn_api_binder_;

	
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
	private static final dtn_bundle_priority_t PRIORITY = dtn_bundle_priority_t.COS_NORMAL;
	 
	/**
	 * The service connection to communicate with DTNService 
	 */
	private ServiceConnection conn_;

	private Spinner dtn_spinner;

	private Button AddContactButton;
	BluetoothDevice device;
	/**
	 * The onCreate function overridden from Android Activity. 
	 * This basically will associate the display view to dtnsend layout and initialize the class
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.dtnapps_dtnsend);

			init();
		}
	
	/**
	 * The onDestroy function override form Android Activity. This will free the DTNAPIBinder resource
	 * by unbinding the service
	 */
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unbindDTNService();
	}
		
	/**
	 * Internal Invalid EndpointIDException for this DTNSend Exception.
	 * This is used to pass exception and notify users about invalid input Destination EID
	 */
	private static class  InvalidEndpointIDException extends Exception
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 6431328157822474346L;
		
	}

//	private void discoveredListInit()
//	{
//		Eid_list.clear();
//		LinkSet EntriesList = ContactManager.getInstance().links();
//		Iterator<Link> i = EntriesList.iterator();
//		Link.set_link_counter(0);
//
//		while (i.hasNext()) {
//			Link element = i.next();
//			String Eid = element.remote_eid().toString();
//			Eid_list.add(Eid+"/");
//		}
//
//	}
	/**
	 * Initialization function for this class. This will init userinterface then bind the DTNService
	 */
	private void init()
	{
		DTNManager.contactName.clear();
		DTNManager.contactEid.clear();

		// TO BE CHANGED
		//DTNManager.contactName.add("Ahsan");


		//-------------
//		Iterator namesI = MultiHopStorage.getInstance().get_contacts_name();
//		while(namesI.hasNext()){
//			DTNManager.contactName.add(namesI.next().toString());
//		}
//
//		DTNManager.contactEid.add("dtn://020000000000.bytewalla.com/");
//		Iterator eidI = MultiHopStorage.getInstance().get_contacts_eid();
//		while(eidI.hasNext()){
//			DTNManager.contactEid.add(eidI.next().toString());
//		}

		DTNManager.contactName.addAll(BluetoothDiscovery.discoverableDevicesList);

		initUI();
//		MultiHopStorage.getInstance().add_messages("dtn://abc.bytewalla.com", "Yo");
//		MultiHopStorage.getInstance().add_messages("dtn://020000000000.bytewalla.com/", "Yo");
		bindDTNService();
		
//		if(BundleDaemon.getInstance().local_eid().str().equals("dtn://0023764f5945.bytewalla.com")){
//			DestEIDEditText.setText("dtn://0023764b6795.bytewalla.com/test");
//		}
//		else {
//			DestEIDEditText.setText("dtn://0023764f5945.bytewalla.com/test");
//		}
		//MessageTextView.setText(BundleDaemon.getInstance().local_eid().str());
	MessageTextView.setText("Hello ");

	}
	
	/**
	 * Initialization for the user interface by referencing with the actual runtime object and
	 * then set appropriate text or events to the runtime object
	 */
	private void initUI()
	{
		    DestEIDEditText = (EditText) this.findViewById(R.id.DTNApps_DTNSend_DestEIDEditText);
			MessageTextView = (TextView) this.findViewById(R.id.DTNApps_DTNSend_MessageTextView);
			SendButton = (Button)this.findViewById(R.id.DTNApps_DTNSend_SendButton);
		dtn_spinner = (Spinner)this.findViewById(R.id.DTNSpinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, DTNManager.contactName);
		AddContactButton = (Button) findViewById(R.id.add_contact_button);

		AddContactButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(DTNSend.this, AddContacts.class);
				startActivity(i);
			}
		});

		dtn_spinner.setAdapter(adapter);
		dtn_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				device = BluetoothDiscovery.devices.get(position);
				//DTNSend.this.DestEIDEditText.setText(DTNManager.contactEid.get(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
			SendButton.setOnClickListener(new OnClickListener() {
				
				
				public void onClick(View v) {
					try {
						// Validate the user input first whether the EID is valid EID
						checkInputEID();

						// If the validator pass, send the message
						sendMessage();

						new AlertDialog.Builder(DTNSend.this).setMessage(
								"Sent DTN message to DTN Service successfully ")
								.setPositiveButton("OK", null).show();

						
					} catch (InvalidEndpointIDException e) {
						new AlertDialog.Builder(DTNSend.this).setMessage(
								"Dest EID is invalid. Please input valid EID for example dtn://endpoint.com")
								.setPositiveButton("OK", null).show();
						
						
						
					} catch (Exception e) {
						new AlertDialog.Builder(DTNSend.this).setMessage(
						"Internal error with " + e.getMessage())
						.setPositiveButton("OK", null).show();
						
					}
					
					
				}
			});
		  	
			closeButton = (Button)this.findViewById(R.id.DTNApps_DTNSend_CloseButton);
			closeButton.setOnClickListener(new OnClickListener() {


				public void onClick(View v) {
					DTNSend.this.finish();

				}
			});
			
	}
	/**
	 * Unbind the DTNService to free resource consumed by the binding
	 */
	private void unbindDTNService()
	{
		
		unbindService(conn_);
	}
	
	/**
	 * bind the DTNService to use the API later
	 */
	private void bindDTNService()
	{
		
		
		conn_ = new ServiceConnection() {

			

			
			public void onServiceConnected(ComponentName arg0, IBinder ibinder) {
				Log.i(TAG, "DTN Service is bound");
				dtn_api_binder_ = (DTNAPIBinder) ibinder;
			}


			
			public void onServiceDisconnected(ComponentName arg0) {
				Log.i(TAG, "DTN Service is Unbound");
				dtn_api_binder_ = null;
			}

			};

			Intent i = new Intent(DTNSend.this, DTNService.class);
			bindService(i, conn_, BIND_AUTO_CREATE);	
	}
	
	/**
	 * major function for send message by calling dtnsend API
	 * @throws UnsupportedEncodingException
	 * @throws DTNOpenFailException
	 * @throws DTNAPIFailException
	 */

	private void sendMessage() throws UnsupportedEncodingException, DTNOpenFailException, DTNAPIFailException
	{
		// Getting values from user interface
		//discoveredListInit();
		String message = MessageTextView.getText().toString();
		String origMsg = message;

		byte[] message_byte_array = message.getBytes("US-ASCII");
        ArrayList<String> destinations = new ArrayList<>();
		String dest_eid = DestEIDEditText.getText().toString();
		Eid_list = DTNDiscovery.Eid_list;
		boolean isfoundinDiscovery=Eid_list.contains(dest_eid)|| (BundleDaemon.getInstance().local_eid().toString()+"/").equals(dest_eid);
		Random rn = new Random();

		int secretId =  rn.nextInt(10000) + 1;

        if(isfoundinDiscovery ) {
            // Destination node is in range, add it
            destinations.add(dest_eid);
        }
        else
        {
            //Destination node is not in range, Add all nodes to flood message



		//	if(!isfoundinDiscovery)

				MultiHopStorage.getInstance().add(secretId,BundleDaemon.getInstance().local_eid().toString(),dest_eid,message);
				message = message + "  ," + secretId;

				message_byte_array = message.getBytes("US-ASCII");
			//	dtn_payload.set_buf(message_byte_array);


            destinations.addAll(Eid_list);
			Log.i(TAG, String.valueOf(destinations.size()));
            isfoundinDiscovery=false;
        }
        // if it is multihop bundle also store it in db to send later discoverd nodes



        //Flooding to previously added Nodes

        for(int i= 0 ;i <destinations.size(); i++) {
            // Setting DTNBundle Payload according to the values
            DTNBundlePayload dtn_payload = new DTNBundlePayload(dtn_bundle_payload_location_t.DTN_PAYLOAD_MEM);
            dtn_payload.set_buf(message_byte_array);

            //	DTNBundlePayload dtn_payload = new DTNBundlePayload(dtn_bundle_payload_location_t.DTN_PAYLOAD_FILE);
            // dtn_payload.set_file(new File("/sdcard/test3MB.zip"));
            //	dtn_payload.set_file(new File("/sdcard/test.htm"));

            // Start the DTN Communication
            DTNHandle dtn_handle = new DTNHandle();
            dtn_api_status_report_code open_status = dtn_api_binder_.dtn_open(dtn_handle);
            if (open_status != dtn_api_status_report_code.DTN_SUCCESS)
                throw new DTNOpenFailException();
            try {
                DTNBundleSpec spec = new DTNBundleSpec();
				spec.setSec_bundle_id(secretId);
				//
				// set Actual destination from the user input
				spec.set_actual_des(new DTNEndpointID(dest_eid));


				// set destination from the user input
                spec.set_dest(new DTNEndpointID(destinations.get(i)));

                // set the source EID from the bundle Daemon
                spec.set_source(new DTNEndpointID(BundleDaemon.getInstance().local_eid().toString()));

                // Set expiration in seconds, default to 1 hour
                spec.set_expiration(EXPIRATION_TIME);
                // no option processing for now
                spec.set_dopts(DELIVERY_OPTIONS);
                // Set prority
                spec.set_priority(PRIORITY);

                // Data structure to get result from the IBinder
                DTNBundleID dtn_bundle_id = new DTNBundleID();


                // add bundle in forward table for later send help
               // if(destinations.size()>1 || !isfoundinDiscovery){
               // MultiHopStorage.getInstance().add(secretId,destinations.get(i)); isfoundinDiscovery=false;
               // }
				if(!isfoundinDiscovery)
				MultiHopStorage.getInstance().add(secretId,destinations.get(i));
				//isfoundinDiscovery=false;

                dtn_api_status_report_code api_send_result = dtn_api_binder_.dtn_send(dtn_handle,
                        spec,
                         dtn_payload,
                        dtn_bundle_id);

                // If the API fail to execute throw the exception so user interface can catch and notify users
                if (api_send_result != dtn_api_status_report_code.DTN_SUCCESS) {
                    throw new DTNAPIFailException();
                }



            } finally {
                dtn_api_binder_.dtn_close(dtn_handle);

            }
        }

		boolean bluetoothSend = false;

		for (int i = 0; i < BluetoothDiscovery.devices.size() && !isfoundinDiscovery; i++)
		{
			bluetoothSend = true;
			DTNBundleSpec spec = new DTNBundleSpec();
			spec.setSec_bundle_id(secretId);
			//


			// set destination from the user input
			spec.set_dest(new DTNEndpointID(dest_eid));

			// set the source EID from the bundle Daemon
			spec.set_source(new DTNEndpointID(BundleDaemon.getInstance().local_eid().toString()));

			// Set expiration in seconds, default to 1 hour
			spec.set_expiration(EXPIRATION_TIME);
			// no option processing for now
			spec.set_dopts(DELIVERY_OPTIONS);
			// Set prority
			spec.set_priority(PRIORITY);

		//dtn_api_binder_.BSend(device, spec, message);

		dtn_api_binder_.send_via_bluetooth(BluetoothDiscovery.devices.get(i), spec, origMsg,false);
		}
		if(!isfoundinDiscovery && bluetoothSend==false){

		}

		
		
	}
	
	/**
	 * validate the input EID and throw exception if it's valid
	 * @throws InvalidEndpointIDException
	 */
	private void checkInputEID() throws InvalidEndpointIDException
	{
		String dest_eid =   DestEIDEditText.getText().toString();
		if (!EndpointID.is_valid_URI(dest_eid)) throw new InvalidEndpointIDException();
		
	}
		
}