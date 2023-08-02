package com.svper.supvpn.utils

import android.util.Log

class LogUtil(any : Any) {
    private val btn = true
    private val tag
        get() = "|${clazz.simpleName}|${Thread.currentThread().name}|"
    private val clazz : Class<Any> = any.javaClass

    fun d(msg: String) {
        if (btn.not())
            return
        val tag = tag
        val message = msg(msg)
        Log.d(tag, message)
    }

    private fun msg(msg: String): String {
        val trace = Throwable().stackTrace
        val element = trace.firstOrNull { it.className != this.javaClass.name }
        return if(element != null)
            "(${element.fileName}:${element.lineNumber}) $msg"
        else msg
    }

}