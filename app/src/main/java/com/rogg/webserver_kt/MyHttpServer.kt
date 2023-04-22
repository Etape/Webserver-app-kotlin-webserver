package com.rogg.webserver_kt

import android.util.Log
import spark.Request
import spark.Response
import spark.Route

class MyHttpServer(private val activity: MainActivity){
    companion object{
        var httpService = spark.Service.ignite()
        val port = 1337
    }

    init {
        httpService = spark.Service.ignite()
        httpService.port(port)
        httpService.threadPool(350)
        httpService.internalServerError("Error : 500 internal error")

    }

    fun start() {
        httpService.get("/", MyHttpRoute("Welcome to our discussion server"))
        Log.i("Server","Http server running ...")
    }
    fun startScan(){
        httpService.get("/scan/:description", ScanHttpRoute(activity))
        Log.i("Server","Http clients scan running ...")
    }
    fun stopScan(){
        stop()
        Log.i("Server","Http clients scan stopped")
    }
    fun setRoute(body: String) {
        stop()
        MyHttpServer(activity)
        httpService.get("/", MyHttpRoute(body))
    }

    fun stop() {
        httpService.stop()
    }

}
class MyHttpRoute(var body:String) : Route {
    override fun handle(request: Request, response: Response): Any {
        var respBody = body
        response.body(respBody)
        return response.body()
    }
}
class ScanHttpRoute(private val activity: MainActivity) : Route {
    override fun handle(request: Request, response: Response): Any {
        val message:String=request.params(":description") // message style="name#id#ip#dev_name
        var dev=Device()
        val splits=message.split(":")
        Log.i("webserver_scan",message)
        if(splits.size>0){
            dev.name=splits[0]
            dev.id=splits[1]
            dev.ip=splits[2].replace("_",".")
            dev.dev_name=splits[3]
            add_device(dev,activity)
            Log.i("webserver_scan","scanned devices : "+ activity.scanned_devices.size)
        }

        var respBody = "Scanned"
        response.body(respBody)
        return response.body()
    }
    fun add_device(device: Device,activity: MainActivity){
        if(getPosition(device,activity.scanned_devices)==-1){
            activity.scanned_devices.add(device)
            activity.runOnUiThread(Runnable {
                activity.adapter.notifyDataSetChanged()
                activity.setContacts()

            })
        }
    }
    fun getPosition(device: Device,dataSet:ArrayList<Device>):Int{
        for(i in dataSet.indices){
            if(dataSet[i].id==device.id)
                return i
        }
        return -1
    }
}
class Device{
    var name="none"
    var id="none"
    var ip="none"
    var dev_name="none"
}

