package com.rogg.webserver_kt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import spark.Request
import spark.Response
import spark.Route
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

class Service_server : Service() {

    val ip=getIPAddress(true)
    var myHttpServer = MyHttpServer()
    var binder=MyBinder()

    inner class MyBinder : Binder() {
        fun getService(): Service_server = this@Service_server
    }
    fun startServer(){
        myHttpServer.start()
        createNotification(" Http server running on : "+ip+":1337/")
    }
    fun stopServer(){
        myHttpServer.stop()
        cancelNotification(this)
    }
    fun setRoute(body:String){
        myHttpServer.setRoute(body)
    }
    fun createNotification(message: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val CHANNEL_ID="Server_wifi_channel_01"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Server_wifi_channel"
            val descriptionText = "Messages_channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_wifi_tethering_black_24dp)
                .setContentTitle("WifiServer ")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setOngoing(true)

            notificationManager.notify(0, builder.build())
        }
    }
    fun getIPAddress( useIPv4: Boolean):String {
        try {
            var interfaces:List<NetworkInterface>  = Collections.list(NetworkInterface.getNetworkInterfaces());
            for ( intf: NetworkInterface in interfaces) {
                val addrs :List<InetAddress>  = Collections.list(intf.getInetAddresses());
                for (addr : InetAddress in addrs) {
                    if (!addr.isLoopbackAddress()) {
                        val sAddr = addr.getHostAddress()
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':')<0

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return if (delim<0)  sAddr.toUpperCase() else sAddr.substring(0, delim).toUpperCase()
                            }
                        }
                    }
                }
            }
        } catch (ignored: Exception) { } // for now eat exceptions
        return ""
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(0)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
        startServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServer()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}