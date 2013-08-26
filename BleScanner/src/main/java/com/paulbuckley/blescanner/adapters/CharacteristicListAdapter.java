package com.paulbuckley.blescanner.adapters;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.paulbuckley.blescanner.R;

import java.util.ArrayList;


public class CharacteristicListAdapter
        extends ArrayAdapter< BluetoothGattCharacteristic >
{

    private final Context mContext;

    private ArrayList< BluetoothGattCharacteristic > mCharacteristics;

    public CharacteristicListAdapter (
            Context context,
            ArrayList< BluetoothGattCharacteristic > characteristics
    )
    {
        super( context, R.layout.characteristic_list_item, characteristics );
        this.mContext = context;
        this.mCharacteristics = characteristics;
    }


    @Override
    public View
    getView (
            int         position,
            View        convertView,
            ViewGroup   parent
    )
    {
        BluetoothGattCharacteristic characteristic = mCharacteristics.get(position);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View rowView = inflater.inflate( R.layout.characteristic_list_item, parent, false );

        // Get the views
        TextView characteristicNameTextView = (TextView) rowView.findViewById( R.id.characteristicNameTextView );

        // Populate all the fields.
        characteristicNameTextView.setText( characteristic.getUuid().toString() );

        return rowView;
    }
}

