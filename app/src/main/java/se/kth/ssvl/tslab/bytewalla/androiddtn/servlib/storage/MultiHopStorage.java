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


package se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.Bundle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundlePayload;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundlePayload.location_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.config.DTNConfiguration;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.config.StorageSetting;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.storage.StorageIterator;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.TimeHelper;

/**
 * This class is implemented as Singleton to store bundles.
 * This class generates bundle id, stores bundle metadata on the disk 
 * and creates separate file for storing payload on the disk. This class also
 * uses Generic StorageImplementation and SQLiteImplementation to store bundles.
 *
 * @author Sharjeel Ahmed (sharjeel@kth.se)
 */

public class MultiHopStorage  {

    /**
     *  Singleton instance Implementation of the BundleStore
     */
    private static MultiHopStorage instance_ = null;

    /**
     * TAG for Android Logging
     */
    private static String TAG = "MultiHopStorage";

    /**
     * SQL Query for creating new Bundle in the SQLite database.
     */
    private static final String Table_CREATE_Bundle =
            "create table IF NOT EXISTS bundle (id integer primary key autoincrement, "
                    + "bundle_id integer not null,"
                    + "source varchar(100) ,"
                    + "destination varchar(100),"
                    + "msg varchar(1000),"
                    + "life integer)";
    private static final String Table_CREATE_Forwards =
            "create table IF NOT EXISTS  Forwards (id integer primary key autoincrement, "
                    + "bundle_id integer not null,"
                    + "destination varchar(100));";

    private static final String Table_CREATE_Contacts =
            "create table IF NOT EXISTS  Contacts (id integer primary key autoincrement, "
                    + "name varchar(100),"
                    + "mac_eid varchar(100));";
    private static final String Table_CREATE_Messages =
            "create table IF NOT EXISTS  Messages (id integer primary key autoincrement, "
                    + "msg varchar(1000),"
                    + "mac_eid varchar(100));";


    /**
     * Singleton Implementation Getter function
     * @return an singleton instance of BundleStore
     */
    public static MultiHopStorage getInstance() {
        if(instance_ == null) {
            instance_ = new MultiHopStorage();
        }
        return instance_;
    }


    /**
     * Private constructor for Singleton Implementation of the BundleStore
     */
    private  MultiHopStorage(){
    }

    /**
     * This function initiate the Bundle Storage for storing bundles.
     * This function creates StorageImplementation & SQLiteImplementation instances,
     * set storage directory path & counts valid number of bundles.
     * @param context The application context for getting for application paths  
     * @param config Get the application configuration to get the memory usage parameters 
     * @return returns true on success
     */

    public boolean init(Context context, DTNConfiguration config){

        config_ = config;

        Log.d(TAG, "Going to init" );

        if(!init_) {
            impt_sqlite_ = new SQLiteImplementation(context,Table_CREATE_Bundle);
           // impt_sqlite_.create_table(Table_CREATE_Forwards);
            forwards = new SQLiteImplementation(context,Table_CREATE_Forwards);
            impt_storage_ = new StorageImplementation<Bundle>(context);
            table_contacts = new SQLiteImplementation(context,Table_CREATE_Contacts);
            table_messages = new SQLiteImplementation(context,Table_CREATE_Messages);
            init_ = true;
            saved_bundles_ = new HashMap<Integer, Long>();
        }
        String app_folder = "/"+config_.storage_setting().storage_path();

        Log.d(TAG, "Current Path: "+path_);
        if(config.storage_setting().storage_type()== StorageSetting.storage_type_t.PHONE){
            path_ = context.getFilesDir().getAbsolutePath().concat(app_folder).concat("/storage");
        }else{
            path_ = Environment.getExternalStorageDirectory().getAbsolutePath().concat(app_folder).concat("/storage");
        }
        Log.d(TAG, "Current Path: "+path_);
        impt_storage_.create_dir(path_);

        String condition = " type = "+location_t.DISK.getCode();
        String[] field = new String[1];
        field[0] = "count(bundle_id)";
        bundle_count_ = impt_sqlite_.get_count(table,condition, field );

        Log.d(TAG, "Total Valid Bundlesss: "+bundle_count_);

        return true;
    }


