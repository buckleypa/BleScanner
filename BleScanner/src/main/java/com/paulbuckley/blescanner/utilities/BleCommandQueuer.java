package com.paulbuckley.blescanner.utilities;

import android.bluetooth.BluetoothGatt;
import android.util.Log;

import java.util.LinkedList;

/**
 * Created by paulb on 9/27/13.
 */
public class
BleCommandQueuer
{
    private final String TAG = BleCommandQueuer.class.getSimpleName();

    private LinkedList< BleCommand > mCommandQueue;
    private BluetoothGatt mGatt;
    private boolean mRunning;

    BleCommand lastCommand = null;


    public
    BleCommandQueuer(
            BluetoothGatt gatt
    )
    {
        mGatt = gatt;
        mCommandQueue = new LinkedList< BleCommand >();
        mRunning = false;
    }


    public boolean
    run()
    {
        boolean didRun = false;
        if( !mRunning )
        {
            if( mCommandQueue.peek() != null )
            {
                BleCommand cmd = mCommandQueue.remove();
                mRunning = true;

                Log.d( TAG, "Command run: " + cmd.toString() );
                lastCommand = cmd;

                didRun = cmd.run( mGatt );
                if( !didRun )
                {
                    run();
                }
            }
        }
        return didRun;
    }


    /*
     * Hook this into the BluetoothGattCallback so whenever there is a GATT callback this
     * function is called.
     */
    public boolean
    callComplete()
    {
        Log.d( TAG, "Call completed: " + lastCommand.toString() );

        mRunning = false;
        this.run();

        return true;
    }


    public void
    add(
            BleCommand command
    )
    {
        if( !mCommandQueue.add( command ) )
        {
            Log.d(TAG, "Command queue filled!");
        }

        Log.d( TAG, "Command added: " + command.toString() );
        this.run();
    }
}
