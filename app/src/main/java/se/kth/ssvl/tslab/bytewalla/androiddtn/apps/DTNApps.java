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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.util.Iterator;

import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNManager;
import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.MultiHopStorage;

/**
 * The portal Activity for DTNApplications.
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 */
//added by sebas

public class DTNApps extends Activity  {

	/**
	 * DTNSendOpenButton reference object
	 */
	private Button DTNSendOpenButton;
	/**
	 * DTNReceiveOpenBuntton reference object
	 */
	private Button DTNDiscoveryOpenButton;
	/**
	 * DTNDiscoveryButton reference object
	 */
	private Button DTNReceiveOpenButton;

	/**
	 * DTNMessageButton reference object
	 */
	private Button DTNMessageOpenButton;

	/**
	 * DTNBluetoothButton reference object
	 */
	private Button DTNBluetoothButton;


	/**
	 * CloseButton reference object
	 */
	private Button CloseButton;

	/**
	 * Oncreate overriding of Android Activity. Basically, this call DTNApps layout from Android Layout and then init() function of this class
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			setContentView(R.layout.dtnapps);
			init();

	}

	/**
	 * Initialiazing function for DTNApps. Bind the object to runtime button, add event listeners
	 */
		private void init()
		{

			DTNManager.contactName.clear();
			DTNManager.contactEid.clear();
			DTNManager.contactName.add("Ahsan");
			Iterator namesI = MultiHopStorage.getInstance().get_contacts_name();
			while(namesI.hasNext()){
				DTNManager.contactName.add(namesI.next().toString());
			}

			DTNManager.contactEid.add("dtn://020000000000.bytewalla.com/");
			Iterator eidI = MultiHopStorage.getInstance().get_contacts_eid();
			while(eidI.hasNext()){
				DTNManager.contactEid.add(eidI.next().toString());
			}
			DTNSendOpenButton    = (Button) this.findViewById(R.id.DTNApps_DTNSendOpenButton);
			DTNReceiveOpenButton = (Button) this.findViewById(R.id.DTNApps_DTNReceiveOpenButton);
			DTNDiscoveryOpenButton =	(Button) this.findViewById(R.id.DTNApps_DTNDiscoveryOpenButton);
			DTNMessageOpenButton = (Button) this.findViewById(R.id.DTNApps_DTNMessageOpenButton);
			//DTNBluetoothButton = (Button) this.findViewById(R.id.DTNApps_DTNBluetoothOpenButton);
			((Button) this.findViewById(R.id.DTNApps_DTNdebugOpenButton)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(DTNApps.this, DTNMessage.class);
					i.putExtra("debug",true);
					startActivity(i);
				}
			});
			CloseButton       = (Button) this.findViewById(R.id.DTNApps_CloseButton);

			CloseButton.setOnClickListener(new OnClickListener() {


				public void onClick(View v) {
					DTNApps.this.finish();

				}
			});

			DTNSendOpenButton.setOnClickListener(new OnClickListener() {
				
				
				public void onClick(View v) {
					Intent i = new Intent(DTNApps.this, DTNSend.class);
					startActivity(i);

					
				}
			});
			
			DTNReceiveOpenButton.setOnClickListener(new OnClickListener() {
				
				
				public void onClick(View v) {
					Intent i = new Intent(DTNApps.this, DTNReceive.class);
					startActivity(i);
				}
			});

			DTNDiscoveryOpenButton.setOnClickListener(new OnClickListener() {


				public void onClick(View v) {
					Intent i = new Intent(DTNApps.this, DTNDiscovery.class);
					startActivity(i);
				}
			});
			DTNMessageOpenButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(DTNApps.this, DTNMessage.class);
					i.putExtra("debug",false);
					startActivity(i);
				}
			});

		}
}
