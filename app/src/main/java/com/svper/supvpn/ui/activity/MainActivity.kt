package com.svper.supvpn.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.github.shadowsocks.bg.BaseService
import com.svper.supvpn.R
import com.svper.supvpn.beans.MyServer
import com.svper.supvpn.databinding.ActivityMainBinding
import com.svper.supvpn.first
import com.svper.supvpn.ui.viewmodel.MainActivityViewModel
import com.svper.supvpn.utils.ActivityManager
import com.svper.supvpn.utils.CoreManager
import com.svper.supvpn.utils.IpUtil
import com.svper.supvpn.utils.ServersManager
import com.svper.supvpn.utils.startActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class MainActivity : BaseActivity<ActivityMainBinding, MainActivityViewModel>(), OnClickListener {
    override val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override val viewModel: MainActivityViewModel by viewModels()
    private var active = false
    private val connectHandler by lazy { ConnectHandler(WeakReference(this)) }
    private var updateConnectTimeJob: Job? = null
    private var progressJob: Job? = null

    class ConnectHandler(private val wActivity: WeakReference<MainActivity>) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                InBlackList -> wActivity.get()?.policyAlert()
                NoNetwork -> wActivity.get()?.networkAlert()
            }
        }
    }

    companion object {
        const val InBlackList = 1000
        const val NoNetwork = 1001
    }

    private val requestPermissionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            switch()
        }
    }

    private fun networkAlert() {
        val alertDialog = AlertDialog.Builder(this).apply {
            setMessage(R.string.network_alert)
            setPositiveButton(R.string.ok) { _, _ -> }
        }.create()
        alertDialog.show()
    }

    private fun policyAlert() {
        val alertDialog = AlertDialog.Builder(this).apply {
            setMessage(R.string.policy_alert)
            setPositiveButton(R.string.confirm) { _, _ ->
                ActivityManager.finishAllActivity()
            }
            setCancelable(false)
        }.create()
        alertDialog.show()
    }

    private fun requestPermission() {
        VpnService.prepare(this)?.let {
            requestPermissionResult.launch(it, ActivityOptionsCompat.makeTaskLaunchBehind())
        }
    }

    private fun checkPermission(): Boolean {
        return VpnService.prepare(this) == null
    }


    private fun switch() {
        if (checkPermission().not()) {
            requestPermission()
            return
        }
        if (active.not())
            return
        active = false
        when (CoreManager.state.value) {
            BaseService.State.Idle, BaseService.State.Stopped -> CoreManager.connectVPN(connectHandler)
            BaseService.State.Connected -> CoreManager.disconnectVPN()
            else -> {}
        }
    }

    override fun initView() {
        initBarPlaceHolder(binding.statusBarHolder, binding.navigationBarHolder)
        binding.lottieView.imageAssetsFolder = "images/"
        binding.lottieView.repeatMode = LottieDrawable.RESTART
        binding.lottieView.repeatCount = LottieDrawable.INFINITE

        binding.lottieHand.imageAssetsFolder = "point/"
        binding.lottieHand.repeatMode = LottieDrawable.RESTART
        binding.lottieHand.repeatCount = LottieDrawable.INFINITE
        bindingDate()

        binding.lottieView.setOnClickListener(this)
        binding.progressBtn.setOnClickListener(this)
        binding.progressText.setOnClickListener(this)

        binding.settingBtn.setOnClickListener(this)
        binding.serverBtn.setOnClickListener(this)

        if (IpUtil.inBlackList())
            policyAlert()
    }

    override fun onStart() {
        super.onStart()
        active = true
    }

    override fun onResume() {
        super.onResume()
        binding.flagImg.setImageResource(ServersManager.getFlagDrawableId(ServersManager.connectServer ?: ServersManager.selectServer))
        updateConnectTimeJob = lifecycleScope.launch {
            while (true) {
                binding.connectTimeText.text = ServersManager.connectTime
                delay(200)
            }
        }
    }

    override fun onBackPressed() {
        if (first.value == true) {
            first.postValue(false)
            return
        }
        val interruptConnect = CoreManager.tryInterruptConnect()
        if (interruptConnect == null) {
            super.onBackPressed()
            return
        }else if(interruptConnect == true)
            active = true
    }

    private fun tryInterrupt(lambda : ()->Unit){
        val interruptConnect = CoreManager.tryInterruptConnect()
        if (interruptConnect == null) {
            lambda.invoke()
            return
        }else if(interruptConnect == true) {
            lambda.invoke()
            active = true
        }
    }

    override fun onPause() {
        super.onPause()
        updateConnectTimeJob?.cancel()
    }

    override fun onStop() {
        super.onStop()
        CoreManager.interruptConnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        connectHandler.removeCallbacksAndMessages(null)
    }

    private fun bindingDate() {
        CoreManager.state.observe(this) {
            when (it) {
                BaseService.State.Connecting -> onConnecting()
                BaseService.State.Connected -> onConnected()
                BaseService.State.Stopping -> onStopping()
                else -> onStopped()
            }
        }
        first.observe(this) {
            if (it) {
                binding.shape.visibility = View.VISIBLE
                binding.lottieHand.visibility = View.VISIBLE
                binding.lottieHand.playAnimation()
            } else {
                binding.shape.visibility = View.GONE
                binding.lottieHand.visibility = View.GONE
                binding.lottieHand.cancelAnimation()
            }
        }
    }

    private fun onStopped() {
        binding.progressText.text = getString(R.string.connect)
        binding.lottieView.cancelAnimation()
        binding.lottieView.progress = 0f
        progressJob?.cancel()
        binding.progressBtn.progress = 0
        if(ServersManager.isConnect){
            if(active.not())
                jumpToEnd("disconnected",ServersManager.connectServer)
            ServersManager.onDisconnect()
        }
    }

    private fun onConnecting() {
        binding.progressText.text = getString(R.string.connecting)
        binding.lottieView.playAnimation()
        progressJob?.cancel()
        progressJob = lifecycleScope.launch {
            while (binding.progressBtn.progress < 60) {
                binding.progressBtn.progress = binding.progressBtn.progress + 1
                delay(30)
            }
        }
    }

    private fun onConnected() {
        binding.progressText.text = getString(R.string.connected)
        binding.lottieView.cancelAnimation()
        binding.lottieView.progress = 0f
        progressJob?.cancel()
        binding.progressBtn.progress = 100
        if(ServersManager.isConnect.not()){
            ServersManager.onConnect()
            if(active.not())
                jumpToEnd("connected",ServersManager.connectServer)
        }
    }

    private fun onStopping() {
        binding.progressText.text = getString(R.string.disconnecting)
        binding.lottieView.playAnimation()
        progressJob?.cancel()
    }

    private fun jumpToEnd(state : String,server : MyServer?){
        startActivity(EndActivity::class.java, Bundle().apply {
            putString("state",state)
            putParcelable("server",server)
        })
    }

    private val selectServerResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            switch()
        }
    }
    override fun onClick(v: View?) {
        if (first.value == true) {
            when (v) {
                binding.lottieView, binding.progressBtn, binding.progressText -> {}
                else -> return
            }
        }
        first.postValue(false)
        when (v) {
            binding.lottieView, binding.progressBtn, binding.progressText -> switch()
            binding.settingBtn -> tryInterrupt { startActivity(SettingActivity::class.java) }
            binding.serverBtn -> tryInterrupt { selectServerResult.launch(Intent(this,SelectServerActivity::class.java)) }
        }
    }
}
