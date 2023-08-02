package com.svper.supvpn.utils

import com.svper.supvpn.utils.api.CountryCode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object IpUtil {
    private val retrofit by lazy { RetrofitManager.getRetrofit("https://api.infoip.io") }
    private val api by lazy { retrofit.create(CountryCode::class.java) }
    private var countryCode : String? = null
    private val blackListCountryCode = arrayListOf("HK","MO","IR")
    fun inBlackList() : Boolean = blackListCountryCode.contains(countryCode)

    fun preLoadCode(){
        MainScope().launch(Dispatchers.IO) {
            try{
                countryCode = api.getCountry().country_short
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
    }

    suspend fun isInBlackList() =
        try{
            countryCode = api.getCountry().country_short
            inBlackList()
        }catch (e : Exception){
            e.printStackTrace()
            false
        }
}