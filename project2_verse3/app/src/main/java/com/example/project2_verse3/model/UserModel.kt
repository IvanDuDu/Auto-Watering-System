package com.example.project2_verse3.model
import androidx.compose.runtime.mutableStateOf
import com.example.project2_verse3.R



data class UserModel(val cusAcc: String, val cusName :String , val cusID :String ,
                     val cusImage :Int , var passWord :String ,)


var mainUser = (UserModel( "dvu3999@gmail.com","Vũ Ngọc Tiến Dũng", "dc075d60-2ed4-11f0-ba5e-e55fd4a29280", R.drawable.tree_,"123"))

var  userList = mutableListOf<UserModel>()



