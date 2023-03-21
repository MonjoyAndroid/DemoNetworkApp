package com.example.demonetworkapp.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.example.demonetworkapp.utils.CommonUtils
import java.lang.reflect.Field
import java.lang.reflect.Method


class ConnectionReceiver : BroadcastReceiver() {
    private val tagLog = javaClass.simpleName as String
    private var connectionFlag = 5 //for No Network

    fun checkConnectivityStatus(context: Context): String? {
        var networkStatus = ""

        // Get connect mangaer
        val connMgr = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        // check for wifi
        val wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        // check for mobile data
        val mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        networkStatus = if (wifi!!.isAvailable) {
            "wifi"
        } else if (mobile!!.isAvailable) {
            "mobileData"
        } else {
            "noNetwork"
        }
        return networkStatus
    }


    override fun onReceive(context: Context, intent: Intent) {

        if(checkConnectivityStatus(context) == "wifi"){
//            CommonUtils.setMobileDataEnabled(context, false)
            toggleMobileDataConnection(context, false)
            connectionFlag = CommonUtils.scanWIFI(context)
        } else if(checkConnectivityStatus(context) == "mobileData"){
            toggleMobileDataConnection(context, false)
            connectionFlag = 6
//            CommonUtils.setMobileDataEnabled(context, false)
        } else{
            toggleMobileDataConnection(context, false)
            connectionFlag = 5
//            CommonUtils.setMobileDataEnabled(context, false)
        }

        // initialize connectivity manager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Initialize network info
        val networkInfo = connectivityManager.activeNetworkInfo

        // check condition
        if (Listener != null) {

            // when connectivity receiver
            // listener  not null
            // get connection status
            val isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting
            if(!isConnected) connectionFlag = 5

            // call listener method
            Listener!!.onNetworkChange(isConnected, connectionFlag)
        }
    }

    interface ReceiverListener {
        // create method
        fun onNetworkChange(isConnected: Boolean, connectionFlag: Int)
    }

    companion object {
        // initialize listener
        var Listener: ReceiverListener? = null
    }


    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
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

    fun toggleMobileDataConnection(context: Context, ON: Boolean): Boolean {
        try {
            // create instance of connectivity manager and get system service
            val conman = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            // define instance of class and get name of connectivity manager
            // system service class
            val conmanClass = Class
                .forName(conman.javaClass.name)
            // create instance of field and get mService Declared field
            val iConnectivityManagerField: Field = conmanClass.getDeclaredField("mService")
            // Attempt to set the value of the accessible flag to true
            iConnectivityManagerField.setAccessible(true)
            // create instance of object and get the value of field conman
            val iConnectivityManager: Any = iConnectivityManagerField.get(conman)!!
            // create instance of class and get the name of iConnectivityManager
            // field
            val iConnectivityManagerClass = Class
                .forName(iConnectivityManager.javaClass.name)
            // create instance of method and get declared method and type
            val setMobileDataEnabledMethod: Method = iConnectivityManagerClass
                .getDeclaredMethod("setMobileDataEnabled", java.lang.Boolean.TYPE)
            // Attempt to set the value of the accessible flag to true
            setMobileDataEnabledMethod.setAccessible(true)
            // dynamically invoke the iConnectivityManager object according to
            // your need (true/false)
            setMobileDataEnabledMethod.invoke(iConnectivityManager, ON)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return true
    }

}