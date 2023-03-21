package com.example.demonetworkapp.utils

import android.R
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.demonetworkapp.interfaces.ConnectivityPopupActionListener
import com.example.demonetworkapp.interfaces.NetworkEnum

class NetworkAlertDialog(context: Context) : AlertDialog.Builder(context) {
    var builder: AlertDialog.Builder ? = null
    private var alertDialog: AlertDialog? = null

    fun showAlertForConnectivity(connectivityPopupActionListener: ConnectivityPopupActionListener, connectivityFlag: Int) {

        var msg = ""
        when (connectivityFlag) {
            NetworkEnum.NetworkType.WEP_CONNECTION.code -> {
                msg=  "Your network security type is WEP, Its Secure network, Go Ahead"
            }
            NetworkEnum.NetworkType.WPA_CONNECTION.code -> {
                msg=  "Your network security type is WPA, Its Secure network, Go Ahead"
            }
            NetworkEnum.NetworkType.WPA2_CONNECTION.code -> {
                msg= "Your network security type is WPA2, Its Secure network, Go Ahead"
            }
            NetworkEnum.NetworkType.WPA3_CONNECTION.code -> {
                msg= "Your network security type is WPA3, Its Secure network, Go Ahead"
            }
            NetworkEnum.NetworkType.UNKNOWN_CONNECTION.code -> {
                msg= "Your network security type is Unknown, Its less Secure network, Closing Application"
            }
            NetworkEnum.NetworkType.NO_CONNECTION.code -> {
                msg=  "You are not connected to internet, Please connect to internet"
            }
            NetworkEnum.NetworkType.MOBILE_CONNECTION.code -> {
                msg= "Your are connected to Mobile data, Go ahead"
            }
            else -> { // Note the block
                msg= "Your network security type is Unknown, Its less Secure network, Closing Application"
            }
        }

        if (builder==null){
            builder = AlertDialog.Builder(context)
        }

        builder!!.setTitle("Network Alert")
            .setMessage(msg)
            .setPositiveButton(R.string.ok) { dialog, which ->

                if(connectivityFlag == 4){
                    connectivityPopupActionListener.onActionButtonClick(true)
                }else{
                    connectivityPopupActionListener.onActionButtonClick(false)
                }

                dialog.dismiss()
            }
        builder!!.setIcon(R.drawable.ic_dialog_alert)

        // Create the AlertDialog
        alertDialog?.dismiss()
        alertDialog = builder!!.create()

        // Set other dialog properties
        alertDialog!!.setCancelable(false)

        alertDialog!!.show()
    }


    fun dismissDialog(){
        if(builder!=null){
            alertDialog?.dismiss()
            builder = null
        }
    }
}