    public Iterator<String> get_pendings()
    {
        return impt_sqlite_.get_all_msgs("bundle",null).iterator();
    }
    public Iterator<String> get_forwards()
    {
        return impt_sqlite_.get_Forwards("Forwards").iterator();
    }

    public Iterator<String> get_contacts_name()
    {
        return impt_sqlite_.get_all_contacts_name("Contacts", null).iterator();
    }

    // getting contact eid

    public Iterator<String> get_contacts_eid()
    {
        return impt_sqlite_.get_all_contacts_eid("Contacts", null).iterator();
    }

    /// change it
    public Iterator<String> get_messages_contacts()
    {
        return impt_sqlite_.get_message_contacts_db("Messages", "mac_eid").iterator();
    }

    public Iterator<String> get_messages_from_contacts(String condition){
        return impt_sqlite_.find_record_messages("Messages", condition).iterator();
    }

    /**
     * Get the payload file of bundle and return the file handler. 
     * @param bundleid Get the Bundle with this bundleid
     * @return returns the payload File of bundle
     */

    public File get_payload_file(int bundleid)
    {
        String payload_filname = payloadFileName+bundleid;
        return impt_storage_.get_file(payload_filname);
    }

    ///             ADDS in Contacts

    public boolean add_contacts(String name, String mac_eid )
    {
        try{
            ContentValues contactObj=new ContentValues();
            contactObj.put("name",name);
            contactObj.put("mac_eid", mac_eid);
            // Log.d
            table_contacts.add(contactsTableString, contactObj);
            int count= table_contacts.getTableSize(contactsTableString);
            return true;
        }catch(Exception e) {
            Log.e(TAG,"contacts table error "+ e.toString());
            return false;
        }


    }

    // add in message table

    public boolean add_messages(String mac_eid, String message )
    {
        try{
            ContentValues contactObj=new ContentValues();
            contactObj.put("mac_eid",mac_eid);
            contactObj.put("msg", message);
            // Log.d
            table_messages.add(messagesTableString, contactObj);
            int count= table_messages.getTableSize(messagesTableString);
            return true;
        }catch(Exception e) {
            Log.e(TAG,"contacts table error "+ e.toString());
            return false;
        }


    }

    /*
    adds entry in forwards table
     */
    public boolean add(int bundle_id, String destination )
    {
        if(destination.charAt( destination.length()-1)!='/')
            destination +="/";

        try{
            ContentValues contObj=new ContentValues();
            contObj.put("bundle_id",bundle_id);
            contObj.put("destination", destination);
           // Log.d
            forwards.add(forwardsTable, contObj);
            return true;
        }catch(Exception e) {
            Log.e(TAG,"forwards table error "+ e.toString());



            return false;
        }


    }

    /**
     * Store the new bundle on the disk. 
     * @param bundle Bundle that function will store on the Disk
     * @return return true if bundle successfully saved otherwise return false
     */
    public boolean add(Bundle bundle,String bundlePayload){
        try{
            ContentValues contObj=new ContentValues();
            //Log.d("TAG","bundletable "+ String.valueOf(impt_sqlite_.get_count("bundle",null,null)));
            contObj.put("bundle_id",(int)bundle.bundleid());
            contObj.put("source", bundle.source().toString());
            //custodian has the address of destination

            contObj.put("destination", bundle.custodian().toString());
            contObj.put("msg", bundlePayload);
            Log.v(TAG, "bundle_id" + (int) bundle.bundleid());
            Log.v(TAG,"source" + bundle.source().toString());
            Log.v(TAG, "destination" + bundle.dest().toString());
            Log.v(TAG, "daemon_dest" + impt_sqlite_.get_count(table, null, null));
           // bundle.payload().read_data(0, bundle.payload().length(), bundle.payload().memory_buf());

            String msg =bundle.owner();
            Log.v(TAG, "daemon_Msg : " + msg);
            int life= (int)TimeHelper.current_seconds_from_ref() +(24*3600);
            contObj.put("life", life);
            impt_sqlite_.add(table, contObj);
            //impt_sqlite_.get_all_bundles();



            Log.d("TAG","bundletable "+ String.valueOf(impt_sqlite_.get_count(table,null,null)));
            return true;
        }catch(Exception e) {
            Log.e(TAG,"bundletable error "+ e.toString());

            return false;
        }
    }

