package com.example.demonetworkapp.interfaces

interface NetworkEnum {

    enum class NetworkType(val code: Int) {

        WEP_CONNECTION(0), WPA_CONNECTION(1), WPA2_CONNECTION(2), WPA3_CONNECTION(3), UNKNOWN_CONNECTION(4), NO_CONNECTION(5), MOBILE_CONNECTION(6)

    }
}