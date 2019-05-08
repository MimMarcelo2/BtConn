/**
 * File name: BluetoothBroadcast
 * It is responsible to read the changes on {@link android.bluetooth.BluetoothAdapter}
 * Turned on, turned off, discoverable, time out of discoverable, device (dis)connected
 */
package com.mimmarcelo.btconn;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

final class BluetoothBroadcast extends BroadcastReceiver {

    /* ** Private static final attributes ** */

    private static final String TAG = BluetoothBroadcast.class.getName();

    /* ** Private static attributes ** */

    private static BluetoothBroadcast bluetoothBroadcast; // Singleton pattern

    /* ** Private attributes ** */

    private List<BluetoothListener> bluetoothListeners; // Observer pattern

    /**
     * Allow getting different combined {@link BluetoothAdapter#ACTION_SCAN_MODE_CHANGED} states:
     *
     * <ul>
     *     <li>When the {@link BluetoothAdapter} is turned on from off state</li>
     *     <li>Or when the {@link BluetoothAdapter} discoverable ends</li>
     * </ul>
     *
     * @see #onReceive(Context, Intent)
     */
    private int prevScanMode;

    /* ** Constructors ** */

    /**
     * Singleton pattern
     *
     * To get the {@link BluetoothBroadcast} instance call {@link #getInstance()}
     *
     * @see #getInstance()
     */
    private BluetoothBroadcast() {
        this.bluetoothListeners = new ArrayList<>();
        this.prevScanMode = 0;
        Log.i(TAG, "A new BluetoothBroadcast was created");
    }

    /* ** Public static methods ** */

    /**
     * Singleton pattern
     *
     * (Create and) and return the {@link BluetoothBroadcast} instance
     *
     * @return The BluetoothBroadcast instance
     */
    public static BluetoothBroadcast getInstance() {
        if (bluetoothBroadcast == null) {
            bluetoothBroadcast = new BluetoothBroadcast();
        }
        Log.i(TAG, "A BluetoothBroadcast was returned");
        return bluetoothBroadcast;
    }

    /* ** Public methods ** */

    /**
     * Observer pattern
     *
     * Register the observers ({@link BluetoothListener}
     *
     * @param bluetoothListener observer to be registered
     */
    public void registerObserver(BluetoothListener bluetoothListener) {
        Log.i(TAG, "Trying register a new observer");
        if (!bluetoothListeners.contains(bluetoothListener)) {
            bluetoothListeners.add(bluetoothListener);
            Log.i(TAG, "A new Observer was registered");
        }
    }

    /**
     * Observer pattern
     *
     * Unregister the observers ({@link BluetoothListener}
     *
     * @param bluetoothListener observer to be unregistered
     */
    public void unregisterObserver(BluetoothListener bluetoothListener) {
        Log.i(TAG, "Trying unregister a observer");
        if (bluetoothListeners.contains(bluetoothListener)) {
            bluetoothListeners.remove(bluetoothListener);
            Log.i(TAG, "A Observer was unregistered");
        }
    }

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     *
     * The Intent filters used in {@link android.content.Context#registerReceiver} are
     * registered in {@link BluetoothManager#setActivity(Activity)} and are:
     *
     * <ul>
     *     <li>{@link BluetoothAdapter#ACTION_SCAN_MODE_CHANGED}</li>
     *     <li>{@link BluetoothDevice#ACTION_FOUND}</li>
     *     <li>{@link BluetoothDevice#ACTION_ACL_CONNECTED}</li>
     *     <li>{@link BluetoothDevice#ACTION_ACL_DISCONNECTED}</li>
     * </ul>
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     *
     * @see BluetoothManager#setActivity(Activity)
     * @see BroadcastReceiver#onReceive(Context, Intent)
     * @see BluetoothAdapter#ACTION_SCAN_MODE_CHANGED
     * @see BluetoothDevice#ACTION_FOUND
     * @see BluetoothDevice#ACTION_ACL_CONNECTED
     * @see BluetoothDevice#ACTION_ACL_DISCONNECTED
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        int requestCode = BluetoothListener.NO_ACTION; // Request code to be send to observers

        Log.i(TAG, "Action received");
        switch (intent.getAction()){
            case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED: // Read changes in BluetoothAdapter
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE);
                Log.i(TAG, "ACTION_SCAN_MODE_CHANGED");

                switch (scanMode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        if (prevScanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                            requestCode = BluetoothListener.TURN_DISCOVERABLE_OFF;
                            Log.i(TAG, "Discoverable turned off");
                        } else if (prevScanMode != BluetoothListener.TURN_BLUETOOTH_ON) {
                            requestCode = BluetoothListener.TURN_BLUETOOTH_ON;
                            Log.i(TAG, "Bluetooth turned on");
                        }
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        requestCode = BluetoothListener.TURN_BLUETOOTH_OFF;
                        Log.i(TAG, "Bluetooth turned off");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        requestCode = BluetoothListener.TURN_DISCOVERABLE_ON;
                        Log.i(TAG, "Discoverable turned on");
                        break;
                        default:
                            Log.i(TAG, "Status '"+scanMode+"' not tested");
                } // end switch scanMode

                prevScanMode = scanMode; // Register this scan mode
                break; // end case ACTION_SCAN_MODE_CHANGED
            case BluetoothDevice.ACTION_FOUND: // Device found
                requestCode = BluetoothListener.DEVICE_FOUND;
                Log.i(TAG, "New device discovered");
                break;
            case BluetoothDevice.ACTION_ACL_CONNECTED: // Device connected
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                prevScanMode = BluetoothListener.TURN_BLUETOOTH_ON;
                requestCode = BluetoothListener.NO_ACTION;
                Log.i(TAG, "Device connected");
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED: // Device disconnected
                requestCode = BluetoothListener.DEVICE_DISCONNECTED;
                Log.i(TAG, "Device disconnected");
                break;
        } // end switch intent.getAction()

        // Send data to observers
        for (BluetoothListener bluetoothListener : bluetoothListeners) {
            bluetoothListener.onActivityResult(requestCode, Activity.RESULT_OK, intent);
            Log.i(TAG, "Data sent to observer");
        }
    } // end onReceive method
} // end BluetoothBroadcast class
