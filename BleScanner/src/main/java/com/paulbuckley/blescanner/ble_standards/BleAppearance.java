package com.paulbuckley.blescanner.ble_standards;

import java.util.HashMap;

/**
 * Created by paulb on 9/10/13.
 *
 * Values based on the Appearance characteristic, shown here:
 * https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.gap.appearance.xml
 */
public class
BleAppearance
{
    public static final int UNKNOWN = 0;
    public static final int GENERIC_PHONE = 64;
    public static final int GENERIC_COMPUTER = 128;
    public static final int GENERIC_WATCH = 192;
    public static final int SUBTYPE_WATCH_SPORTS = 193;
    public static final int GENERIC_CLOCK = 256;
    public static final int GENERIC_DISPLAY = 320;
    public static final int GENERIC_REMOTE_CONTROL = 384;
    public static final int GENERIC_EYE_GLASSES = 448;
    public static final int GENERIC_TAG = 512;
    public static final int GENERIC_KEYRING = 576;
    public static final int GENERIC_MEDIA_PLAYER = 640;
    public static final int GENERIC_BARCODE_SCANNER = 704;
    public static final int GENERIC_THERMOMETER = 768;
    public static final int SUBTYPE_THERMOMETER_EAR = 769;
    public static final int GENERIC_HEART_RATE_SENSOR = 832;
    public static final int SUBTYPE_HEART_RATE_SENSOR_BELT = 833;
    public static final int GENERIC_BLOOD_PRESSURE = 896;
    public static final int SUBTYPE_BLOOD_PRESSURE_ARM = 897;
    public static final int SUBTYPE_BLOOD_PRESSURE_WRIST = 898;
    public static final int GENERIC_HID = 960;
    public static final int SUBTYPE_HID_KEYBOARD = 961;
    public static final int SUBTYPE_HID_MOUSE = 962;
    public static final int SUBTYPE_HID_JOYSTICK = 963;
    public static final int SUBTYPE_HID_GAMEPAD = 964;
    public static final int SUBTYPE_HID_DIGITALIZER_TABLET = 965;
    public static final int SUBTYPE_HID_CARD_READER = 966;
    public static final int SUBTYPE_HID_DIGITAL_PEN = 967;
    public static final int SUBTYPE_BARCODE_SCANNER = 968;
    public static final int GENERIC_GLUCOSE_METER = 1024;
    public static final int GENERIC_RUNNING_WALKING_SENSOR = 1088;
    public static final int SUBTYPE_RUNNING_WALKING_SENSOR_IN_SHOE = 1089;
    public static final int SUBTYPE_RUNNING_WALKING_SENSOR_ON_SHOE = 1090;
    public static final int SUBTYPE_RUNNING_WALKING_SENSOR_ON_HIP = 1091;
    public static final int GENERIC_CYCLING = 1152;
    public static final int SUBTYPE_CYCLING_COMPUTER = 1153;
    public static final int SUBTYPE_CYCLING_SPEED_SENSOR = 1154;
    public static final int SUBTYPE_CYCLING_CADENCE_SENSOR = 1155;
    public static final int SUBTYPE_CYCLING_POWER_SENSOR = 1156;
    public static final int SUBTYPE_CYCLING_SPEED_AND_CADENCE_SENSOR = 1157;
    public static final int GENERIC_PULSE_OXIMETER = 3136 ;
    public static final int SUBTYPE_PULSE_OXIMETER_FINGERTIP = 3137;
    public static final int SUBTYPE_PULSE_OXIMTER_WRIST_WORN = 3138;
    public static final int GENERIC_OUTDOOR_SPORTS = 5184;
    public static final int SUBTYPE_OUTDOOR_SPORTS_LOCATION_DISPLAY_DEVICE = 5185;
    public static final int SUBTYPE_OUTDOOR_SPORTS_LOCATION_AND_NAVIGATION_DISPLAY_DEVICE = 5186;
    public static final int SUBTYPE_OUTDOOR_SPORTS_LOCATION_POD = 5187;
    public static final int SUBTYPE_OUTDOOR_SPORTS_LOCATION_AND_NAVIGATION_POD = 5188;

    private static HashMap< Integer, String > appearanceName = new HashMap<Integer, String>();

    static
    {
        appearanceName.put( UNKNOWN, "Unknown" );
        appearanceName.put( GENERIC_PHONE, "Phone (Generic)" );
        appearanceName.put( GENERIC_COMPUTER, "Computer (Generic)" );
        appearanceName.put( GENERIC_WATCH, "Watch (Generic)" );
        appearanceName.put( SUBTYPE_WATCH_SPORTS, "Watch (Sports)" );
        appearanceName.put( GENERIC_CLOCK, "Clock (Generic)" );
        appearanceName.put( GENERIC_DISPLAY, "Display (Generic)" );
        appearanceName.put( GENERIC_REMOTE_CONTROL, "Remote Control (Generic)" );
        appearanceName.put( GENERIC_EYE_GLASSES, "Eye Glasses  (Generic)" );
        appearanceName.put( GENERIC_TAG, "Tag (Generic)" );
        appearanceName.put( GENERIC_KEYRING, "Keyring (Generic)" );
        appearanceName.put( GENERIC_MEDIA_PLAYER, "Media Player (Generic)" );
        appearanceName.put( GENERIC_BARCODE_SCANNER, "Barcode Scanner (Generic)" );
        appearanceName.put( GENERIC_THERMOMETER, "Thermometer (Generic)" );
        appearanceName.put( SUBTYPE_THERMOMETER_EAR, "Thermometer (Ear)" );
        appearanceName.put( GENERIC_HEART_RATE_SENSOR, "Heart Rate Sensor (Generic)" );
        appearanceName.put( SUBTYPE_HEART_RATE_SENSOR_BELT, "Heart Rate Sensor (Belt)" );
        appearanceName.put( GENERIC_BLOOD_PRESSURE, "Blood Pressure (Generic)" );
        appearanceName.put( SUBTYPE_BLOOD_PRESSURE_ARM, "Blood Pressure (Arm)" );
        appearanceName.put( SUBTYPE_BLOOD_PRESSURE_WRIST, "Blood Pressure (Wrist)" );
        appearanceName.put( GENERIC_HID, "Human Interface Device (Generic)" );
        appearanceName.put( SUBTYPE_HID_KEYBOARD, "HID (Keyboard)" );
        appearanceName.put( SUBTYPE_HID_MOUSE, "HID (Mouse)" );
        appearanceName.put( SUBTYPE_HID_JOYSTICK, "HID (Joystick)" );
        appearanceName.put( SUBTYPE_HID_GAMEPAD, "HID (Gamepad)" );
        appearanceName.put( SUBTYPE_HID_DIGITALIZER_TABLET, "HID (Digitalizer Tablet)" );
        appearanceName.put( SUBTYPE_HID_CARD_READER, "HID (Card Reader)" );
        appearanceName.put( SUBTYPE_HID_DIGITAL_PEN, "HID (Digital Pen)" );
        appearanceName.put( SUBTYPE_BARCODE_SCANNER, "HID (Barcode Scanner)" );
        appearanceName.put( GENERIC_GLUCOSE_METER, "Glucose Meter (Generic)" );
        appearanceName.put( GENERIC_RUNNING_WALKING_SENSOR, "Running/Walking Sensor (Generic)" );
        appearanceName.put( SUBTYPE_RUNNING_WALKING_SENSOR_IN_SHOE, "Running/Walking Sensor (In Shoe)" );
        appearanceName.put( SUBTYPE_RUNNING_WALKING_SENSOR_ON_SHOE, "Running/Walking Sensor (On Shoe)" );
        appearanceName.put( SUBTYPE_RUNNING_WALKING_SENSOR_ON_HIP, "Running/Walking Sensor (On Hip)" );
        appearanceName.put( GENERIC_CYCLING, "Cycling (Generic)" );
        appearanceName.put( SUBTYPE_CYCLING_COMPUTER, "Cycling (Computer)" );
        appearanceName.put( SUBTYPE_CYCLING_SPEED_SENSOR, "Cycling (Speed Sensor)" );
        appearanceName.put( SUBTYPE_CYCLING_CADENCE_SENSOR, "Cycling (Cadence Sensor)" );
        appearanceName.put( SUBTYPE_CYCLING_POWER_SENSOR, "Cycling (Power Sensor)" );
        appearanceName.put( SUBTYPE_CYCLING_SPEED_AND_CADENCE_SENSOR, "Cycling (Speed and Cadence Sensor)" );
        appearanceName.put( GENERIC_PULSE_OXIMETER, "Pulse Oximeter (Generic)" );
        appearanceName.put( SUBTYPE_PULSE_OXIMETER_FINGERTIP, "Pulse Oximiter (Fingertip)" );
        appearanceName.put( SUBTYPE_PULSE_OXIMTER_WRIST_WORN, "Pulse Oximiter (Wrist Worn)" );
        appearanceName.put( GENERIC_OUTDOOR_SPORTS, "Outdoor Sports Activity (Generic)" );
        appearanceName.put( SUBTYPE_OUTDOOR_SPORTS_LOCATION_DISPLAY_DEVICE, "Outdoor Sports Activity (Location Display Device)" );
        appearanceName.put( SUBTYPE_OUTDOOR_SPORTS_LOCATION_AND_NAVIGATION_DISPLAY_DEVICE, "Outdoor Sports Activity (Location and Navigation Display Device)" );
        appearanceName.put( SUBTYPE_OUTDOOR_SPORTS_LOCATION_POD, "Outdoor Sports Activity (Location Pod)" );
        appearanceName.put( SUBTYPE_OUTDOOR_SPORTS_LOCATION_AND_NAVIGATION_POD, "Outdoor Sports Activity (Location and Navigation Pod)" );
    }


    public static String
    toString(
            int appearance
    )
    {
        if( !appearanceName.containsKey( appearance ) )
        {
            appearance = UNKNOWN;
        }

        return appearanceName.get( appearance );
    }
}
