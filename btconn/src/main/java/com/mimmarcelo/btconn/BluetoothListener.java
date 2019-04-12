/**
 * File name: BluetoothListener
 * It sets constants of the module and it is used to enable Observer pattern
 */
package com.mimmarcelo.btconn;

import android.content.Intent;

public interface BluetoothListener {
    //Define extra data names
    String EXTRA_MESSAGE = "extraMessage";
    String EXTRA_STATUS = "extraStatus";

    //Define EXTRA_STATUS values
    int STATUS_NONE = 0;
    int STATUS_BLUETOOTH_TURNED_ON = 1;
    int STATUS_BLUETOOTH_TURNED_OFF = 2;
    int STATUS_DISCOVERABLE_TURNED_ON = 3;
    int STATUS_DISCOVERABLE_TURNED_OFF = 4;
    int STATUS_DEVICE_CONNECTED = 5;

    /**
     * Method used to enable observer pattern
     * @param intent Data received
     */
    void messageReceived(Intent intent);
}
