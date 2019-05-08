/**
 * File name: BluetoothBuilder
 * It is responsible to create a {@link com.mimmarcelo.btconn.BluetoothManager} instance
 */
package com.mimmarcelo.btconn;

import android.app.Activity;
import android.util.Log;

import java.util.UUID;

public class BluetoothBuilder {

    private static final String TAG = BluetoothBuilder.class.getName();
    private UUID uuid;
    private Activity activity;

    public BluetoothBuilder(Activity activity, UUID uuid){
        this.activity = activity;
        this.uuid = uuid;
        Log.i(TAG, "A new BluetoothBuilder was created");
    }

    public BluetoothBuilder(Activity activity){
        this(activity, null);
    }

    public BluetoothBuilder setUuid(UUID uuid){
        this.uuid = uuid;
        return this;
    }

    public BluetoothBuilder setUuid(String uuid){
        return setUuid(UUID.fromString(uuid));
    }

    public BluetoothManager build(){
        BluetoothManager manager = BluetoothManager.getInstance();

        manager.setActivity(activity);
        manager.setUuid(uuid);

        if(manager.getActivity() == null){
            Log.e(TAG, "An Activity implementing BluetoothListener is required", new Throwable());
        }
        else{
            Log.i(TAG, "A BluetoothManager was build");
        }
        return manager;
    }
}
