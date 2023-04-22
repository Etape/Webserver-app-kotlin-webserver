package com.rogg.webserver_kt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import spark.Request
import spark.Response
import spark.Route
import spark.Service
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.os.Build
import android.os.IBinder
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    companion object {
        var responseBody:String=""
    }
    var devices:ArrayList<Device> = ArrayList()
    var scanned_devices:ArrayList<Device> = ArrayList()
    lateinit var answer:TextView
    lateinit var startButton:Button
    lateinit var startScan:Button
    lateinit var sendMessage:Button
    lateinit var sendMessage1:Button
    lateinit var sendMessage2:Button
    lateinit var sendMessage3:Button
    lateinit var gotoscan:Button
    lateinit var gotomessagery:Button
    lateinit var scanlayout:LinearLayout
    lateinit var messagery:LinearLayout
    lateinit var recycler_scan:RecyclerView
    lateinit var ips:EditText
    lateinit var mess:EditText
    var thread=Thread.currentThread()

    var stop=false

    lateinit var adapter:ViewholderDevice
    var serverstate:Boolean=false
    var scanstate:Boolean=false
    var mBound:Boolean=false
    lateinit var mService: Service_server

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // when the service is connected, get its instance
            val binder = service as Service_server.MyBinder
            mService = binder.getService()
            mService.activity=this@MainActivity
            mService.initialize()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            // service disconnected
            mBound = false
        }
    }
    var scanned=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cancelNotification(this)
        setContentView(R.layout.activity_main)
        mess=findViewById(R.id.message)
        startScan=findViewById(R.id.start_scan)
        answer=findViewById(R.id.answer)
        gotoscan=findViewById(R.id.goto_scan)
        gotomessagery=findViewById(R.id.goto_messagery)
        messagery=findViewById(R.id.messagery_container)
        scanlayout=findViewById(R.id.scan_container)
        sendMessage=findViewById(R.id.sendMessagetoall)
        sendMessage1=findViewById(R.id.sendMessageto1)
        sendMessage2=findViewById(R.id.sendMessageto2)
        sendMessage3=findViewById(R.id.sendMessageto3)
        startButton=findViewById(R.id.start_server)

        recycler_scan=findViewById(R.id.recycler_scan)
        adapter= ViewholderDevice(this,scanned_devices)
        recycler_scan.layoutManager= LinearLayoutManager(this)
        recycler_scan.adapter=adapter

        val ip=getIPAddress(true)
        gotomessagery.setOnClickListener {
            scanlayout.visibility=View.GONE
            messagery.visibility=View.VISIBLE
            if(scanstate){
                scanstate=!scanstate
                mService.stopScan()
                startScan.text="START SCAN"
                startScan.setBackgroundResource(R.drawable.round_shape)
                answer.text=answer.text.toString()+"\n Scan Stopped"
            }
            setContacts()
        }

        gotoscan.setOnClickListener {
            messagery.visibility=View.GONE
            scanlayout.visibility=View.VISIBLE
            if(serverstate){
                scanstate=!scanstate
                mService.stopServer()
                startButton.text="START SERVER"
                answer.text=answer.text.toString()+"\n Server Stopped "
                startButton.setBackgroundColor(Color.BLUE)
            }
        }

        startButton.setOnClickListener(View.OnClickListener {
            serverstate=!serverstate
            if (serverstate && mBound){
                mService.startServer()
                answer.text=answer.text.toString()+"\n Server Starting ..."
                answer.text=answer.text.toString()+"\n Http server running on : "+ip+":1337/"
                startButton.text="STOP SERVER"
                startButton.setBackgroundColor(Color.RED)
            }
            else{
                mService.stopServer()
                startButton.text="START SERVER"
                answer.text=answer.text.toString()+"\n Server Stopped "
                startButton.setBackgroundColor(Color.BLUE)
            }
        })
        startScan.setOnClickListener {
            scanstate=!scanstate
            if(scanstate && mBound){
                stop=true
                mService.startScan()
                answer.text=answer.text.toString()+"\n Scan Starting ..."
                answer.text=answer.text.toString()+"\n Http device scan running on : "+ip+":1337/scan/"

                startScan.text="STOP SCAN"
                startScan.setBackgroundResource(R.drawable.round_shape_off)
            }
            else{
                stop=false
                mService.stopScan()
                startScan.text="START SCAN"
                startScan.setBackgroundResource(R.drawable.round_shape)
                answer.text=answer.text.toString()+"\n Scan Stopped"
            }
        }
        sendMessage.setOnClickListener(View.OnClickListener {
            if (mess.text.toString().isEmpty()){
                answer.text=answer.text.toString()+"\n Empty Message "
            }
            else if(!serverstate){
                answer.text=answer.text.toString()+"\n Server not started "

            }
            else{
                responseBody="Message#"+mess.text.toString()+"#To#"+"*"+"#time#" + (System.currentTimeMillis()/1000).toString()
                mService.setRoute(responseBody)
                answer.text=answer.text.toString()+"\n posting message : "+mess.text.toString()+"\n To : everyone"
            }
        })
        sendMessage1.setOnClickListener(View.OnClickListener {

            if (mess.text.toString().isEmpty()){
                answer.text=answer.text.toString()+"\n Empty Message "
            }
            else if(!serverstate){
                answer.text=answer.text.toString()+"\n Server not started "

            }
            else{
                responseBody="Message#"+mess.text.toString()+"#To#"+devices[0].id+"#time#" + (System.currentTimeMillis()/1000).toString()
                mService.setRoute(responseBody)
                answer.text=answer.text.toString()+"\n posting message : "+mess.text.toString()+"\n To : "+sendMessage1.text
            }
        })

        sendMessage2.setOnClickListener(View.OnClickListener {
            if (mess.text.toString().isEmpty()){
                answer.text=answer.text.toString()+"\n Empty Message "
            }
            else if(!serverstate){
                answer.text=answer.text.toString()+"\n Server not started "

            }
            else{
                responseBody="Message#"+mess.text.toString()+"#To#"+devices[1].id+"#time#" + (System.currentTimeMillis()/1000).toString()
                mService.setRoute(responseBody)
                answer.text=answer.text.toString()+"\n posting message : "+mess.text.toString()+"\n To : "+sendMessage2.text
            }
        })
        sendMessage3.setOnClickListener(View.OnClickListener {

            if (mess.text.toString().isEmpty()){
                answer.text=answer.text.toString()+"\n Empty Message "
            }
            else if(!serverstate){
                answer.text=answer.text.toString()+"\n Server not started "

            }
            else{
                responseBody="Message#"+mess.text.toString()+"#To#"+devices[2].id+"#time#" + (System.currentTimeMillis()/1000).toString()
                mService.setRoute(responseBody)
                answer.text=answer.text.toString()+"\n posting message : "+mess.text.toString()+"\n To : "+sendMessage3.text
            }
        })
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

    override fun onStart() {
        super.onStart()
        val intentBind = Intent(this, Service_server::class.java)
        bindService(intentBind, connection, Context.BIND_AUTO_CREATE)

    }
    override fun onResume() {
        super.onResume()
        if (serverstate)
            mService.createNotification(" Http server running on : "+mService.ip+":1337/")
    }
    fun setContacts(){

        Log.i("Client Http","devices : "+devices.size)
        if(devices.size>=3){
            sendMessage3.visibility=View.VISIBLE
            sendMessage2.visibility=View.VISIBLE
            sendMessage1.visibility=View.VISIBLE
            sendMessage1.text=devices[0].name
            sendMessage2.text=devices[1].name
            sendMessage3.text=devices[2].name
        }
        else if(devices.size==2){
            sendMessage1.visibility=View.VISIBLE
            sendMessage2.visibility=View.VISIBLE
            sendMessage1.text=devices[0].name
            sendMessage2.text=devices[1].name
            sendMessage3.visibility=View.GONE
        }
        else if(devices.size==1){
            sendMessage1.visibility=View.VISIBLE
            sendMessage1.text=devices[0].name
            sendMessage2.visibility=View.GONE
            sendMessage3.visibility=View.GONE
        }
        else{
            sendMessage1.visibility=View.GONE
            sendMessage2.visibility=View.GONE
            sendMessage3.visibility=View.GONE
        }
    }
}
