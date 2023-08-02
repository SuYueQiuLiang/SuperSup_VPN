package com.svper.supvpn.ui.viewmodel

import android.os.Message
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svper.supvpn.ui.activity.StartActivity
import com.svper.supvpn.ui.activity.StartActivity.Companion.JUMP
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class StartActivityViewModel : ViewModel() {
    val progress : MutableLiveData<Int> = MutableLiveData(0)
    private var delay = 30L
    private var progressJob : Job? = null
    private var startActivityHandler: StartActivity.StartActivityHandler? = null


    private fun endProgress(){
        startActivityHandler?.sendMessage(Message.obtain().apply {
            what = JUMP
        })
    }

    fun startProgress(startActivityHandler : StartActivity.StartActivityHandler){
        this.startActivityHandler = startActivityHandler
        progressJob = viewModelScope.launch {
            while ((progress.value ?: 0) < 100){
                progress.postValue((progress.value ?: 0) + 1)
                delay(delay)
            }
            ensureActive()
            endProgress()
        }
    }

    fun stopProgress(){
        progressJob?.cancel()
        progress.postValue(0)
    }
}