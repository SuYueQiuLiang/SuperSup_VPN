package com.svper.supvpn.ui.activity

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.activity.viewModels
import com.svper.supvpn.databinding.ActivityStartBinding
import com.svper.supvpn.ui.viewmodel.StartActivityViewModel
import com.svper.supvpn.utils.startActivity
import java.lang.ref.WeakReference

class StartActivity : BaseActivity<ActivityStartBinding,StartActivityViewModel>() {

    companion object{
        const val JUMP = 1000
    }

    override val binding: ActivityStartBinding by lazy { ActivityStartBinding.inflate(layoutInflater) }
    override val viewModel: StartActivityViewModel by viewModels()
    val handler = StartActivityHandler(WeakReference(this))
    class StartActivityHandler(val wActivity : WeakReference<StartActivity>) : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                JUMP -> {
                    wActivity.get()?.jump()
                }
            }
        }
    }

    override fun onBackPressed() {

    }

    fun jump(){
        startActivity(MainActivity::class.java)
        finish()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startProgress(handler)
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun initView() {
        bind()
    }

    private fun bind(){
        viewModel.progress.observe(this){
            binding.progressBar.progress = it
        }
    }
}