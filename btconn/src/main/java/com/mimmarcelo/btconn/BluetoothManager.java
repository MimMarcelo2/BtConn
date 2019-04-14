/**
 * File name: BluetoothManager
 * It is the interface between the client app and the Bluetooth library
 * It is responsible for control all Bluetooth sources
 */
package com.mimmarcelo.btconn;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public final class BluetoothManager implements BluetoothListener{

    //Singleton pattern
    private static BluetoothManager bluetoothManager;

    private Activity activity;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothBroadcast bluetoothBroadcast;
    private ConnectionThread connectionThread;
    private IntentFilter filter;
    private SelectServiceDialog selectServiceDialog;

    //Singleton pattern
    private BluetoothManager(Activity activity){

        //Set for Broadcast listen to
        this.filter = new IntentFilter();
        this.filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        this.filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.filter.addAction(BluetoothDevice.ACTION_FOUND);

        this.bluetoothBroadcast = BluetoothBroadcast.getBluetoothBroadcast();
        this.bluetoothBroadcast.registerObserver(this);

        this.activity = activity;
        this.activity.registerReceiver(bluetoothBroadcast, filter);

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.selectServiceDialog = SelectServiceDialog.getInstance(activity, this);

        this.connectionThread = null;

    }

    /**
     * Creates or returns a BluetoothManager instance (Singleton pattern)
     * @param activity Instance from the BluetoothManager is called
     * @return BluetoothManager instance
     */
    public static BluetoothManager getBluetoothManager(Activity activity) {
        if(bluetoothManager == null){
            bluetoothManager = new BluetoothManager(activity);
        }
        return bluetoothManager;
    }

    /**
     * Show popup asking permission to enable Bluetooth
     */
    public void turnOn() {
        Intent intent;
        if(bluetoothAdapter.isEnabled()){
            sendStatusMessage(BluetoothListener.STATUS_BLUETOOTH_TURNED_ON);
        }
        else {
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.activity.startActivity(intent);
            //Message comes from BluetoothBroadcast class
        }
    }

    /**
     * Turn Bluetooth off
     */
    public void turnOff(){
        if(bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            //Message comes from BluetoothBroadcast class
        }
        else {
            sendStatusMessage(BluetoothListener.STATUS_BLUETOOTH_TURNED_OFF);
        }
    }

    /**
     * Show popup asking permission to enable discovering
     * @param seconds Time for discovering
     */
    public void askToOpenService(int seconds){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds);
        activity.startActivity(intent);
        //Message comes from BluetoothBroadcast class
    }

    /**
     * Show popup with a list of open service
     */
    public void searchForOpenService(){
        if(checkPermissions()) {
            sendStatusMessage(BluetoothListener.STATUS_SEARCHING_FOR_SERVICES);
            bluetoothBroadcast.registerObserver(selectServiceDialog);
            selectServiceDialog.show();
        }
        else{
            sendStatusMessage(BluetoothListener.STATUS_PERMISSION_REQUIRED);
        }
    }

    /**
     * @return The BluetoothAdapter
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    /**
     * @return The connected device
     */
    public BluetoothDevice getDevice(){
        return connectionThread.getDevice();
    }

    /**
     * Send a message by the connection
     * @param message Message to be send
     */
    public void sendMessage(String message){
        Intent intent = new Intent();
        intent.putExtra(BluetoothListener.EXTRA_MESSAGE, message);
        connectionThread.sendMessage(intent);
    }

    /**
     * Main channel between BtConn and the client app.
     * All classes send data through this method that sends them to client app
     * @param intent Data received
     */
    @Override
    public void messageReceived(final Intent intent) {
        if(intent.hasExtra(BluetoothListener.EXTRA_STATUS)){
            switch (intent.getIntExtra(BluetoothListener.EXTRA_STATUS, 0)) {
                case BluetoothListener.STATUS_DISCOVERABLE_TURNED_ON:
                    openService();
                    break;
                case BluetoothListener.STATUS_DEVICE_SELECTED:
                    bluetoothBroadcast.unregisterObserver(selectServiceDialog);
                    if(intent.hasExtra(BluetoothDevice.EXTRA_DEVICE)){
                        BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        connect(d.getAddress());
                    }
                    break;
            }
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BluetoothListener)activity).messageReceived(intent);
            }
        });
    }

    /**
     * Starts the thread for connection as a Bluetooth server
     */
    private void openService(){
        connectionThread = new ServerConnectionThread(this);
        connectionThread.start();
        //Message comes from BluetoothBroadcast class
    }

    /**
     * Starts the thread for connection as a Bluetooth client
     * @param macAddress MAC address from Bluetooth server
     */
    private void connect(String macAddress){
        connectionThread = new ClientConnectionThread(this, macAddress);
        connectionThread.start();
        //Message comes from BluetoothBroadcast class
    }

    /**
     * Asks fine location permission that is necessary to connect as a Bluetooth client
     * @return true if the permission is enabled
     */
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return false;
            }
        }
        return true;
    }

    /**
     * Send a status message to activity
     * @param status to be send
     */
    private void sendStatusMessage(int status){
        Intent intent = new Intent();
        intent.putExtra(BluetoothListener.EXTRA_STATUS, status);
        messageReceived(intent);//Sends message
    }

    /**
     * Unregisters the broadcasts
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        bluetoothBroadcast.unregisterObserver(this);
        bluetoothBroadcast.unregisterObserver(selectServiceDialog);
        activity.unregisterReceiver(bluetoothBroadcast);
    }

    /**
     * Singleton pattern
     * @return This BluetoothManager instance;
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return bluetoothManager;
    }

}
