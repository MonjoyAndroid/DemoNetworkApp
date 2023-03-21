package com.example.demonetworkapp.utils

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.demonetworkapp.interfaces.ConnectivityPopupActionListener
import com.example.demonetworkapp.interfaces.NetworkEnum
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


object CommonUtils {

    private val tagLog = javaClass.simpleName as String

    fun checkAndRequestPermissions(context: Context, activity: Activity, reqPermission: Int): Boolean {
        val internet = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)
        val coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val listPermissionsNeeded: MutableList<String> = ArrayList()

        if (internet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET)
        }
        if (fineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (coarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                listPermissionsNeeded.toTypedArray<String>(),
                reqPermission
            )
            return false
        }
        return true
    }


    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT < 23) {
            val ni = cm.activeNetworkInfo
            if (ni != null) {
                return ni.isConnected && (ni.type == ConnectivityManager.TYPE_WIFI || ni.type == ConnectivityManager.TYPE_MOBILE)
            }
        } else {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
        }
        return false
    }


    fun scanWIFI(context: Context): Int{
        var flag = 5 //for insecure connection
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        val networkList: List<ScanResult>? = wifi!!.scanResults

        //get current connected SSID for comparison to ScanResult
        val wi = wifi.connectionInfo

        val currentSSID = wi.ssid.substring(1, wi.ssid.length - 1)
        val currentBSSID = wi.bssid
        if (networkList != null) {
            for (network in networkList) {
                //check if current connected SSID
                if (currentSSID == network.SSID && currentBSSID == network.BSSID) {
                    //get capabilities of current connection
                    val capabilities: String = network.capabilities
                    Log.d(tagLog, network.SSID + " capabilities : " + capabilities)
                    if (capabilities.contains("WPA3")) {
                        //do something
                        flag = 3
                        Log.d(tagLog, "WPA3 connection established")
                    } else if (capabilities.contains("WPA2")) {
                        flag = 2
                        Log.d(tagLog, "WPA2 connection established")
                    } else if (capabilities.contains("WPA")) {
                        flag = 1
                        Log.d(tagLog, "WPA connection established")
                    } else if (capabilities.contains("WEP")) {
                        flag = 0
                        Log.d(tagLog, "WEP connection established")
                    }else{
                        flag = 4
                        Log.d(tagLog, "Unknown connection established")
                    }
                }
            }
        }
        return flag
    }


    @Throws(
        ClassNotFoundException::class,
        NoSuchFieldException::class,
        IllegalAccessException::class,
        NoSuchMethodException::class,
        InvocationTargetException::class
    )
    fun setMobileDataEnabled(context: Context, enabled: Boolean) {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        val methodSet: Method = Class.forName(tm!!.javaClass.name)
            .getDeclaredMethod("setDataEnabled", java.lang.Boolean.TYPE)
        methodSet.invoke(tm, enabled)

    }

}