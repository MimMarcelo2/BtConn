/**
 * File name: ItemListAdapter
 * It is an adapter to create the list of current connections in the SelectConnectionDialog popup
 */
package com.mimmarcelo.btconn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

final class ItemListAdapter extends ArrayAdapter<BluetoothItem> {

    /* ** Private attibutes ** */

    private List<BluetoothItem> connections;

    /* ** Constructors ** */

    public ItemListAdapter(Context context, List<BluetoothItem> connections) {
        super(context, android.R.layout.simple_list_item_single_choice, connections);
        this.connections = connections;
    }

    /* ** Public methods ** */

    /**
     * Defines how each list line should be presented
     *
     * @param position    Device in the connections list
     * @param convertView Represents the line view of the list
     * @param parent      Represents the component view of the list
     * @return The correspondent view of the line
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothItem connection = connections.get(position);
        DeviceHolder holder;

        // if the item list view was not created yet
        if (convertView == null) {
            // gets a layout to item list view
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_single_choice, parent, false);

            // creates the correspondent holder
            holder = new DeviceHolder((TextView) convertView.findViewById(android.R.id.text1));

            //Associates the holder to list item view
            convertView.setTag(holder);
        } // end if convertView == null
        else {
            holder = (DeviceHolder) convertView.getTag();
        }

        holder.setText(connection.getText());
        return convertView;
    } // end of getView method

    @Override
    public BluetoothItem getItem(int position) {
        try {
            return super.getItem(position);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    } // end getItem method
} // end ItemListAdapter class
