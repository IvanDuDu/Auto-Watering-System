package com.example.project2_verse3.model

import com.example.project2_verse3.ui.navigation.WifiInfo

data class TreeModel(val name: String, val image: Int, val RecommendHumidity: Int)

data class StaffModel(val BossID: String,val staffID:String , val Humidity: Int, val tree: TreeModel)

data class BossModel(val BossID: String,val bossName:String,var wifiInfo: WifiInfo,val numberOfStaff:Int, val staffs: List<StaffModel>)



