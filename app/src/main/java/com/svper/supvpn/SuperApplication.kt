package com.svper.supvpn

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process.myPid
import android.webkit.WebView
import androidx.lifecycle.MutableLiveData
import com.github.shadowsocks.Core
import com.svper.supvpn.ui.activity.MainActivity
import com.svper.supvpn.utils.CoreManager
import com.svper.supvpn.utils.IpUtil
import com.svper.supvpn.utils.ServersManager
import com.tencent.mmkv.MMKV


lateinit var application: SuperApplication
val MMKVInstance by lazy { MMKV.defaultMMKV() }
val first = MutableLiveData(true)
class SuperApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName()
            if (packageName != processName)
                WebView.setDataDirectorySuffix(processName)
        }

        Core.init(this, MainActivity::class)

        if (processName() != BuildConfig.APPLICATION_ID)
            return

        application = this
        registerActivityLifecycleCallbacks(com.svper.supvpn.utils.ActivityManager)

        MMKV.initialize(this)

        CoreManager.service.connect(this, CoreManager)

        ServersManager.updateConfig("")

        IpUtil.preLoadCode()
    }

    private fun processName(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.runningAppProcesses.firstOrNull { it.pid == myPid() }?.processName ?: BuildConfig.APPLICATION_ID
        }
    }
}