/**
 * File name: BluetoothListener
 * It sets constants of the module and it is used to enable Observer pattern
 */
package com.mimmarcelo.btconn;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

public interface BluetoothListener {

    /* ** Public static constants ** */

    // Defines extra data names
    String EXTRA_MESSAGE = "extraMessage";
    String EXTRA_STATUS = "extraStatus";
    String EXTRA_CONNECTION = "extraConnection";
    String EXTRA_DEVICE = BluetoothDevice.EXTRA_DEVICE;

    // Defines EXTRA_STATUS values
    int NO_ACTION = -1;
    int TURN_BLUETOOTH_ON = 1;
    int TURN_BLUETOOTH_OFF = 2;
    int TURN_DISCOVERABLE_ON = 3;
    int TURN_DISCOVERABLE_OFF = 4;
    int TURN_SEARCHING_ON = 5;
    int DEVICE_FOUND = 6;
    int DEVICE_SELECTED = 7;
    int DEVICE_CONNECTED = 8;
    int DEVICE_DISCONNECTED = 9;
    int CLOSE_CONNECTION = 10;
    int MESSAGE_RECEIVED = 11;

    int BLUETOOTH_ALREADY_ON = 50;
    int BLUETOOTH_ALREADY_OFF = 51;
    int CONNECTED_AS_CLIENT_CANNOT_BE_A_SERVER = 52;
    int CONNECTED_AS_SERVER_CANNOT_BE_A_CLIENT = 53;
    int PERMISSION_REQUIRED = 54;
    int NO_CONNECTIONS = 55;

    /* ** Public methods ** */

    /**
     * Method used to enable observer pattern
     *
     * @param data Data received
     */
    void onActivityResult(int requestCode, int resultCode, Intent data);
} // end BluetoothListener class
