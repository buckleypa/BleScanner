package com.paulbuckley.blescanner.ble_standards;

/**
 * Created by paulb on 9/6/13.
 */
public class BleFlags
{
    public boolean limitedDiscoverableMode;
    public boolean generalDiscoverableMode;
    public boolean brEdrNotSupported;
    public boolean simultaneousLeBrEdrToSameDeviceController;
    public boolean simultaneousLeBrEdrToSameDeviceHost;

    private final static byte LE_LIMITED_DISCOVERABLE_MODE = 0x01;
    private final static byte LE_GENERAL_DISCOVERABLE_MODE = 0x02;
    private final static byte BR_EDR_NOT_SUPPORTED = 0x04;
    private final static byte SIM_LE_BR_EDR_TO_SAME_DEV_CONTROLLER = 0x08;
    private final static byte SIM_LE_BR_EDR_TO_SAME_DEV_HOST = 0x10;

    public BleFlags(
            byte data
    )
    {
        limitedDiscoverableMode = ( data & LE_LIMITED_DISCOVERABLE_MODE ) != 0;
        generalDiscoverableMode = ( data & LE_GENERAL_DISCOVERABLE_MODE ) != 0;
        brEdrNotSupported = ( data & BR_EDR_NOT_SUPPORTED ) != 0;
        simultaneousLeBrEdrToSameDeviceController = ( data & SIM_LE_BR_EDR_TO_SAME_DEV_CONTROLLER ) != 0;
        simultaneousLeBrEdrToSameDeviceHost = ( data & SIM_LE_BR_EDR_TO_SAME_DEV_HOST ) != 0;
    }
}
