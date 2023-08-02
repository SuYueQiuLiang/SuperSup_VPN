package com.svper.supvpn.utils

import android.os.Message
import androidx.lifecycle.MutableLiveData
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.svper.supvpn.ui.activity.MainActivity
import com.svper.supvpn.ui.activity.MainActivity.Companion.InBlackList
import com.svper.supvpn.ui.activity.MainActivity.Companion.NoNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

object CoreManager : ShadowsocksConnection.Callback {
    val state: MutableLiveData<BaseService.State> = MutableLiveData()
    val service by lazy { ShadowsocksConnection(true) }
    private lateinit var iShadowsocksService: IShadowsocksService
    private var interruptState = false
    private var coreState: BaseService.State = BaseService.State.Idle
        set(value) {
            field = value
            if(interruptState)
                return
            when(value){
                BaseService.State.Connecting,BaseService.State.Stopping -> {}
                else -> state.postValue(value)
            }
        }
    private var connectJob: Job? = null

    fun connectVPN(connectHandler: MainActivity.ConnectHandler) {
        val startTimeStamp = System.currentTimeMillis()
        val endTime = startTimeStamp + 2000
        if(coreState != BaseService.State.Idle && coreState != BaseService.State.Stopped)
            return
        connectJob = MainScope().launch(Dispatchers.IO) {
            if (!ServersManager.hasConnection()) {
                connectHandler.sendMessage(Message.obtain().apply { what = NoNetwork })
                return@launch
            }
            state.postValue(BaseService.State.Connecting)
            if(IpUtil.isInBlackList()){
                connectHandler.sendMessage(Message.obtain().apply { what = InBlackList })
                return@launch
            }
            val server = ServersManager.getSelectServerOrFaster()
            ProfileManager.clear()
            DataStore.profileId = ProfileManager.createProfile(
                Profile(
                    name = server.getName(),
                    host = server.idjkp,
                    remotePort = server.idjkort,
                    password = server.idjkword,
                    method = server.idjkway
                )
            ).id
            while (System.currentTimeMillis() < endTime)
                delay(200)
            ensureActive()
            Core.startService()
        }
    }

    fun interruptConnect(){
        connectJob?.cancel()
        state.postValue(coreState)
    }

    fun tryInterruptConnect() : Boolean?{
        return when (state.value) {
            BaseService.State.Stopping -> {
                connectJob?.cancel()
                state.postValue(coreState)
                true
            }
            BaseService.State.Connecting -> false
            else -> null
        }
    }

    fun disconnectVPN() {
        val startTimeStamp = System.currentTimeMillis()
        val endTime = startTimeStamp + 2000
        if(coreState != BaseService.State.Connected)
            return
        connectJob = MainScope().launch(Dispatchers.IO) {
            state.postValue(BaseService.State.Stopping)
            while (System.currentTimeMillis() < endTime)
                delay(200)
            ensureActive()
            Core.stopService()
        }
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        this.coreState = state
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        iShadowsocksService = service
        coreState = BaseService.State.values()[service.state]
    }
}