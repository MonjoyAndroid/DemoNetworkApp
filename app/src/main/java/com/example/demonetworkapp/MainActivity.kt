package com.example.demonetworkapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.demonetworkapp.interfaces.ConnectivityPopupActionListener
import com.example.demonetworkapp.network.ConnectionReceiver
import com.example.demonetworkapp.utils.CommonUtils
import com.example.demonetworkapp.utils.NetworkAlertDialog
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), ConnectionReceiver.ReceiverListener, ConnectivityPopupActionListener {
    private var imgNoConnection: ImageView? = null
    lateinit var context: Context
    private var mConnectivityReceiver: BroadcastReceiver? = null
    private var webView: WebView? = null
    private var connectivityFlag = 5 //No Connection
    private var connectivityPopupActionListener: ConnectivityPopupActionListener? =null
    private var networkAlertDialog: NetworkAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        if(networkAlertDialog==null) {
            networkAlertDialog = NetworkAlertDialog(context)
        }
        initView()
        // Initialize listener
        mConnectivityReceiver = ConnectionReceiver()
        registerConnectionReceiver()
        ConnectionReceiver.Listener = this
        connectivityPopupActionListener = this

    }

    private fun initView(){
        webView = findViewById<WebView>(R.id.webView1)
        webView!!.getSettings().setJavaScriptEnabled(true)
        imgNoConnection = findViewById<ImageView>(R.id.img_no_connection)
    }

    private fun checkAllInternetConnectivity(){
        if(CommonUtils.checkAndRequestPermissions(context, this@MainActivity, MainActivity.REQUEST_INTERNET_MULTIPLE_PERMISSIONS)){
            if(checkConnectivityStatus(context) == "wifi"){
                toggleMobileDataConnection(false)
                connectivityFlag = CommonUtils.scanWIFI(context)
            } else if(checkConnectivityStatus(context) == "mobileData"){
                toggleMobileDataConnection(false)
                connectivityFlag = 6 //Mobile Network
            } else{
                toggleMobileDataConnection(false)
                connectivityFlag = 5 //No Network
            }
            networkAlertDialog!!.showAlertForConnectivity(connectivityPopupActionListener!!, connectivityFlag)

        }else{
            CommonUtils.checkAndRequestPermissions(context, this@MainActivity, MainActivity.REQUEST_INTERNET_MULTIPLE_PERMISSIONS)
        }
    }


//    private fun showSnackBar(isConnected: Boolean) {
//
//        // initialize color and message
//        val message: String
//        val color: Int
//
//        // check condition
//        if (isConnected) {
//
//            // when internet is connected
//            // set message
//            message = "Connected to Internet"
//
//            // set text color
//            color = Color.WHITE
//        } else {
//
//            // when internet
//            // is disconnected
//            // set message
//            message = "Not Connected to Internet"
//
//            // set text color
//            color = Color.RED
//        }
//
//        // initialize snack bar
//        val snackbar =
//            Snackbar.make(findViewById<View>(R.id.btn_check), message, Snackbar.LENGTH_LONG)
//
//        // initialize view
//        val view: View = snackbar.view
//
//        // Assign variable
//        val textView: TextView = view.findViewById(R.id.snackbar_text)
//
//        // set text color
//        textView.setTextColor(color)
//
//        // show snack bar
//        snackbar.show()
//    }

    override fun onNetworkChange(isConnected: Boolean, connectionFLag: Int) {
        connectivityFlag = connectionFLag
        networkAlertDialog!!.showAlertForConnectivity(connectivityPopupActionListener!!, connectivityFlag)
        // display snack bar
//        showSnackBar(isConnected)
    }


    override fun onResume() {
        super.onResume()
        // call method
        checkAllInternetConnectivity()
//        networkAlertDialog!!.showAlertForConnectivity(connectivityPopupActionListener!!, connectivityFlag)
    }


    override fun onDestroy() {
        super.onDestroy()
        if(mConnectivityReceiver != null)
           unregisterConnectionReceiver()
    }

    fun loadWebView() {
        webView!!.loadUrl("https://www.google.com")
//        webView!!.webChromeClient = object : WebChromeClient() {
//            override fun onProgressChanged(view: WebView, progress: Int) {
//                this@MainActivity.setTitle("Loading...")
//                this@MainActivity.setProgress(progress * 100)
//                if (progress == 100) {
//
//                }
//            }
//        }
    }

    private fun registerConnectionReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(
                mConnectivityReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(
                mConnectivityReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    private fun unregisterConnectionReceiver() {
        try {
            unregisterReceiver(mConnectivityReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }


    companion object {
        private const val REQUEST_INTERNET_MULTIPLE_PERMISSIONS = 11
    }


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


    fun isMobileDataEnable(): Boolean {
        var mobileDataEnabled = false // Assume disabled
        val cm = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            val cmClass = Class.forName(cm.javaClass.name)
            val method = cmClass.getDeclaredMethod("getMobileDataEnabled")
            method.isAccessible = true // method is callable
            // get the setting for "mobile data"
            mobileDataEnabled = method.invoke(cm) as Boolean
        } catch (e: Exception) {
            // Some problem accessible private API and do whatever error
            // handling here as you want..
        }
        return mobileDataEnabled
    }

    fun toggleMobileDataConnection(ON: Boolean): Boolean {
        try {
            // create instance of connectivity manager and get system service
            val conman = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
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

    override fun onActionButtonClick(isBlockApp: Boolean) {
        if(isBlockApp){
            closeApplication()
        } else {
            if (CommonUtils.isNetworkConnected(context)) {
                webView!!.visibility = View.VISIBLE
                imgNoConnection!!.visibility = View.GONE

                loadWebView()
            } else {
                webView!!.visibility = View.GONE
                imgNoConnection!!.visibility = View.VISIBLE
            }
        }

    }

    fun closeApplication(){
        finish();
        exitProcess(0)
    }

}