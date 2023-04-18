package com.rogg.webserver_kt

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import spark.Spark.*
import spark.Request
import spark.Response
import spark.Route
import spark.Service
import android.text.format.Formatter.formatIpAddress
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.ContextCompat.getSystemService
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.text.format.Formatter
import android.support.v4.content.ContextCompat.getSystemService
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        var responseBody:String=""
    }
    var answer:TextView?=null
    var startButton:Button?=null
    var sendMessage:Button?=null
    var sendMessage1:Button?=null
    var sendMessage2:Button?=null
    var sendMessage3:Button?=null
    var ips:EditText?=null
    var mess:EditText?=null
    var serverstate:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelNotification(this)
        setContentView(R.layout.activity_main)
        mess=findViewById(R.id.message)
        answer=findViewById(R.id.answer)
        sendMessage=findViewById(R.id.sendMessagetoall)
        sendMessage1=findViewById(R.id.sendMessageto1)
        sendMessage2=findViewById(R.id.sendMessageto2)
        sendMessage3=findViewById(R.id.sendMessageto3)
        startButton=findViewById(R.id.start_server)
        val ip=getIPAddress(true)

        var myHttpServer = MyHttpServer()
        startButton!!.setOnClickListener(View.OnClickListener {
            serverstate=!serverstate
            if (serverstate){
                answer!!.text=answer!!.text.toString()+"\n Server Starting ..."
                myHttpServer.start()
                answer!!.text=answer!!.text.toString()+"\n Http server running on : "+ip+":1337/"
                createNotification("Server running ...")
                startButton!!.text="STOP SERVER"
                startButton!!.setBackgroundColor(Color.RED)
            }
            else{
                myHttpServer!!.stop()
                startButton!!.text="START SERVER"
                answer!!.text=answer!!.text.toString()+"\n Server Stopped "
                startButton!!.setBackgroundColor(Color.BLUE)
                cancelNotification(this)
            }
        })
        sendMessage!!.setOnClickListener(View.OnClickListener {
            if (mess!!.text.toString().isEmpty()){
                answer!!.text=answer!!.text.toString()+"\n Empty Message "
            }
            else if(!serverstate){
                answer!!.text=answer!!.text.toString()+"\n Server not started "

            }
            else{
                responseBody="Message#"+mess!!.text.toString()+"#To#"+"*"+"#time#" + (System.currentTimeMillis()/1000).toString()
                myHttpServer.setRoute(responseBody)
                answer!!.text=answer!!.text.toString()+"\n posting message : "+mess!!.text.toString()+"\n To : everyone"
            }
        })
        sendMessage1!!.setOnClickListener(View.OnClickListener {

            if (mess!!.text.toString().isEmpty()){
                answer!!.text=answer!!.text.toString()+"\n Empty Message "
            }
            else if(!serverstate){
                answer!!.text=answer!!.text.toString()+"\n Server not started "

            }
            else{
                responseBody="Message#"+mess!!.text.toString()+"#To#"+"ID0001"+"#time#" + (System.currentTimeMillis()/1000).toString()
                myHttpServer.setRoute(responseBody)
                answer!!.text=answer!!.text.toString()+"\n posting message : "+mess!!.text.toString()+"\n To : ID0001"
            }

        })
        sendMessage2!!.setOnClickListener(View.OnClickListener {
            if (mess!!.text.toString().isEmpty()){
                answer!!.text=answer!!.text.toString()+"\n Empty Message "
            }
            else if(!serverstate){
                answer!!.text=answer!!.text.toString()+"\n Server not started "

            }
            else{
                responseBody="Message#"+mess!!.text.toString()+"#To#"+"ID0002"+"#time#" + (System.currentTimeMillis()/1000).toString()
                myHttpServer.setRoute(responseBody)
                answer!!.text=answer!!.text.toString()+"\n posting message : "+mess!!.text.toString()+"\n To : ID0002"
            }
        })
        sendMessage3!!.setOnClickListener(View.OnClickListener {

            if (mess!!.text.toString().isEmpty()){
                answer!!.text=answer!!.text.toString()+"\n Empty Message "
            }
            else if(!serverstate){
                answer!!.text=answer!!.text.toString()+"\n Server not started "

            }
            else{
                responseBody="Message#"+mess!!.text.toString()+"#To#"+"ID0003"+"#time#" + (System.currentTimeMillis()/1000).toString()
                myHttpServer.setRoute(responseBody)
                answer!!.text=answer!!.text.toString()+"\n posting message : "+mess!!.text.toString()+"\n To : ID0003"
            }

        })

    }
    class MyHttpServer{
        companion object{
            var instance = MyHttpServer()
            var httpService = Service.ignite()
            val port = 1337
        }

        init {
            httpService = Service.ignite()
            httpService.port(port)
            httpService.threadPool(350)
            httpService.internalServerError("Error : 500 internal error")

        }

        fun start() {
            httpService.get("/", MyHttpRoute("Welcome to our discussion server"))
            Log.i("Server","Http server running on : "+port)
        }
        fun setRoute(body: String) {
            stop()
            MyHttpServer()
            httpService.get("/", MyHttpRoute(body))
        }

        fun stop() {httpService.stop()}
    }
    class MyHttpRoute(var body:String) : Route {
        override fun handle(request: Request, response: Response): Any {
            var respBody = body
            response.body(respBody)
            return response.body()
        }
    }

    private fun createNotification(message: String) {
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
            val intent = Intent(this, this::class.java).apply {
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
            for ( intf:NetworkInterface in interfaces) {
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

    fun cancelNotification(context:Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(0)
    }

    override fun onStop() {
        super.onStop()
        cancelNotification(this)
    }
    override fun onRestart() {
        super.onRestart()
        cancelNotification(this)
    }

    override fun onResume() {
        super.onResume()
        if (serverstate)
            createNotification("Server running ...")
    }
}
