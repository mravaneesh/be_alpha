package com.example.authentication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id:String = "",
    val name:String = "",
    val username:String = "",
    val email:String = "",
    val bio:String = "",
    val birthdate:String = "Select",
    val gender:String = "Select",
    val profileImageUrl:String="",
    val followers:Int = 0,
    val following:Int = 0,
    val posts:Int = 0,
    val createdAt:Long = System.currentTimeMillis()
): Parcelable

