package com.svper.supvpn.utils

import android.webkit.WebSettings
import com.svper.supvpn.application
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitManager {
    private const val overTimeSeconds = 10L
    private val logUtil by lazy { LogUtil(this) }
    private val httpLoggingInterceptor by lazy { HttpLoggingInterceptor{ logUtil.d(it) }.setLevel(HttpLoggingInterceptor.Level.BODY) }
    private val headerInterceptor by lazy {
        Interceptor{
            val request = it.request().newBuilder().apply {
                removeHeader("User-Agent")
                addHeader("User-Agent",WebSettings.getDefaultUserAgent(application))
            }
            it.proceed(request.build())
        }
    }
    private val okhttpClient
        get() =
            OkHttpClient.Builder().apply {
                writeTimeout(overTimeSeconds,TimeUnit.SECONDS)
                readTimeout(overTimeSeconds,TimeUnit.SECONDS)
                callTimeout(overTimeSeconds,TimeUnit.SECONDS)
                connectTimeout(overTimeSeconds,TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
                addInterceptor(headerInterceptor)
                addInterceptor(httpLoggingInterceptor)
            }.build()
    fun getRetrofit(baseUrl : String) =
        Retrofit.Builder().apply {
            client(okhttpClient)
            baseUrl(baseUrl)
            addConverterFactory(GsonConverterFactory.create())
        }.build()
}