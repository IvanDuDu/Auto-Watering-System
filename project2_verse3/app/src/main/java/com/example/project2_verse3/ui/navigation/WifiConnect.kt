package com.example.project2_verse3.ui.navigation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi


data class WifiInfo(
    val ssid: String,
    val password: String,
    val encryption: String
)

fun parseWifiQRCode(data: String): WifiInfo? {
    try {
        val cleaned = data.removePrefix("WIFI:").removeSuffix(";;")
        val map = cleaned.split(";").associate {
            val (k, v) = it.split(":")
            k to v
        }
        return WifiInfo(
            ssid = map["S"] ?: return null,
            password = map["P"] ?: "",
            encryption = map["T"] ?: "nopass"
        )
    } catch (e: Exception) {
        return null
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
fun connectToWifi(context: Context, wifiInfo: WifiInfo, onConnect: () -> Unit) {
    val specifier = WifiNetworkSpecifier.Builder()
        .setSsid(wifiInfo.ssid)
        .setWpa2Passphrase(wifiInfo.password)
        .build()

    val request = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .setNetworkSpecifier(specifier)
        .build()

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            connectivityManager.bindProcessToNetwork(network)
            Toast.makeText(context, "Kết nối thành công tới ${wifiInfo.ssid}", Toast.LENGTH_SHORT).show()
            onConnect() // báo về Compose
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Toast.makeText(context, "Không thể kết nối đến Wi-Fi", Toast.LENGTH_SHORT).show()
        }
    }

    connectivityManager.requestNetwork(request, callback)
}


