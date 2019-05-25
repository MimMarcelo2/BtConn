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
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BluetoothManager implements BluetoothListener {

    /* ** Constants ** */

    private static final String TAG = BluetoothManager.class.getName();

    /* ** Private static attributes ** */

    private static BluetoothManager bluetoothManager; // Singleton pattern

    /* ** Private attributes ** */

    /**
     * Activity that will receive all {@link BluetoothListener#onActivityResult(int, int, Intent)}
     * calls. It works as an observer when Bluetooth behaviour changes.
     *
     * <p>
     *     It is defined in {@link BluetoothBuilder#BluetoothBuilder(Activity, UUID)}
     *     or in {@link BluetoothBuilder#BluetoothBuilder(Activity)}
     *     or in {@link #setActivity(Activity)} method
     * </p>
     *
     * @see BluetoothListener#onActivityResult(int, int, Intent)
     * @see BluetoothBuilder#BluetoothBuilder(Activity, UUID)
     * @see BluetoothBuilder#BluetoothBuilder(Activity)
     * @see #setActivity(Activity)
     */
    private Activity activity;

    /**
     * The Universally Unique Identifier for the application.
     *
     * <p>
     *     It is a number that identifies uniquely the application on the network,
     *     this way, only applications with the same {@link UUID} can
     *     establishes Bluetooth connection
     * </p>
     *
     * It is defined in {@link BluetoothBuilder#BluetoothBuilder(Activity, UUID)}
     * or in {@link BluetoothBuilder#setUuid(UUID)}
     * or in {@link BluetoothBuilder#setUuid(String)}
     * or in {@link #setUuid(UUID)}
     * or in {@link #setUuid(String)}
     *
     * @see BluetoothBuilder#BluetoothBuilder(Activity, UUID)
     * @see BluetoothBuilder#setUuid(UUID)
     * @see BluetoothBuilder#setUuid(String)
     * @see #setUuid(UUID)
     * @see #setUuid(String)
     */
    private UUID uuid;

    /**
     * Broadcast to listen to {@link BluetoothAdapter} changes
     * Need to be registered in active Activity (Context)
     *
     * <p>
     *     It is managed in {@link #setActivity(Activity)}
     * </p>
     * @see #setActivity(Activity)
     */
    private BluetoothBroadcast bluetoothBroadcast;

    /**
     * Define what events the {@link #bluetoothBroadcast} may listen to
     *
     * <p>
     *     It is set in {@link #BluetoothManager()}
     * </p>
     */
    private IntentFilter filter;

    /**
     * List of all activated connections
     */
    private List<ConnectedThread> connectedThreads;

    /**
     * Create and manage popups to the application
     */
    SelectItemDialog selectItemDialog;

    /* ** Constructors ** */

    /**
     * Singleton pattern
     *
     * <p>
     *     Create a {@link BluetoothManager} instance, but it is accessible
     *     only inner btconn library.
     * </p>
     *
     * It is called, <em>if not exists,</em> in {@link #getInstance()}
     *
     * @see #getInstance()
     */
    private BluetoothManager() {

        //Set what Broadcast may listen to
        this.filter = new IntentFilter();
        this.filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        this.filter.addAction(BluetoothDevice.ACTION_FOUND);
        this.filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        this.bluetoothBroadcast = BluetoothBroadcast.getInstance();
        this.bluetoothBroadcast.registerObserver(this);

        this.connectedThreads = new ArrayList<>();
        Log.i(TAG, "New BluetoothManager created");
    } // end constructor BluetoothManager

    /* ** Public static methods ** */

    /**
     * Singleton pattern
     *
     * <p>
     *     (Create and) return a {@link BluetoothManager} instance
     * </p>
     *
     * It is called in {@link BluetoothBuilder#build()}
     *
     * @return The {@link BluetoothManager} instance
     */
    protected static BluetoothManager getInstance() {
        if (bluetoothManager == null) {
            bluetoothManager = new BluetoothManager();
        }
        Log.i(TAG, "BluetoothManager required");
        return bluetoothManager;
    }

    /**
     * Stop all process involving btconn library
     */
    public void destroy(){
        bluetoothBroadcast.unregisterObserver(this);
        activity.unregisterReceiver(bluetoothBroadcast);
        stopAllConnections();
        bluetoothManager = null;
        Log.i(TAG, "BluetoothManager removed from activity");
    }

    /* ** Public methods ** */

    /**
     * @return The BluetoothAdapter
     */
    public BluetoothAdapter getBluetoothAdapter() {
        Log.i(TAG, "BluetoothAdapter required");
        return BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * @return The registred Activity
     */
    public Activity getActivity() {
        Log.i(TAG, "Activity required");
        return activity;
    }

    /**
     * Return a list of current connections
     *
     * @return Connections list
     */
    public List<ConnectedThread> getConnections() {
        stopUnutilizedConnections();
        Log.i(TAG, "List of connections required");
        return connectedThreads;
    }

    /**
     * Set or update the activity for {@link BluetoothManager}
     *
     * @param activity
     */
    public void setActivity(Activity activity) {
        if (activity != null) {
            if (activity instanceof BluetoothListener) {

                // If exist a previous Activity, unregister the BluetoothBroadcast
                if (this.activity != null) {
                    Log.i(TAG, "BluetoothBroadcast unregistered from previous Activity");
                    this.activity.unregisterReceiver(bluetoothBroadcast);
                }
                this.activity = activity;
                this.activity.registerReceiver(bluetoothBroadcast, filter);
                Log.i(TAG, "Activity updated");
            } // end if activity instanceof BluetoothListener
            else {
                Log.e(TAG, "Activity needs to be a instance of BluetoothListener", new Throwable());
            }
        } // end if activity != null
        else {
            Log.e(TAG, "Activity is null", new Throwable());
        }
    } // end setActivity method

    /**
     * Set or update the {@link UUID} connection number
     *
     * @param uuid
     */
    public void setUuid(UUID uuid) {
        Log.i(TAG, "UUID updated");
        this.uuid = uuid;
    }

    /**
     * Convert a {@link String} to {@link UUID}, so
     * set or update the {@link UUID} connection number
     *
     * @param uuid
     */
    public void setUuid(String uuid) {
        setUuid(UUID.fromString(uuid));
    }

    /**
     * Show popup asking permission to enable Bluetooth
     *
     * <p>
     *     The answer can be caught on {@link BluetoothListener#onActivityResult(int, int, Intent)}
     * </p>
     *
     * Possible resultCode:
     *
     * <ul>
     *     <li>{@link Activity#RESULT_OK} when the bluetooth is turned on successfully</li>
     *     <li>{@link Activity#RESULT_CANCELED} when the user cancels the operation</li>
     *     <li>{@link BluetoothListener#BLUETOOTH_ALREADY_ON} when the Bluetooth is already on</li>
     * </ul>
     */
    public void turnBluetoothOn() {
        if (getBluetoothAdapter().isEnabled()) {
            Log.i(TAG, "Bluetooth is already on");
            onActivityResult(TURN_BLUETOOTH_ON, BLUETOOTH_ALREADY_ON, null);
        } else {
            Log.i(TAG, "Asking permission to turn Bluetooth on");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, TURN_BLUETOOTH_ON);
        }
    } // end turnBluetoothOn method

    /**
     * Turn Bluetooth off
     *
     * <p>
     *     The answer can be caught on {@link BluetoothListener#onActivityResult(int, int, Intent)}
     * </p>
     *
     * Possible resultCode:
     *
     * <ul>
     *     <li>{@link Activity#RESULT_OK} when the bluetooth is turned off</li>
     *     <li>{@link BluetoothListener#BLUETOOTH_ALREADY_OFF} when the Bluetooth is already off</li>
     * </ul>
     */
    public void turnBluetoothOff() {
        if (getBluetoothAdapter().isEnabled()) {
            stopAllConnections();
            getBluetoothAdapter().disable();
            Log.i(TAG, "Turning Bluetooth off");
        } else {
            Log.i(TAG, "Bluetooth is already off");
            onActivityResult(TURN_BLUETOOTH_OFF, BLUETOOTH_ALREADY_OFF, null);
        }
    } // end turnBluetoothOff method

    /**
     * Show popup asking permission to enable discovering
     *
     * <p>
     *     The answer can be caught on {@link BluetoothListener#onActivityResult(int, int, Intent)}
     * </p>
     *
     * Possible resultCode:
     *
     * <ul>
     *     <li>{@link Activity#RESULT_OK} when the bluetooth is turned off</li>
     *     <li>{@link Activity#RESULT_CANCELED} when the user cancels the operation</li>
     * </ul>
     *
     * @param seconds Time <em>,in seconds,</em> for discovering
     */
    public void turnDiscoverableOn(int seconds) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds);

        Log.i(TAG, "Asking to turn Bluetooth discoverable");
        activity.startActivityForResult(intent, TURN_DISCOVERABLE_ON);
    } // end turnDiscoverableOn method

    /**
     * Show popup with a list of devices with open service
     *
     * <p>
     *     If the Android Device version is greater than 6 {@link android.os.Build.VERSION_CODES#M},
     *     this functionality is possible only with ACCESS_FINE_LOCATION permission enabled
     * </p>
     * <p>
     *     The answer can be caught on onActivityResult method in Activity
     * </p>
     *
     * Possible resultCode:
     *
     * <ul>
     *     <li>{@link Activity#RESULT_OK} when the search starts</li>
     *     <li>{@link BluetoothListener#PERMISSION_REQUIRED} when the permission is required</li>
     * </ul>
     *
     * <p>
     * If the ACCESS_FINE_LOCATION permission it was required, the answer can be caught on
     * {@link android.support.v7.app.AppCompatActivity#onRequestPermissionsResult(int, String[], int[])}
     * </p>
     *
     * requestCode
     *
     * <ul>
     *     <li>{@link BluetoothListener#PERMISSION_REQUIRED}</li>
     * </ul>
     */
    public void searchForServices() {
        // Verify if permission Manifest.permission.ACCESS_FINE_LOCATION is enabled
        if (checkPermissions(PERMISSION_REQUIRED)) {
            Log.i(TAG, "Searching for discoverable services");
            onActivityResult(TURN_SEARCHING_ON, Activity.RESULT_OK, null);
            selectItemDialog = new SelectItemDialog(activity, this, DEVICE_SELECTED);
            getBluetoothAdapter().startDiscovery();
        } else {
            Log.i(TAG, "Asking for required permission");
            onActivityResult(TURN_SEARCHING_ON, PERMISSION_REQUIRED, null);
        }
    } // end turnDiscoverableOn method

    /**
     * Show a popup with all current connections
     *
     * <p>
     *     The selected connection will be closed
     * </p>
     */
    public void selectConnectionToClose() {
        if (getConnections().size() > 0) {
            Log.i(TAG, "Asking to select connection to be closed");
            new SelectItemDialog(activity, this, (List<BluetoothItem>) (List<?>) connectedThreads, CLOSE_CONNECTION);
        } else {
            Log.i(TAG, "There is no connection to be closed");
            onActivityResult(CLOSE_CONNECTION, NO_CONNECTIONS, null);
        }
    } // End selectConnectionToClose

    /**
     * Send the message to all connected devices
     *
     * @param message Message to be send
     */
    public void sendMessage(String message) {
        Log.i(TAG, "Sending message: " + message);
        for (ConnectedThread conn : connectedThreads) {
            conn.sendMessage(message);
        }
    } // end sendMessage method

    /**
     * Send the message to the specific conn
     *
     * @param message
     * @param conn
     */
    public void sendMessage(String message, ConnectedThread conn) {
        Log.i(TAG, "Sending message: " + message);
        conn.sendMessage(message);
    }

    /**
     * Send the message to connection specified by the connIndex
     *
     * @param message
     * @param connIndex
     */
    public void sendMessage(String message, int connIndex) {
        sendMessage(message, connectedThreads.get(connIndex));
    }

    /**
     * Main channel between BtConn and the client app.
     *
     * <p>
     *     All btconn classes send data through this method
     *     before that data to be send to client app
     * </p>
     *
     * @param data Data received
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        // Some requestCode requires that specific actions would be run
        switch (requestCode) {
            case DEVICE_FOUND:
                if (data.hasExtra(BluetoothDevice.EXTRA_DEVICE)) {
                    BluetoothDevice d = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    selectItemDialog.update(new BluetoothItemAdapter(d));
                }
                break; // end case DEVICE_FOUND
            case TURN_DISCOVERABLE_ON:
                Log.i(TAG, "Service is open");
                openService();
                break;
            case DEVICE_CONNECTED:
                if (resultCode == Activity.RESULT_OK) {
                    if (data.hasExtra(EXTRA_CONNECTION)) {
                        ConnectedThread conn = (ConnectedThread) data.getSerializableExtra(EXTRA_CONNECTION);
                        conn.start();
                        connectedThreads.add(conn);
                    }
                } // End if (resultCode == Activity.RESULT_OK)
                break; // End case DEVICE_CONNECTED
            case DEVICE_SELECTED:
                if (getBluetoothAdapter().isDiscovering()) {
                    getBluetoothAdapter().cancelDiscovery();
                }
                if (resultCode == Activity.RESULT_OK) {
                    BluetoothDevice d = data.getParcelableExtra(EXTRA_CONNECTION);
                    Log.i(TAG, "Device selected: " + d.getAddress());
                    connect(d.getAddress());
                } else {
                    Log.i(TAG, "None device selected");
                }
                break; // end case DEVICE_SELECTED
            case CLOSE_CONNECTION:
                if (resultCode == Activity.RESULT_OK) {
                    stopConnection(connectedThreads.indexOf(data.getSerializableExtra(BluetoothListener.EXTRA_CONNECTION)));
                    Log.i(TAG, "Closing connection");
                }
                break;
            case DEVICE_DISCONNECTED:
                stopUnutilizedConnections();
                Log.i(TAG, "Connection closed");
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
     * Starts a new thread for connection as a Bluetooth server
     * and adds to connectedThreads list
     */
    private void openService() {
        ConnectionThread conn = new ConnectionThread(uuid, this);
        Log.i(TAG, "Server thread started");
        conn.start();
    }

    /**
     * Starts a new thread for connection as a Bluetooth client and adds to connectedThreads list
     *
     * @param macAddress MAC address from Bluetooth server
     */
    private void connect(String macAddress) {
        ConnectionThread client = new ConnectionThread(uuid, this, macAddress);
        client.start();
        Log.i(TAG, "Client thread started");
    } // end connect method

    /**
     * Stops the connection referred by the index
     */
    private void stopConnection(int index) {
        if (index >= 0) {
            if (connectedThreads.get(index) != null) {
                try {
                    ConnectedThread conn = connectedThreads.get(index);
                    conn.cancel();
                    conn.interrupt();
                    Log.i(TAG, "Connection closed");
                } catch (IOException e) {
                    Log.e(TAG, "Exception: ", new Throwable());
                    e.printStackTrace();
                } // End try catch

                connectedThreads.remove(index);
            } // end if connection != null
            Log.i(TAG, "Connection removed from list");
        } // end if index >= 0
    } // end stopConnection method

    /**
     * Stops the all connections
     */
    private void stopAllConnections() {
        if (connectedThreads != null) {
            for (int index = connectedThreads.size() - 1; index >= 0; index--) {
                stopConnection(index);
            }
        }
    } // end stopAllConnections method

    /**
     * Stops only connections that have no where to send messages
     */
    private void stopUnutilizedConnections() {
        for (int index = connectedThreads.size() - 1; index >= 0; index--) {
            if (connectedThreads.get(index).getDevice() == null || !connectedThreads.get(index).isRunning()) {
                stopConnection(index);
            }
        }
    } // end stopUnutilizedConnections method

    /**
     * Asks for ACCESS_FINE_LOCATION permission that is necessary to connect as a Bluetooth client
     *
     * @param requestCode Number to identify the answer in sendError method
     * @return true if the permission is enabled
     */
    private boolean checkPermissions(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Asking required permission");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
                return false;
            }
        } // End if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
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
} // End BluetoothManager class
