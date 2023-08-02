package com.svper.supvpn.ui.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import com.svper.supvpn.BuildConfig
import com.svper.supvpn.R
import com.svper.supvpn.databinding.ActivitySettingBinding
import com.svper.supvpn.utils.startActivity

class SettingActivity : BaseActivity<ActivitySettingBinding, ViewModel>(),OnClickListener {
    override val binding: ActivitySettingBinding by lazy { ActivitySettingBinding.inflate(layoutInflater) }
    override val viewModel: ViewModel by viewModels()
    override fun initView() {
        binding.privacy.setOnClickListener(this)
        binding.update.setOnClickListener(this)
        binding.share.setOnClickListener(this)
        binding.backBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            binding.privacy -> privacy()
            binding.update -> update()
            binding.share -> share()
            binding.backBtn -> finish()
        }
    }

    private fun privacy(){
        startActivity(WebActivity::class.java)
    }
    private fun update(){
        try{
            var intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
                setPackage("com.android.vending")
            }
            if(intent.resolveActivity(this.packageManager) != null)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            else intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}") }
            startActivity(intent)
        }catch (e : Exception){
            e.printStackTrace()
        }
    }
    private fun share(){
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT,"https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
        }
        startActivity(Intent.createChooser(intent,"Share ${getString(R.string.app_name)}"))
    }
}