/**
 * File name: BluetoothManager
 * It is the interface between the client app and the Bluetooth btconn library
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

    private static BluetoothManager bluetoothManager; // Singleton pattern

    /* ** Private attributes ** */

    /**
     * Activity that will receive all {@link BluetoothListener#onActivityResult(int, int, Intent)}
     * calls. It works as an observer when Bluetooth Adapter changes.
     *
     * It is defined in {@link BluetoothBuilder#build(Activity)} or in
     * {@link #setActivity(Activity)} method
     *
     * @see BluetoothListener#onActivityResult(int, int, Intent)
     * @see BluetoothBuilder#build(Activity)
     * @see #setActivity(Activity)
     */
    private Activity activity;

    /**
     * Broadcast to listen to {@link BluetoothAdapter} changes
     * Need to be registered in active Activity (Context)
     *
     * It is created in {@link #setActivity(Activity)}
     *
     * @see #setActivity(Activity)
     */
    private BluetoothBroadcast bluetoothBroadcast;

    /**
     * Define what events the {@link #bluetoothBroadcast} may listen to
     *
     * It is set in {@link #BluetoothManager()}
     */
    private IntentFilter filter;

    /**
     * List of all activated connections
     */
    private List<ConnectionThread> connectionThreads;

    /* ** Constructors ** */

    /**
     * Singleton pattern
     *
     * Create a {@link BluetoothManager} instance, but it is accessible
     * only in own scope.
     *
     * It is called, <em>if not exists,</em> in {@link #getInstance()}
     *
     * @see #getInstance()
     */
    private BluetoothManager() {

        //Set for Broadcast listen to
        this.filter = new IntentFilter();
        this.filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        this.filter.addAction(BluetoothDevice.ACTION_FOUND);
        this.filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        this.bluetoothBroadcast = BluetoothBroadcast.getInstance();
        this.bluetoothBroadcast.registerObserver(this);

        this.connectionThreads = new ArrayList<>();
    } // end constructor BluetoothManager

    /* ** Public static methods ** */

    /**
     * Singleton pattern
     *
     * (Create and) return a {@link BluetoothManager} instance
     *
     * It is called in {@link BluetoothBuilder#build(Activity)}
     *
     * @return The {@link BluetoothManager} instance
     */
    protected static BluetoothManager getInstance() {
        if (bluetoothManager == null) {
            bluetoothManager = new BluetoothManager();
        }
        return bluetoothManager;
    }

    /* ** Public methods ** */

    /**
     * @return The BluetoothAdapter
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
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

    public Activity getActivity() {
        return activity;
    }

    /**
     * Set or update the activity for {@link BluetoothManager}
     *
     * @param activity
     */
    public void setActivity(Activity activity){
        if(activity != null) {
            if (activity instanceof BluetoothListener) {
                /*
                 * If exist a previous Activity, unregister the BluetoothBroadcast
                 */
                if(this.activity != null){
                    this.activity.unregisterReceiver(bluetoothBroadcast);
                }
                this.activity = activity;
                this.activity.registerReceiver(bluetoothBroadcast, filter);

                this.activity = activity;
            } // end if activity instanceof BluetoothListener
        } // end if activity != null
    } // end setActivity method

    /**
     * Show popup asking permission to enable Bluetooth
     *
     * The answer can be caught on {@link BluetoothListener#onActivityResult(int, int, Intent)}
     * Possible resultCode:
     * {@link Activity#RESULT_OK} when the bluetooth is turned on
     * {@link Activity#RESULT_CANCELED} when the user cancels the operation
     * {@link BluetoothListener#BLUETOOTH_ALREADY_ON} when the Bluetooth is already on
     */
    public void turnBluetoothOn() {
        if (getBluetoothAdapter().isEnabled()) {
            onActivityResult(TURN_BLUETOOTH_ON, BLUETOOTH_ALREADY_ON, null);
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, TURN_BLUETOOTH_ON);
        }
    } // end turnBluetoothOn method

    /**
     * Turn Bluetooth off
     *
     * The answer can be caught on {@link BluetoothListener#onActivityResult(int, int, Intent)}
     * Possible resultCode:
     * {@link Activity#RESULT_OK} when the bluetooth is turned off
     * {@link BluetoothListener#BLUETOOTH_ALREADY_OFF} when the Bluetooth is already off
     */
    public void turnBluetoothOff() {
        if (getBluetoothAdapter().isEnabled()) {
            stopAllConnections();
            getBluetoothAdapter().disable();
        } else {
            onActivityResult(TURN_BLUETOOTH_OFF, BLUETOOTH_ALREADY_OFF, null);
        }
    } // end turnBluetoothOff method

    /**
     * Verify if there are activated connections,
     * If there are and it is a Client connection
     * - Send status error message CONNECTED_AS_CLIENT_CANNOT_BE_A_SERVER
     * If there are no connection activated, or the connections are Server connection
     * - Show popup asking permission to enable discovering
     *
     * The answer can be caught on {@link BluetoothListener#onActivityResult(int, int, Intent)}
     * Possible resultCode:
     * {@link Activity#RESULT_OK} when the bluetooth is turned off
     * {@link Activity#RESULT_CANCELED} when the user cancels the operation
     * {@link BluetoothListener#CONNECTED_AS_CLIENT_CANNOT_BE_A_SERVER} when there is a client connection
     *
     * @param seconds Time for discovering
     */
    public void turnDiscoverableOn(int seconds) {
        if (connectionThreads.size() > 0) {
            if (connectionThreads.get(0) != null) {
                if (connectionThreads.get(0) instanceof ClientConnectionThread) {
                    onActivityResult(TURN_DISCOVERABLE_ON, CONNECTED_AS_CLIENT_CANNOT_BE_A_SERVER, null);
                    return;
                }
            } // end if connectionThreads.get(0) != null
        } // end if connectionsThread.size() > 0

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds);
        activity.startActivityForResult(intent, TURN_DISCOVERABLE_ON);
    } // end turnDiscoverableOn method

    /**
     * Verifies if there are activated connections,
     * If there are and it is a Server connection
     * - Send status error message CONNECTED_AS_SERVER_CANNOT_BE_A_CLIENT
     * If there are no connection activated, or the connection is a Client connection
     * Show popup with a list of devices with open service
     * This functionality is possible only with ACCESS_FINE_LOCATION permission enabled
     *
     * The answer can be caught on onActivityResult method in Activity
     * Possible resultCode:
     * {@link Activity#RESULT_OK} when the search starts
     * {@link BluetoothListener#CONNECTED_AS_SERVER_CANNOT_BE_A_CLIENT} when there is a server connection
     * {@link BluetoothListener#PERMISSION_REQUIRED} when the permission is required
     *
     * If the ACCESS_FINE_LOCATION permission it was required, the answer can be caught on
     * {@link android.support.v7.app.AppCompatActivity#onRequestPermissionsResult(int, String[], int[])}
     * requestCode = {@link BluetoothListener#PERMISSION_REQUIRED}
     */
    public void searchForServices() {
        if (connectionThreads.size() > 0) {
            if (connectionThreads.get(0) != null) {
                if (connectionThreads.get(0) instanceof ServerConnectionThread) {
                    onActivityResult(TURN_SEARCHING_ON, CONNECTED_AS_SERVER_CANNOT_BE_A_CLIENT, null);
                    return;
                }
            }
        } // end if connectionsThread.size() > 0

        // Verify is permission Manifest.permission.ACCESS_FINE_LOCATION is enabled
        if (checkPermissions(PERMISSION_REQUIRED)) {
            onActivityResult(TURN_SEARCHING_ON, Activity.RESULT_OK, null);
            SelectServiceDialog selectServiceDialog = SelectServiceDialog.getInstance(activity, this);
            selectServiceDialog.show();
        } else {
            onActivityResult(TURN_SEARCHING_ON, PERMISSION_REQUIRED, null);
        }
    } // end turnDiscoverableOn method

    /**
     * Show a popup with all current connections
     * The selected connection will be closed
     */
    public void selectConnectionToClose(){
        if(getConnectionThreads().size() > 0) {
            SelectConnectionDialog selectConnectionDialog = SelectConnectionDialog.getInstance(activity, this);
            selectConnectionDialog.show(getConnectionThreads());
        }
        else {
            onActivityResult(CLOSE_CONNECTION, NO_CONNECTIONS, null);
        }
    } // End selectConnectionToClose

    /**
     * Send the message for all connected connections
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
     * @param data Data received
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            // Some requestCode requires that specific actions would be run
            switch (requestCode) {
                case TURN_DISCOVERABLE_ON:
                    openService();
                    break;
                case DEVICE_SELECTED:
                    if (resultCode == Activity.RESULT_OK) {
                        BluetoothDevice d = data.getParcelableExtra(EXTRA_PARAM);
                        connect(d.getAddress());
                    }
                    break; // end case DEVICE_SELECTED
                case CLOSE_CONNECTION:
                    // Finish the selected connection
                    if(resultCode == Activity.RESULT_OK){
                        stopConnection(connectionThreads.indexOf(data.getSerializableExtra(BluetoothListener.EXTRA_PARAM)));
                    }
                    break;
                case DEVICE_DISCONNECTED:
                    stopUnutilizedConnections();
                    break;
            } // end switch EXTRA_STATUS

        // To send the data for activity is necessary run in its own thread
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BluetoothListener) activity).onActivityResult(requestCode, resultCode, data);
            }
        });
    } // end onActivityResult method

    /* ** Private methods ** */

    /**
     * Return a list of current connections
     *
     * @return Connections list
     */
    private List<ConnectionThread> getConnectionThreads() {
        stopUnutilizedConnections();
        return connectionThreads;
    }

    /**
     * Starts a new thread for connection as a Bluetooth server
     * and adds to connectionThreads list
     */
    private void openService() {
        ConnectionThread conn = new ServerConnectionThread(this);
        conn.start();
        connectionThreads.add(conn);
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
    } // end connect method

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
     * @param requestCode Number to identify the answer in sendError method
     *
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
     * Unregister the broadcasts
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        bluetoothBroadcast.unregisterObserver(this);
        activity.unregisterReceiver(bluetoothBroadcast);
    }

} // End BluetoothManager class
