/**
 * File name: BluetoothManager
 * It is the interface between the client app and the Bluetooth library
 * It is responsible for control all Bluetooth sources
 */
package com.mimmarcelo.btconn;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import java.util.Set;

public final class BluetoothManager implements BluetoothListener{

    //Singleton pattern
    private static BluetoothManager bluetoothManager;

    private Activity activity;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothBroadcast bluetoothBroadcast;
    private ConnectionThread connectionThread;
    private IntentFilter filter;

    //Singleton pattern
    private BluetoothManager(){
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothBroadcast = null;
        this.connectionThread = null;

        this.filter = new IntentFilter();
        this.filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        this.filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
    }

    /**
     * Creates or returns a BluetoothManager instance (Singleton pattern), and updates the activity param
     * @param activity Instance from the BluetoothManager is called
     * @return BluetoothManager instance
     */
    public static BluetoothManager getBluetoothManager(Activity activity) {
        if(bluetoothManager == null){
            bluetoothManager = new BluetoothManager();
        }
        bluetoothManager.setActivity(activity);
        return bluetoothManager;
    }

    /**
     * Update settings
     * @param activity
     */
    public void setActivity(Activity activity){
        bluetoothBroadcast = BluetoothBroadcast.getBluetoothBroadcast(this);
        this.activity = activity;
        this.activity.registerReceiver(bluetoothBroadcast, filter);
    }

    /**
     * Show popup asking permission to enable Bluetooth
     * @param requestCode Request identifier
     * @param options Extra data
     */
    public void turnOn(int requestCode, Bundle options) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        this.activity.startActivityForResult(intent, requestCode, options);
    }

    /**
     * Show popup asking permission to enable Bluetooth
     * @param requestCode Request identifier
     */
    public  void turnOn(int requestCode){
        turnOn(requestCode, null);
    }

    /**
     * Turn Bluetooth off
     */
    public void turnOff(){
        if(isActived()) {
            bluetoothAdapter.disable();
        }
    }

    public boolean isActived(){
        return  bluetoothAdapter.isEnabled();
    }

    public String getDeviceName(){
        return bluetoothAdapter.getName();
    }

    public Set<BluetoothDevice> getBondedDevices(){
        return bluetoothAdapter.getBondedDevices();
    }

    /**
     * Show popup asking permission to enable discovering
     * @param requestCode Request identifier
     * @param seconds Time for discovering
     * @param options Extra data
     */
    public void askToOpenService(int requestCode, int seconds, Bundle options){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds);
        activity.startActivityForResult(intent, requestCode, options);
    }

    /**
     * Show popup asking permission to enable discovering
     * @param requestCode Request identifier
     * @param seconds Time for discovering
     */
    public void askToOpenService(int requestCode, int seconds){
        askToOpenService(requestCode, seconds, null);
    }

    public void connect(String macAddress){
        connectionThread = new ClientConnectionThread(this, macAddress);
    }

    public BluetoothDevice getDevice(){
        return connectionThread.getDevice();
    }

    /**
     * Send message by the connection
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
                case BluetoothListener.STATUS_DEVICE_CONNECTED:
                    //Wait the connectionThread load the device
                    while (connectionThread.getDevice() == null){}
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
     * Starts the thread for connection
     */
    private void openService(){
        connectionThread = new ServerConnectionThread(this);
        connectionThread.start();
    }

}
