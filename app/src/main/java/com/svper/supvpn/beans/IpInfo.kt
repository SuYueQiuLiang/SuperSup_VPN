package com.svper.supvpn.beans


import androidx.annotation.Keep

@Keep
data class IpInfo(
    val city: String,
    val country_long: String,
    val country_short: String,
    val ip: String,
    val latitude: Double,
    val longitude: Double,
    val postal_code: String,
    val region: String,
    val timezone: String
)