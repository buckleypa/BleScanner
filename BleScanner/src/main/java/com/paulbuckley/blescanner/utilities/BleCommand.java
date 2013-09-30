package com.paulbuckley.blescanner.utilities;

import android.bluetooth.BluetoothGatt;

/**
 * Created by paulb on 9/27/13.
 */
public interface
BleCommand
{

    /***********************************************************************************************
     * Called when the command is ready to be run.
     */
    public boolean
    run(
            BluetoothGatt gatt
    );


    /***********************************************************************************************
     * String representation of the command.
     */
    public String
    toString();
}
