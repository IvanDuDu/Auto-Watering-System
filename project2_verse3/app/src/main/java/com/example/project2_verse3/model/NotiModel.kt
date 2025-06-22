package com.example.project2_verse3.model

import kotlinx.serialization.Serializable

@Serializable
data class NotiModel(val bossName: String, val treeName: String, val formType: Int, var time:Long)