package com.paulbuckley.blescanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.paulbuckley.blescanner.activities.ConnectedDeviceActivity;
import com.paulbuckley.blescanner.types.AdvertisementData;
import com.paulbuckley.blescanner.types.AdvertisingBleDevice;
import com.paulbuckley.blescanner.R;
import com.paulbuckley.blescanner.ble_standards.BleAdTypes;

import java.util.ArrayList;

/**
 * Created by paulb on 8/12/13.
 */
public class AdvertisingDevicesAdapter
        extends BaseExpandableListAdapter
{

    ArrayList< AdvertisingBleDevice > mAdvertisers;
    Context context;

    public AdvertisingDevicesAdapter(
            Context context,
            ArrayList< AdvertisingBleDevice > advertisers
    )
    {
        this.context = context;
        this.mAdvertisers = advertisers;
    }

    @Override
    public int
    getGroupCount() {
        return mAdvertisers.size();
    }

    @Override
    public int
    getChildrenCount(
            int groupPosition
    )
    {
        return mAdvertisers.get( groupPosition ).getAdvertisementData().size();
    }


    @Override
    public Object
    getGroup(
            int groupPosition
    )
    {
        return mAdvertisers.get( groupPosition );
    }

    @Override
    public Object
    getChild(
            int groupPosition,
            int childPosition
    )
    {
        return mAdvertisers.get( groupPosition ).getAdvertisementData().get( childPosition );
    }

    @Override
    public long
    getGroupId(
            int groupPosition
    )
    {
        return groupPosition;
    }

    @Override
    public long
    getChildId(
            int groupPosition,
            int childPosition
    )
    {
        return childPosition;
    }

    @Override
    public boolean
    hasStableIds()
    {
        return true;
    }

    @Override
    public View
    getGroupView(
            int groupPosition,
            boolean isExpanded,
            View convertView,
            ViewGroup parent
    )
    {
        AdvertisingBleDevice advertiser = mAdvertisers.get( groupPosition );

        if( convertView == null )
        {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate( R.layout.advertising_device_list_item, null );
        }

        TextView deviceNameTextView = (TextView) convertView.findViewById( R.id.deviceNameTextView );
        TextView deviceAddrTextView = (TextView) convertView.findViewById( R.id.deviceAddrTextView );
        TextView deviceRssiTV = (TextView) convertView.findViewById( R.id.deviceRssiTV );

        setConnectButton( groupPosition, convertView );

        deviceNameTextView.setText( advertiser.device.getName() );
        deviceAddrTextView.setText( "Address: " + advertiser.device.getAddress() );
        deviceRssiTV.setText( "RSSI: " + Integer.toString( advertiser.rssi ) );

        return convertView;
    }


    @Override
    public View
    getChildView(
            final int groupPosition,
            final int childPosition,
            boolean isLastChild,
            View convertView,
            ViewGroup parent
    )
    {
        if( convertView == null )
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate( R.layout.advertising_device_data_list_item, null );
        }

        TextView advertisingDeviceDataName = (TextView) convertView.findViewById( R.id.advertisingDeviceDataName );
        TextView advertisingDeviceDataInfo = (TextView) convertView.findViewById( R.id.advertisingDeviceDataInfo );

        AdvertisementData data = (AdvertisementData) getChild( groupPosition, childPosition );
        advertisingDeviceDataName.setText( data.getName() );

        advertisingDeviceDataInfo.setText( data.toString() );

        // @TODO Add info button that links to online documentation.

        return convertView;
    }


    @Override
    public boolean
    isChildSelectable(
            int groupPosition,
            int childPosition
    )
    {
        return true;
    }

    public void
    clear()
    {
        mAdvertisers.clear();
    }


    private void
    setConnectButton(
            int groupPosition,
            View convertView
    )
    {
        LinearLayout connectButton = (LinearLayout) convertView.findViewById( R.id.connectButton );
        connectButton.setTag( groupPosition );

        connectButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void
                onClick(
                        View v
                )
                {
                    Integer position = (Integer) v.getTag();

                    AdvertisingBleDevice advertiser = (AdvertisingBleDevice) getGroup( position );

                    if( advertiser == null ) return;

                    Intent intent = new Intent( AdvertisingDevicesAdapter.this.context, ConnectedDeviceActivity.class );
                    intent.putExtra( ConnectedDeviceActivity.EXTRAS_DEVICE_NAME, advertiser.device.getName() );
                    intent.putExtra( ConnectedDeviceActivity.EXTRAS_DEVICE_ADDRESS, advertiser.device.getAddress() );

                    AdvertisingDevicesAdapter.this.context.startActivity( intent );
                }
            });
        connectButton.setFocusable( false );
    }

/*
    @Override
    public View
    getView (
            int         position,
            View        convertView,
            ViewGroup   parent
    )
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View rowView = inflater.inflate( R.layout.advertising_device_list_item, parent, false );

        TextView deviceNameTextView = (TextView) rowView.findViewById( R.id.deviceNameTextView );
        TextView deviceAddrTextView = (TextView) rowView.findViewById( R.id.deviceAddrTextView );
        TextView deviceRssiTV = (TextView) rowView.findViewById( R.id.deviceRssiTV );
        TextView deviceScanRecordTV = (TextView) rowView.findViewById( R.id.deviceScanRecordTV );

        AdvertisingBleDevice advertiser = (AdvertisingBleDevice) this.getItem( position );

        // Populate all the fields.
        deviceNameTextView.setText( advertiser.device.getName() );
        deviceAddrTextView.setText( advertiser.device.getAddress() );
        deviceRssiTV.setText( Integer.toString( advertiser.rssi ) + "db" );

        TableLayout advertisingDataTable = (TableLayout) rowView.findViewById( R.id.advertisingDataTable );

        for( Integer adType : advertiser.getPackets().keySet() )
        {
            TableRow tr = new TableRow( rowView.getContext() );
            tr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView categoryTV = new TextView( rowView.getContext() );
            categoryTV.setText( BleAdTypes.getName( adType ) );
            tr.addView(categoryTV);

            TextView categoryDataTV = new TextView( rowView.getContext() );
            byte[] data = advertiser.getPackets().get(adType);

            // Get the value as a hex array
            StringBuilder hexString = new StringBuilder();
            hexString.append( "0x" );
            for( byte hex: data )
            {
                hexString.append( String.format( "%02X-", hex ) );
            }
            hexString.deleteCharAt(hexString.length() - 1);
            categoryDataTV.setText( hexString.toString() );

            tr.addView( categoryDataTV );

            advertisingDataTable.addView( tr );
        }

        return rowView;
    }
    */
}
