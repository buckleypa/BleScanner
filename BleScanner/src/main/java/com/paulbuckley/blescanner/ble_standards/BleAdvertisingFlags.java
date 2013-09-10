package com.paulbuckley.blescanner.ble_standards;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paulb on 9/6/13.
 */
public class BleAdvertisingFlags
{
    public final static byte LE_LIMITED_DISCOVERABLE_MODE = 0x01;
    public final static byte LE_GENERAL_DISCOVERABLE_MODE = 0x02;
    public final static byte BR_EDR_NOT_SUPPORTED = 0x04;
    public final static byte SIM_LE_BR_EDR_TO_SAME_DEV_CONTROLLER = 0x08;
    public final static byte SIM_LE_BR_EDR_TO_SAME_DEV_HOST = 0x10;

    private static ArrayList<Pair< Byte, String >> flagNames = new ArrayList<Pair<Byte, String>>();

    static
    {

        flagNames.add( new Pair( LE_LIMITED_DISCOVERABLE_MODE, "LE Limited Discoverable Mode" ) );
        flagNames.add( new Pair( LE_GENERAL_DISCOVERABLE_MODE, "LE General Discoverable Mode" ) );
        flagNames.add( new Pair( BR_EDR_NOT_SUPPORTED, "BR/EDR Not Supported" ) );
        flagNames.add( new Pair( SIM_LE_BR_EDR_TO_SAME_DEV_CONTROLLER, "Simultaneous LE and BR/EDR to Same Device Capable (Controller)" ) );
        flagNames.add( new Pair( SIM_LE_BR_EDR_TO_SAME_DEV_HOST, "Simultaneous LE and BR/EDR to Same Device Capable (Host)" ) );
    }

    public static String
    toString(
            byte mask
    )
    {
        StringBuilder flagsInfo = new StringBuilder();

        for( Pair<Byte, String> flagName : flagNames )
        {
            if( bitMask( flagName.first, mask ) ) flagsInfo.append( flagName.second + "\n" );
        }

        return flagsInfo.toString().trim();
    }


    private static boolean
    bitMask(
            byte flag,
            byte mask
    )
    {
        return ( flag & mask ) != 0;
    }
}
