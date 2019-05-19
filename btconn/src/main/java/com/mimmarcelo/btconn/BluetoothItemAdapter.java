package com.mimmarcelo.btconn;

import android.bluetooth.BluetoothDevice;

class BluetoothItemAdapter implements BluetoothItem {
    BluetoothDevice device;

    public BluetoothItemAdapter(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public String getText() {
        return device.getName();
    }

    @Override
    public Object getInstance() {
        return device;
    }

}
