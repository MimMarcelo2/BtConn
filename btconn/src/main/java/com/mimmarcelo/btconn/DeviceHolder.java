/**
 * File name: DeviceHolder
 * Defines a holder for the SelectServiceDialog popup list
 */
package com.mimmarcelo.btconn;

import android.widget.TextView;

final class DeviceHolder{

    private TextView textView;

    public DeviceHolder(TextView textView) {
        this.textView = textView;
    }

    public void setText(String name){
        this.textView.setText(name);
    }
}
