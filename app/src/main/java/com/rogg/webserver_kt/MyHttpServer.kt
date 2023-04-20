package com.rogg.webserver_kt

import android.util.Log
import spark.Request
import spark.Response
import spark.Route

class MyHttpServer{
    companion object{
        var instance = MyHttpServer()
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
    fun setRoute(body: String) {
        stop()
        MyHttpServer()
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
