package com.paulbuckley.blescanner.types;

import android.bluetooth.BluetoothGattCharacteristic;
import android.text.format.Time;
import android.view.View;

/**
 * Created by paulb on 8/28/13.
 */
public class
ExtendedBtGattCharacteristic
{
    private BluetoothGattCharacteristic mCharacteristic;
    private Time mReadTime;
    private View mListItemView = null;

    public ExtendedBtGattCharacteristic(
            BluetoothGattCharacteristic characteristic
    )
    {
        this.mCharacteristic = characteristic;
        this.mReadTime = new Time();
        this.mReadTime.setToNow();
    }


    public BluetoothGattCharacteristic
    get()
    {
        return mCharacteristic;
    }


    public void
    wasRead()
    {
        mReadTime.setToNow();
    }


    public Time
    getReadTime()
    {
        return mReadTime;
    }


    public View
    getListItemView()
    {
        return mListItemView;
    }


    public void
    setListItemView (
            View view
    )
    {
        this.mListItemView = view;
    }
}
