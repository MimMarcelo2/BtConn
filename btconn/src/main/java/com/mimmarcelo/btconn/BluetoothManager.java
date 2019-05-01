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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothManager implements BluetoothListener {

    /* ** Private static attributes ** */

    // Singleton pattern
    private static BluetoothManager bluetoothManager;

    /* ** Private attributes ** */

    private Activity activity;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothBroadcast bluetoothBroadcast;
    private List<ConnectionThread> connectionThreads;
    private IntentFilter filter;
    private SelectServiceDialog selectServiceDialog;
    private SelectConnectionDialog selectConnectionDialog;

    /* ** Constructors ** */

    /**
     * Singleton pattern
     *
     * @param activity current activity
     */
    private BluetoothManager(Activity activity) {

        //Set for Broadcast listen to
        this.filter = new IntentFilter();
        this.filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        this.filter.addAction(BluetoothDevice.ACTION_FOUND);
        this.filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        this.bluetoothBroadcast = BluetoothBroadcast.getBluetoothBroadcast();
        this.bluetoothBroadcast.registerObserver(this);

        this.activity = activity;
        this.activity.registerReceiver(bluetoothBroadcast, filter);

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.selectServiceDialog = SelectServiceDialog.getInstance(activity, this);
        this.selectConnectionDialog = SelectConnectionDialog.getInstance(activity, this);

        this.connectionThreads = new ArrayList<>();
    } // end constructor BluetoothManager

    /* ** Public static methods ** */

    /**
     * Creates and/or returns a BluetoothManager instance (Singleton pattern)
     *
     * @param activity Instance from the BluetoothManager is called
     * @return BluetoothManager instance
     */
    public static BluetoothManager getInstance(Activity activity) {
        if (bluetoothManager == null) {
            bluetoothManager = new BluetoothManager(activity);
        }
        return bluetoothManager;
    }

    /* ** Public methods ** */

    /**
     * @return The BluetoothAdapter
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    /**
     * Returns a list of all connected devices
     *
     * @return devices list
     */
    public List<BluetoothDevice> getConnectedDevices() {
        List<BluetoothDevice> devices = new ArrayList<>();
        for (ConnectionThread conn : getConnectionThreads()) {
            devices.add(conn.getDevice());
        }
        return devices;
    } // end getConnectedDevices method

    /**
     * Show popup asking permission to enable Bluetooth
     *
     * @param requestCode Number to identify the answer in onActivityResult method
     */
    public void turnOn(int requestCode) {
        if (bluetoothAdapter.isEnabled()) {
            sendStatusMessage(BluetoothListener.STATUS_BLUETOOTH_TURNED_ON);
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.activity.startActivityForResult(intent, requestCode);
            // Message comes from BluetoothBroadcast class
            // But, if the user do not enable, the answer is only received in onActivityResult method
        }
    } // end turnOn method

    /**
     * Turn Bluetooth off
     */
    public void turnOff() {
        if (bluetoothAdapter.isEnabled()) {
            stopAllConnections();
            bluetoothAdapter.disable();
            // Message comes from BluetoothBroadcast class
        } else {
            sendStatusMessage(BluetoothListener.STATUS_BLUETOOTH_TURNED_OFF);
        }
    } // end turnOff method

    /**
     * Verifies if there are activated connections,
     * If there are and it is a Client connection
     * - Send status error message STATUS_CONNECTED_AS_CLIENT_CANNOT_BE_A_SERVER
     * If there are no connection activated, or the connections are Server connection
     * - Show popup asking permission to enable discovering
     *
     * @param requestCode Number to identify the answer in onActivityResult method
     * @param seconds     Time for discovering
     */
    public void askToOpenService(int requestCode, int seconds) {
        if (connectionThreads.size() > 0) {
            if (connectionThreads.get(0) != null) {
                if (connectionThreads.get(0) instanceof ClientConnectionThread) {
                    sendStatusMessage(BluetoothListener.STATUS_CONNECTED_AS_CLIENT_CANNOT_BE_A_SERVER);
                    return;
                }
            }
        } // end if connectionsThread.size() > 0

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds);
        activity.startActivityForResult(intent, requestCode);
        // Message comes from BluetoothBroadcast class
        // But, if the user do not enable, the answer is only received in onActivityResult method
    } // end askToOpenService method

    /**
     * Verifies if there are activated connections,
     * If there are and it is a Server connection
     * - Send status error message STATUS_CONNECTED_AS_SERVER_CANNOT_BE_A_CLIENT
     * If there are no connection activated, or the connection is a Client connection
     * Show popup with a list of devices with open service
     * This functionality is possible only with ACCESS_FINE_LOCATION permission enabled
     *
     * @param requestCode Number to identify the answer in onRequestPermissionsResult method when asked for permission
     */
    public void searchForOpenService(int requestCode) {
        if (connectionThreads.size() > 0) {
            if (connectionThreads.get(0) != null) {
                if (connectionThreads.get(0) instanceof ServerConnectionThread) {
                    sendStatusMessage(BluetoothListener.STATUS_CONNECTED_AS_SERVER_CANNOT_BE_A_CLIENT);
                    return;
                }
            }
        } // end if connectionsThread.size() > 0

        if (checkPermissions(requestCode)) {
            sendStatusMessage(BluetoothListener.STATUS_SEARCHING_FOR_SERVICES);
            bluetoothBroadcast.registerObserver(selectServiceDialog);
            selectServiceDialog.show();
        } else {
            sendStatusMessage(BluetoothListener.STATUS_PERMISSION_REQUIRED);
        }
    } // end askToOpenService method

    /**
     * Shows a popup with all current connections
     * The selected connection will be closed
     */
    public void selectConnectionToClose(){
        if(getConnectionThreads().size() > 0) {
            selectConnectionDialog.show(getConnectionThreads());
        }
        else {
            sendStatusMessage(BluetoothListener.STATUS_NOT_CONNECTED);
        }
    }

    /**
     * Sends the message for all connected connections
     *
     * @param message Message to be send
     */
    public void sendMessage(String message) {
        Intent intent = new Intent();
        intent.putExtra(BluetoothListener.EXTRA_MESSAGE, message);
        for (ConnectionThread conn : connectionThreads) {
            conn.sendMessage(intent);
        }
    } // end sendMessage method

    /**
     * Main channel between BtConn and the client app.
     * All classes send data through this method that sends them to client app
     *
     * @param intent Data received
     */
    @Override
    public void messageReceived(final Intent intent) {
        if (intent.hasExtra(BluetoothListener.EXTRA_STATUS)) {
            // Some status require that specific actions would be run
            switch (intent.getIntExtra(BluetoothListener.EXTRA_STATUS, 0)) {
                case BluetoothListener.STATUS_DISCOVERABLE_TURNED_ON:
                    openService();
                    break;
                case BluetoothListener.STATUS_DEVICE_SELECTED:
                    bluetoothBroadcast.unregisterObserver(selectServiceDialog);
                    if (intent.hasExtra(BluetoothDevice.EXTRA_DEVICE)) {
                        BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        connect(d.getAddress());
                    }
                    break; // end case STATUS_DEVICE_SELECTED
                case BluetoothListener.STATUS_CONNECTION_SELECTED:
                    // Finishes the selected connection
                    if(intent.hasExtra(BluetoothListener.EXTRA_CONNECTION)){
                        stopConnection(connectionThreads.indexOf(intent.getSerializableExtra(BluetoothListener.EXTRA_CONNECTION)));
                    }
                    break;
                case BluetoothListener.STATUS_DEVICE_DISCONNECTED:
                    if (intent.hasExtra(BluetoothDevice.EXTRA_DEVICE)) {
                        BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        for(int i = connectionThreads.size()-1; i >=0 ; i--){
                            if(connectionThreads.get(i).getDevice().getAddress().equals(d.getAddress())){
                                stopConnection(i);
                                break;
                            }
                        }
                    }
            } // end switch EXTRA_STATUS
        } // end if has EXTRA_STATUS

        // To send the data for activity is necessary run in its thread
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BluetoothListener) activity).messageReceived(intent);
            }
        });
    } // end messageReceived method

    /* ** Private methods ** */

    /**
     * Returns a list of current connections
     *
     * @return Connections list
     */
    private List<ConnectionThread> getConnectionThreads(){
        stopUnutilizedConnections();
        return connectionThreads;
    }

    /**
     * Starts a new thread for connection as a Bluetooth server and adds to connectionThreads list
     */
    private void openService() {
        ConnectionThread conn = new ServerConnectionThread(this);
        conn.start();
        connectionThreads.add(conn);
        //Message comes from BluetoothBroadcast class
    }

    /**
     * If exists some connection, stops it
     * Starts a new thread for connection as a Bluetooth client and adds to connectionThreads list
     *
     * @param macAddress MAC address from Bluetooth server
     */
    private void connect(String macAddress) {
        stopAllConnections();
        ConnectionThread conn = new ClientConnectionThread(this, macAddress);
        conn.start();
        connectionThreads.add(conn);
        //Message comes from BluetoothBroadcast class
    } // end connect method

    /**
     * Send a status message to activity
     *
     * @param status to be send
     */
    private void sendStatusMessage(int status) {
        Intent intent = new Intent();
        intent.putExtra(BluetoothListener.EXTRA_STATUS, status);
        messageReceived(intent);//Sends message
    }

    /**
     * Stops the connection referred by the index
     */
    private void stopConnection(int index) {
        if (index >= 0) {
            if (connectionThreads.get(index) != null) {
                try {
                    ConnectionThread conn = connectionThreads.get(index);
                    conn.cancel();
                    conn.interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } // end if connection != null
            connectionThreads.remove(index);
        } // end if index >= 0
    } // end stopConnection method

    /**
     * Stops the all connections
     */
    private void stopAllConnections() {
        if (connectionThreads != null) {
            for (int index = connectionThreads.size() - 1; index >= 0; index--) {
                stopConnection(index);
            }
        }
    } // end stopAllConnections method

    /**
     * Stops only connections that have no for where send messages
     */
    private void stopUnutilizedConnections() {
        for (int index = connectionThreads.size() - 1; index >= 0; index--) {
            if (connectionThreads.get(index).getDevice() == null) {
                stopConnection(index);
            }
        }
    } // end stopUnutilizedConnections method

    /**
     * Asks for ACCESS_FINE_LOCATION permission that is necessary to connect as a Bluetooth client
     *
     * @param requestCode Number to identify the answer in onActivityResult method
     * @return true if the permission is enabled
     */
    private boolean checkPermissions(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
                return false;
            }
        }
        return true;
    } // end checkPermissions method

    /**
     * Singleton pattern
     *
     * @return This BluetoothManager instance;
     */
    @Override
    protected Object clone() {
        return bluetoothManager;
    }

    /**
     * Unregisters the broadcasts
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        bluetoothBroadcast.unregisterObserver(this);
        bluetoothBroadcast.unregisterObserver(selectServiceDialog);
        activity.unregisterReceiver(bluetoothBroadcast);
    }
}//end BluetoothManager class
