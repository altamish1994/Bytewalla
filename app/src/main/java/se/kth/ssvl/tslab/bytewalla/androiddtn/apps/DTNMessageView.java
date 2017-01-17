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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNManager;
import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.MultiHopStorage;

/**
 * The portal Activity for DTNApplications.
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 */
//added by sebas

public class DTNMessageView extends Activity  {


	private TextView title;
	private ListView listView ;
	static ArrayAdapter<String> adapter=null;
	static String value=null;
	static ArrayList<String> message_list=null;
	/**
	 * Oncreate overriding of Android Activity. Basically, this call DTNApps layout from Android Layout and then init() function of this class
	 */

	public static boolean update()
	{

		if(adapter!=null)
		{
			message_list.clear();
			Iterator<String > i = MultiHopStorage.getInstance().get_messages_from_contacts(value);
			while (i.hasNext())
			{
				String t_name=i.next();
//				boolean cono=true;
//				for(int a=0;a< DTNManager.contactEid.size();a++){
//					if(t_name.equals(DTNManager.contactEid.get(a))){
//						message_list.add(DTNManager.contactName.get(a));
//						cono=false;
//					}
//				}
				//if(cono){
					message_list.add(t_name);
				//}
				// here it is adding in forwards table
			}
				adapter.notifyDataSetChanged();
			return true;
		}
		return false;

	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dtn_message_view);
		init();
	}

	/**
	 * Initialiazing function for DTNApps. Bind the object to runtime button, add event listeners
	 */
	private void init()
	{
		 value = getIntent().getExtras().getString("Name");
		title = (TextView) findViewById(R.id.TitleName);
		boolean con=true;
		for(int a=0;a< DTNManager.contactEid.size();a++){
			if(value.equals(DTNManager.contactEid.get(a))){
				title.setText("Name: "+DTNManager.contactName.get(a));
				con=false;
			}
		}
		if(con){
			title.setText("Name: "+value);


		}

		listView = (ListView) findViewById(R.id.contact_messageList);
		 message_list = new ArrayList<String>();

		message_list.clear();
		 adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, message_list);
		update();
		listView.setAdapter(adapter);

	}
}
