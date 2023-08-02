package com.svper.supvpn.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.shadowsocks.bg.BaseService
import com.svper.supvpn.R
import com.svper.supvpn.beans.MyServer
import com.svper.supvpn.databinding.ActivitySelectServerBinding
import com.svper.supvpn.ui.adapter.ServerRecyclerAdapter
import com.svper.supvpn.ui.viewmodel.SelectServerActivityViewModel
import com.svper.supvpn.utils.CoreManager
import com.svper.supvpn.utils.ServersManager

class SelectServerActivity : BaseActivity<ActivitySelectServerBinding, SelectServerActivityViewModel>(),OnClickListener {
    override val binding: ActivitySelectServerBinding by lazy { ActivitySelectServerBinding.inflate(layoutInflater) }
    override val viewModel: SelectServerActivityViewModel by viewModels()

    override fun initView() {
        val adapter = ServerRecyclerAdapter(ServersManager.serverList){
            if(it != ServersManager.selectServer && CoreManager.state.value == BaseService.State.Connected)
                disconnectAlert(it)
            else if(CoreManager.state.value == BaseService.State.Stopped||CoreManager.state.value == BaseService.State.Idle){
                ServersManager.selectServer = it
                finishWithCodeOk()
            }
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.backBtn.setOnClickListener(this)
    }

    private fun disconnectAlert(server : MyServer){
        AlertDialog.Builder(this).apply {
            setMessage(R.string.disconnect_alert)
            setPositiveButton(R.string.sure){_,_ ->
                ServersManager.selectServer = server
                finishWithCodeOk()
            }
            setNegativeButton(R.string.back){_,_ -> }
        }.show()
    }

    private fun finishWithCodeOk(){
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onClick(v: View?) {
        when(v){
            binding.backBtn -> finish()
        }
    }
}