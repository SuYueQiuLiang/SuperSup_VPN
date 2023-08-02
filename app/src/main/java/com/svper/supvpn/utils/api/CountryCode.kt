package com.svper.supvpn.utils.api

import com.svper.supvpn.beans.IpInfo
import retrofit2.http.GET

interface CountryCode {
    @GET("https://api.infoip.io/")
    suspend fun getCountry() : IpInfo
}