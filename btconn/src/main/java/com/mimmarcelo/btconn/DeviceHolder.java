/**
 * File name: DeviceHolder
 * Defines a holder for the SelectItemDialog popup list
 */
package com.mimmarcelo.btconn;

import android.widget.TextView;

final class DeviceHolder {

    /* ** Private attributes ** */

    private TextView textView;

    /* ** Constructors ** */

    public DeviceHolder(TextView textView) {
        this.textView = textView;
    }

    /* ** Public methods ** */

    public void setText(String name) {
        this.textView.setText(name);
    }
} // end DeviceHolder class
