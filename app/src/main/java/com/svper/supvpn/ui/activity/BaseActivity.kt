package com.svper.supvpn.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar

abstract class BaseActivity<B : ViewBinding,V : ViewModel> : AppCompatActivity() {
    abstract val binding : B
    abstract val viewModel : V
    private lateinit var immersionBar: ImmersionBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFirst()
        setContentView(binding.root)
        immersionBar = ImmersionBar.with(this).apply {
            transparentBar()
            init()
        }

        initView()
    }
    protected fun initBarPlaceHolder(statusBarHolder : View? = null,navigationBarHolder : View? = null){
        statusBarHolder?.let { immersionBar.statusBarView(it).init() }
        navigationBarHolder?.let {
            if(ImmersionBar.isNavigationAtBottom(this)&&ImmersionBar.hasNavigationBar(this))
                it.layoutParams.height = ImmersionBar.getNavigationBarHeight(this)
        }
    }
    protected open fun initFirst(){}
    protected open fun initView(){}
}