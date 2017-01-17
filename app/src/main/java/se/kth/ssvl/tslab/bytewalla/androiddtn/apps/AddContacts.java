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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNManager;
import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.MultiHopStorage;

/**
 * The portal Activity for DTNApplications.
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 */
//added by sebas

public class AddContacts extends Activity  {

	/**
	 * AddContactButton reference object
	 */
	private Button AddContactButton;
	private EditText nameEditText;
	private EditText eidEditText;


	/**
	 * Oncreate overriding of Android Activity. Basically, this call DTNApps layout from Android Layout and then init() function of this class
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.add_contact);
		init();
	}

	/**
	 * Initialiazing function for DTNApps. Bind the object to runtime button, add event listeners
	 */
	private void init()
	{
		AddContactButton    = (Button) this.findViewById(R.id.add_contact_button_save);
		nameEditText		= (EditText) this.findViewById(R.id.contact_add_name);
		eidEditText			= (EditText) this.findViewById(R.id.contact_add_eid);
		AddContactButton.setOnClickListener(new OnClickListener() {


			public void onClick(View v) {
				String s_name=nameEditText.getText().toString();
				String s_eid=eidEditText.getText().toString();
				if(MultiHopStorage.getInstance().add_contacts(s_name,s_eid)) {
					DTNManager.contactName.add(nameEditText.getText().toString());
					DTNManager.contactEid.add(eidEditText.getText().toString());

				}
				finish();
			}
		});

	}
}