    public boolean add(int bundle_id,String source , String destination, String message){
        try{
            ContentValues contObj=new ContentValues();
            contObj.put("bundle_id",bundle_id);
            contObj.put("source", source);
            contObj.put("destination", destination);
            contObj.put("msg", message);
            int life= (int)TimeHelper.current_seconds_from_ref() + (24*3600);
            contObj.put("life", life);
            impt_sqlite_.add(table, contObj);
            return true;
        }catch(Exception e) {
            Log.e(TAG,"bundletable error "+ e.toString());

            return false;
        }
    }



    public int getTableSize(Context context){


        bundle_count_= impt_sqlite_.getTableSize(table);
        return bundle_count_;
    }

    public int getForwardsTableSize(Context context){

        forwards_bundle_count = forwards.getTableSize(forwardsTable);
        return  forwards_bundle_count;
    }

    public int getContactsTableSize(Context context){

        contact_bundle_count = impt_sqlite_.getTableSize(contactsTableString);
        return  contact_bundle_count;
    }

    /**
     * Get the bundle from storage based on bundleid and return the bundle. 
     * @param bundleid Get the Bundle with this bundle id
     * @return If bundle found then return the bundle otherwise return null 
     */

    public Bundle get(int bundleid){

        try{
            String filename = bundleFileName+bundleid;
            Log.d(TAG, "Going to get bundle in database : "+bundleid );
            Bundle bundle = impt_storage_.get_object(filename);
            return bundle;
        }catch(Exception e){
            Log.e(TAG, e.toString());
        }
        return null ;
    }

    /**
     * Update the bundle if bundle already saved on the disk. 
     * @param bundle Get the Bundle to update
     * @return return true if bundle successfully updated otherwise return false
     */
    public boolean update(Bundle bundle){
        int id = bundle.bundleid();
        String condition_is_record = " id = "+id;
        String limit = "1";
        String field = "id";
        String orderBy = null;
        int resulte_is_record = impt_sqlite_.get_record(table, condition_is_record, field, orderBy,limit);

        if((resulte_is_record==id) && bundle.payload().location()==BundlePayload.location_t.DISK){

            int bundleid = bundle.bundleid();
            Log.d(TAG, "Going to update bundle in database : "+bundleid );
            ContentValues values = new ContentValues();
            values.put("type", bundle.payload().location().getCode());

            String condition = "id = "+bundleid;

            if(impt_sqlite_.update(table, values, condition, null)){
                if(!saved_bundles_.containsKey(bundle.bundleid())){
                    long bundle_size = impt_storage_.get_file_size_with_name(bundleFileName+bundle.durable_key());
                    bundle_size += bundle.durable_size();
                    saved_bundles_.put(bundle.bundleid(), bundle_size);
                    global_storage_.add_total_size(bundle_size);
                    Log.d(TAG, "Added size : "+ bundle.durable_size()+ " to "+ global_storage_.get_total_size());
                }
                String bundle_filname = bundleFileName+bundle.durable_key();


                Log.e(TAG, "Updating One by One");
                //Testing functions
                Log.e(TAG, "Updating Object");
                boolean result = impt_storage_.add_object(bundle, bundle_filname);

                return result;
            }
        }
        return false;
    }


    /**
     * Delete the bundle if bundle exists. 
     * @param bundle Get the Bundle to delete
     * @return true if bundle successfully deleted otherwise return false
     */

