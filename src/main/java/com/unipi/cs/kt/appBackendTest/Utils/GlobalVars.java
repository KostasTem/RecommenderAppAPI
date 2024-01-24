package com.unipi.cs.kt.appBackendTest.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalVars {
    public static final int BASE_RECOMMENDATION_APP_COUNT = 16;
    public static final List<String> excludedApps = Arrays.asList("YouTube", "Google App", "Files", "Messages", "Contacts", "Camera", "Clock", "Gmail", "Duo", "Google Play Music", "Phone", "Drive", "Maps", "Chrome", "Google Play Movies & TV", "WebView Shell", "Photos", "Calendar", "Settings", "Calculator", "Wallpapers", "Custom Locale", "Dev Tools","Facebook","Twitter","Twitch","Yahoo Finance","Spotify","Reddit","Steam","Netflix","Microsoft Teams");
    public static final List<String> settings =  Arrays.asList("ACCESSIBILITY_ENABLED","BLUETOOTH_ON","ADB_ENABLED","USB_MASS_STORAGE_ENABLED","isDeviceSecure","isDataEnabled");
    public static List<Integer> percentageOfUsersWithSetting = Arrays.asList(0,0,0,0,0,0,0);
}
