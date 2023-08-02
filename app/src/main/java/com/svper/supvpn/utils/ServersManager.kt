package com.svper.supvpn.utils

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.gson.Gson
import com.svper.supvpn.MMKVInstance
import com.svper.supvpn.R
import com.svper.supvpn.application
import com.svper.supvpn.beans.MyServer
import com.svper.supvpn.beans.MyServerList
import com.svper.supvpn.beans.PingInfo
import java.io.BufferedReader
import java.text.DecimalFormat
import java.util.LinkedList
import java.util.Random
import kotlin.math.roundToInt

object ServersManager {
    val serverList = MyServerList()

    val fastServer by lazy { MyServer(idjktry = "Faster Server") }

    var selectServer: MyServer
        get() {
            val temp = MMKVInstance.decodeParcelable("selectServer", MyServer::class.java) ?: fastServer
            return if (serverList.contains(temp))
                temp
            else {
                MMKVInstance.remove("selectServer")
                fastServer
            }
        }
        set(value) {
            MMKVInstance.encode("selectServer", value)
        }

    var connectServer: MyServer?
        get() = MMKVInstance.decodeParcelable("connectServer", MyServer::class.java)
        set(value) {
            MMKVInstance.encode("connectServer", value)
        }

    val connectTime
        get() = run {
            val currentTimeStamp = System.currentTimeMillis()
            val startTimeStamp = MMKVInstance.decodeLong("connectTime", System.currentTimeMillis())
            val passedSeconds = (currentTimeStamp - startTimeStamp) / 1000
            val passedHours = passedSeconds / 60 / 60 % 100
            val passedMinutes = passedSeconds / 60 % 60
            val passedSecond = passedSeconds % 60
            val decimalFormat = DecimalFormat("00")
            application.getString(R.string.connect_time_format)
                .format(decimalFormat.format(passedHours), decimalFormat.format(passedMinutes), decimalFormat.format(passedSecond))
        }

    val connectTimeLong
        get() = run {
            val currentTimeStamp = System.currentTimeMillis()
            val startTimeStamp = MMKVInstance.decodeLong("connectTime", System.currentTimeMillis())
            (currentTimeStamp - startTimeStamp) / 1000
        }

    var MMKVConfig
        get() = MMKVInstance.decodeString("MMKVConfig") ?: ""
        set(value) {
            MMKVInstance.encode("MMKVConfig", value)
        }

    val localConfig
        get() = application.assets.open("myserver.json").bufferedReader().use(BufferedReader::readText)

    val isConnect
        get() = MMKVInstance.containsKey("connectTime")

    fun onConnect(){
        connectServer = selectServer
        MMKVInstance.encode("connectTime", System.currentTimeMillis())
    }

    fun onDisconnect(){
        connectServer = null
        MMKVInstance.remove("connectTime")
    }

    private fun chooseCFG(cfg: String): MyServerList {
        val json = if (cfg != "") {
            MMKVConfig = cfg
            cfg
        } else if (MMKVConfig != "") MMKVConfig
        else localConfig
        return Gson().fromJson(json, MyServerList::class.java)
    }

    fun updateConfig(cfg: String) {
        synchronized(serverList) {
            serverList.clear()
            serverList.add(fastServer)
            serverList.addAll(chooseCFG(cfg))
        }
    }

    @SuppressLint("DiscouragedApi")
    fun getFlagDrawableId(server: MyServer): Int {
        var id =
            application.resources.getIdentifier((server.idjktry ?: "").lowercase().replace(" ", ""), "drawable", application.packageName)
        if (id == 0)
            id = application.resources.getIdentifier(
                (fastServer.idjktry ?: "").lowercase().replace(" ", ""),
                "drawable",
                application.packageName
            )
        return id
    }

    fun getSelectServerOrFaster(): MyServer {
        if (serverList.contains(selectServer) && selectServer != fastServer)
            return selectServer
        val nodeList = LinkedList<MyServer>().apply {
            addAll(serverList)
            remove(fastServer)
        }
        val pingInfoList = mutableListOf<PingInfo>()
        for (server in nodeList) {
            val ping = getPing(server.idjkp ?: "")
            if (ping != Int.MAX_VALUE)
                pingInfoList.add(PingInfo(ping, server))
        }
        if (pingInfoList.size <= 0)
            return nodeList.random()
        if (pingInfoList.size <= 3)
            return pingInfoList.random().server
        return pingInfoList[Random().nextInt(3)].server
    }

    fun hasConnection(): Boolean {
        val connectivityManager = application.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.let {
            val activeNetwork = it.activeNetwork ?: return false
            val cap = it.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                cap.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        }
    }

    private fun getPing(host: String): Int {
        return try {
            val str = "/system/bin/ping -c 1 -w 1 $host"
            val runTime = Runtime.getRuntime()
            val returnInfo = runTime.exec(str).inputStream.bufferedReader().use(BufferedReader::readText)
            if (returnInfo.contains("min/avg/max/mdev").not())
                return Int.MAX_VALUE
            returnInfo.substring(returnInfo.indexOf("min/avg/max/mdev") + 19).split("/").toTypedArray()[1].toFloat().roundToInt()
        } catch (e: Exception) {
            Int.MAX_VALUE
        }
    }
}