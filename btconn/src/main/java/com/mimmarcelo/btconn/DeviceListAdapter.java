/**
 * File name: DeviceListAdapter
 * It is an adapter to create the list of open services in the SelectServiceDialog popup
 */
package com.mimmarcelo.btconn;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

final class DeviceListAdapter extends ArrayAdapter<BluetoothDevice>{

    private List<BluetoothDevice> devices;

    public DeviceListAdapter(Context context, List<BluetoothDevice> devices) {
        super(context, android.R.layout.simple_list_item_single_choice, devices);
        this.devices = devices;
    }

    /**
     * Defines how each list line should be presented
     * @param position Device in the devices list
     * @param convertView Represents the line view of the list
     * @param parent Represents the component view of the list
     * @return The correspondent view of the line
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = devices.get(position);
        DeviceHolder holder;

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_single_choice,parent, false);
            holder = new DeviceHolder((TextView)convertView.findViewById(android.R.id.text1));

            convertView.setTag(holder);
        }
        else {
            holder = (DeviceHolder)convertView.getTag();
        }

        holder.setText(device.getName());
        return convertView;
    }

    @Override
    public BluetoothDevice getItem(int position) {
        try{
            return super.getItem(position);
        }
        catch (IndexOutOfBoundsException e){
            return null;
        }
    }
}
