package com.svper.supvpn.ui.activity

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import com.svper.supvpn.databinding.ActivityWebBinding

class WebActivity : BaseActivity<ActivityWebBinding, ViewModel>() {
    override val binding: ActivityWebBinding by lazy { ActivityWebBinding.inflate(layoutInflater) }
    override val viewModel: ViewModel by viewModels()
    private val url = "https://m.baidu.com"
    override fun initView() {
        super.initView()
        binding.webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }
        binding.webView.loadUrl(url)
    }
}