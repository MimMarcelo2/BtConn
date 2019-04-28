/**
 * File name: BluetoothListener
 * It sets constants of the module and it is used to enable Observer pattern
 */
package com.mimmarcelo.btconn;

import android.content.Intent;

public interface BluetoothListener {

    /* ** Public static constants ** */

    // Defines extra data names
    String EXTRA_MESSAGE = "extraMessage";
    String EXTRA_STATUS = "extraStatus";
    String EXTRA_CONNECTION = "extraConnection";

    // Defines EXTRA_STATUS values
    int STATUS_NONE = 0;
    int STATUS_BLUETOOTH_TURNED_ON = 1;
    int STATUS_BLUETOOTH_TURNED_OFF = 2;
    int STATUS_DISCOVERABLE_TURNED_ON = 3;
    int STATUS_DISCOVERABLE_TURNED_OFF = 4;
    int STATUS_DEVICE_CONNECTED = 5;
    int STATUS_DEVICE_FOUND = 6;
    int STATUS_DEVICE_SELECTED = 7;
    int STATUS_PERMISSION_REQUIRED = 8;
    int STATUS_SEARCHING_FOR_SERVICES = 9;
    int STATUS_CONNECTED_AS_CLIENT_CANNOT_BE_A_SERVER = 10;
    int STATUS_CONNECTED_AS_SERVER_CANNOT_BE_A_CLIENT = 11;
    int STATUS_NOT_CONNECTED = 12;
    int STATUS_CONNECTION_SELECTED = 13;

    /* ** Public methods ** */

    /**
     * Method used to enable observer pattern
     *
     * @param intent Data received
     */
    void messageReceived(Intent intent);
} // end BluetoothListener class
