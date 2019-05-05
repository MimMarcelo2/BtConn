/**
 * File name: BluetoothBuilder
 * It is responsible to create a {@link com.mimmarcelo.btconn.BluetoothManager} instance
 */
package com.mimmarcelo.btconn;

import android.app.Activity;
import android.util.Log;

public abstract class BluetoothBuilder {

    private static String TAG = BluetoothBuilder.class.getName();

    public static BluetoothManager build(Activity activity){
        BluetoothManager manager = BluetoothManager.getInstance();

        manager.setActivity(activity);

        if(manager.getActivity() == null){
            Log.e(TAG, "An Activity implementing BluetoothListener is required", new Throwable());
        }
        return manager;
    }
}
