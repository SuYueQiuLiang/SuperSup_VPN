package com.svper.supvpn.utils

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.github.shadowsocks.Core.activity
import com.svper.supvpn.application
import com.svper.supvpn.utils.ActivityManager.activityList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

fun Activity.startActivity(clazz: Class<out Activity>,bundle: Bundle? = null){
    val intent = Intent(this,clazz)
    bundle?.let { intent.putExtras(it) }
    if(ActivityManager.containsActivity(this))
        startActivity(intent)
}

object ActivityManager : ActivityLifecycleCallbacks {
    private val activityList : MutableList<WeakReference<Activity>> by lazy { mutableListOf() }
    private var showingActivityCount : Int = 0
    private var finishJob : Job? = null
    private const val finishDelay = 3000L


    private val logUtil by lazy { LogUtil(this) }

    fun containsActivity(activity: Activity) : Boolean{
        synchronized(activityList){
            val iterator = activityList.iterator()
            while (iterator.hasNext()){
                val next = iterator.next().get()
                if(next == activity)
                    return true
            }
            return false
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        synchronized(activityList){
            activityList.add(WeakReference(activity))
            logUtil.d("activity $activity created,${activityList.size} activities in stack now")
        }
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
        synchronized(this){
            finishJob?.cancel()
            showingActivityCount++
            logUtil.d("activity $activity started,$showingActivityCount activities showing")
        }
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        synchronized(this){
            showingActivityCount --
            logUtil.d("activity $activity stopped,$showingActivityCount activities showing")
            if(showingActivityCount <= 0)
                finishJob = GlobalScope.launch {
                    delay(finishDelay)
                    ensureActive()
                    finishAllActivity()
                }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        synchronized(activityList){
            removeActivity(activity)
            logUtil.d("activity $activity destroyed,${activityList.size} activities in stack now")
        }
    }

    fun finishAllActivity(){
        synchronized(activityList){
            val iterator = activityList.iterator()
            while (iterator.hasNext())
                iterator.next().get()?.finish()
            val am = application.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            logUtil.d(am.appTasks[0].taskInfo.toString())
        }
    }

    private fun removeActivity(activity: Activity){
        synchronized(activityList){
            val iterator = activityList.iterator()
            while (iterator.hasNext()){
                val next = iterator.next().get()
                if(next == null)
                    iterator.remove()
                if(next == activity){
                    iterator.remove()
                    break
                }
            }
        }
    }

}