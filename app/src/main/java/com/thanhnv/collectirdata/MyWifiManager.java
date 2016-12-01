package com.thanhnv.collectirdata;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by thanhnv on 9/20/2016.
 */

public class MyWifiManager {
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;

    public MyWifiManager(Context context){
        wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
    }

    public String getSSID(){
        return wifiInfo.getSSID().replace("\"", "");
    }

    public String getWifiInfo(){
        String detail = "WifiInfo : \n";
        detail +=  "mac address: " + wifiInfo.getMacAddress() + "\n";
        detail +=  "SSID       : " + wifiInfo.getSSID() + "\n";
        detail +=  "ip address : " + wifiInfo.getIpAddress() + "\n";
        detail +=  "hiden ssid : " + wifiInfo.getHiddenSSID() + "\n";
        detail +=  "network id : " + wifiInfo.getNetworkId() + "\n";
        detail +=  "link speed : " + wifiInfo.getLinkSpeed() + "\n";

        return detail;
    }

}
