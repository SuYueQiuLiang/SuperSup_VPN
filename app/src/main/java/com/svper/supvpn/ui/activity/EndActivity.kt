package com.svper.supvpn.ui.activity

import android.view.View
import android.view.View.OnClickListener
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import com.svper.supvpn.R
import com.svper.supvpn.beans.MyServer
import com.svper.supvpn.databinding.ActivityEndBinding
import com.svper.supvpn.utils.ServersManager

class EndActivity : BaseActivity<ActivityEndBinding, ViewModel>(),OnClickListener {
    override val binding: ActivityEndBinding by lazy { ActivityEndBinding.inflate(layoutInflater) }
    override val viewModel: ViewModel by viewModels()
    override fun initView() {
        when(intent.getStringExtra("state")){
            "disconnected" -> onDisconnected()
            "connected" -> onConnected()
        }
        binding.flagImg.setImageResource(ServersManager.getFlagDrawableId(intent.getParcelableExtra("server") ?: ServersManager.connectServer ?: ServersManager.selectServer))

        binding.backBtn.setOnClickListener(this)
    }
    private fun onDisconnected(){
        binding.flagBg.setImageResource(R.drawable.disconnect_flag_bg)
        binding.connectStateText.text = getString(R.string.disconnected_succeeded)
    }
    private fun onConnected(){
        binding.flagBg.setImageResource(R.drawable.connect_flag_bg)
        binding.connectStateText.text = getString(R.string.connected_succeeded)
    }

    override fun onClick(v: View?) {
        when(v){
            binding.backBtn -> finish()
        }
    }
}