    public boolean del(Bundle bundle){
        if(bundle.payload().location()==BundlePayload.location_t.DISK){
            String condition = "id = "+bundle.durable_key();
            Log.d(TAG, "Going to Del bundle in database: "+bundle.durable_key() );
            if(impt_sqlite_.delete_record(table, condition)){
                String filename = bundleFileName+bundle.durable_key();
                String filename_payload = payloadFileName+bundle.durable_key();
                if(saved_bundles_.containsKey(bundle.bundleid())){
                    long bundle_size = saved_bundles_.get(bundle.bundleid());
                    saved_bundles_.remove(bundle.bundleid());
                    global_storage_.remove_total_size(bundle_size);
                    Log.d(TAG, "deleteing size : "+ bundle_size);
                }
                if(impt_storage_.delete_file(filename) && impt_storage_.delete_file(filename_payload)){
                    bundle_count_ -= 1;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Delete the bundle if bundle exists. 
     * @param bundleid Get the Bundle with this bundleid to delete
     * @return return true if bundle successfully deleted otherwise return false
     */

    public boolean del(int bundleid){
        String condition = "bundle_id = "+bundleid;
        Log.d(TAG, "Going to Del bundle in database:"+bundleid);
        if(impt_sqlite_.delete_record(table,condition)){

            return true;
        }
        return false;
    }

    public boolean delete(String condition){
        if(impt_sqlite_.delete_record(table,condition)){

            return true;
        }
        return false;
    }

    /**
     * Generate an unique bundle id for new bundle. 
     * @return the next bundle id
     */
    public int next_id(){
        ContentValues values = new ContentValues();
        values.put("type", -1);

        int result =  impt_sqlite_.add(table, values);
        Log.d(TAG, "Returing new id:"+result);
        return result;
    }

    /**
     * Create a new iterator to iterator all the bundles.
     * This function also calls clean_garbage_bundles() to clean all the 
     * garbage bundles from disk before creating new iterator.
     * @return New bundle iterator to iterator bundles
     */

    /**
     * Private Class that implements Iterator to make a BundleIterator.
     */

    private static class BundleStoreIterator implements Iterator<Bundle>
    {
        /**
         * StorageIterator<Integer> object to storage Iterator. Logic is
         * implemented in StorageIterator that is a generic iterator.
         */
        private StorageIterator<Integer> itr_;

        /**
         * Set the iterator for bundles before iterating bundles.
         * Iterator checks for the next bundle in the database.  
         * @param  impt_sqlite SQLite obejct to access database
         * @param bundle_store to access bundle_store objects
         * @param table database table name 
         * @param pre_condition Database query condition has two parts pre_condition & post_condition  
         * @param post_condition Database query condition has two parts pre_condition & post_condition
         * @param first_condition Database query condition for initializing iterator
         */

        public void set_itr(SQLiteImplementation impt_sqlite, BundleStore bundle_store, String table, String pre_condition, String post_condition, String first_condition)
        {
            itr_ = new StorageIterator<Integer>(impt_sqlite, table, pre_condition, post_condition, first_condition);
        }


        /**
         * Check if iterator has more objects.
         * @return True if there is more else false  
         */
        public boolean hasNext() {
            return itr_.hasNext();
        }


        /**
         * Return the next Bundle
         * @return Next Bundle in the iterator  
         */

        public Bundle next() {
            return instance_.get(itr_.next());
        }


        public void remove() {
            // TODO Auto-generated method stub

        }

    }

    /**
     * This function closes SQLite connection.
     */

    public void close(){
        impt_sqlite_.close();
        init_ = false;
    }



    /**
     * This function get the total storage quota and return it
     * @return total storage quota.
     */

    public long quota()   {
        return config_.storage_setting().quota() * (long) Math.pow(2, 20);
    }


    /**
     * This function is the getter of total_size_
     * @return Total storage currently using.
     */

    public long total_size()      {
        return total_size_;
    }

    /**
     * This function is the setter of total_size_
     * @param sz sets the total_size_
     */

    public void set_total_size(long sz) {
        total_size_ = sz;
    }

    /**
     * Reset all the bundle storage. 
     * It resets bundle storage folder and bundle table in database.
     * @return If storage has been reset successfully then return true otherwise false.
     */

    public boolean reset_storage(){
        Log.d(TAG, "Going to delete Files");
        if(!impt_storage_.delete_dir(path_)){
            return false;
        }

        if(impt_sqlite_.drop_table(table)){
            if(impt_sqlite_.create_table(Table_CREATE_Bundle)){
              //return true;
            }
        }
        if(forwards.drop_table("Forwards")){
            if(forwards.create_table(Table_CREATE_Forwards)){
                return true;
            }
        }
        if(table_contacts.drop_table(contactsTableString)){
            if(table_contacts.create_table(Table_CREATE_Contacts)){

            }
        }
        if(table_messages.drop_table(messagesTableString)){
            if(table_contacts.create_table(Table_CREATE_Messages)){
                return true;
            }
        }
        return false;
    }

    /**
     * Get total number of stored bundles. 
     * @return Total number of stored bundles
     */

    public int get_bundle_count(){
        return bundle_count_;
    }

    /**
     * Test function to check if bundle file exists on the disk or not. 
     * @return True if file exist else return false
     */

    public boolean test_is_bundle_file(int bundleid){
        String filename = bundleFileName+bundleid;
        return impt_storage_.is_bundle_file(filename);
    }

    /**
     * Test function to check the stored bundle location type.
     * @param bundleid Bundle id to check its location type  
     * @return location type of bundle stored in database
     */

    public int test_get_location_type(int bundleid){
        String condition = " id = "+bundleid;
        String limit = "1";
        String field = "type";
        String orderBy = null;
        return impt_sqlite_.get_record(table, condition, field, orderBy,limit);
    }


    /**
     * Delete all the invalid bundles from database and disk
     */
    public void clean_garbage_bundles(){

        Iterator<Integer> iterator = impt_sqlite_.get_all_bundles();
        int id;
        Bundle b;
        global_storage_ = GlobalStorage.getInstance();
        int count = 0;
        while(iterator.hasNext()){
            count++;
            id = iterator.next();
            b = get(id);
            Log.d(TAG, "Validating Bundles: "+id);
            if(b==null){
                //delete id
                Log.d(TAG, "Validating Bundles deleting(bundle is null): "+id);
                this.del(id);
            }
            else{
                if(b.source().valid()){
                    if(b.dest().valid()){
                        Log.d(TAG, "Source and dest EIDs validated: "+id);
                        long bundle_size = impt_storage_.get_file_size_with_name(bundleFileName+b.durable_key());
                        bundle_size += b.durable_size();
                        saved_bundles_.put(b.bundleid(), bundle_size);
                        global_storage_.add_total_size(bundle_size);
                    }
                    else{
                        Log.d(TAG, "Only Source EID validated: "+id);
                    }
                }
                else{
                    Log.d(TAG, "EIDs not validated: "+id);
                }

                if(!b.complete()){
                    this.del(id);
                }
            }

        }

        // Update Bundle Count
        String condition = " type = "+location_t.DISK.getCode();
        String[] field = new String[1];
        field[0] = "count(bundle_id)";
        bundle_count_ = impt_sqlite_.get_count(table,condition, field );

        Log.d(TAG, "Total Valid Bundles: "+bundle_count_);
    }

    /**
     * Total memory consumption
     */
    protected  long total_size_;

    /**
     * StorageImplementation object to use with bundle
     */

    private static StorageImplementation<Bundle> impt_storage_;

    /**
     * SQLiteImplementation object
     */
    private static SQLiteImplementation impt_sqlite_;
    private static SQLiteImplementation forwards;
    private static SQLiteImplementation table_contacts;
    private static SQLiteImplementation table_messages;

    public static SQLiteImplementation impt_sqlite_(){return impt_sqlite_;}
    /**
     * DTNConfiguration to stores the application configurations,
     */
    private static DTNConfiguration config_;

    /**
     * Number of bundles stored on the disk
     */
    private static int bundle_count_;
    private static int forwards_bundle_count;
    private static int contact_bundle_count;
    /**
     * default bundle file name
     */
    private static String bundleFileName = "bundle_";

    /**
     * default bundle payload file name
     */
    private static String payloadFileName = "payload_";

    /**
     * Database table name for storing bundles.
     */
    private static String table = "bundle";
    private static String forwardsTable = "Forwards";

    private static String contactsTableString = "Contacts";
    private static String messagesTableString = "Messages";
    /**
     * GlobalStorage object
     */
    GlobalStorage global_storage_;

    /**
     * init_ to make sure in init() it only makes SQLiteImplementation only once
     */
    private static boolean init_ = false;

    /**
     * Storage path of bundle folder
     */
    private static String path_;

    /**
     * HashMap to store bundle id and bundle size of stored bundles.
     */
    private static HashMap<Integer, Long> saved_bundles_;
}