package com.example.profile_data.model

data class UserProfileDto(
    val id:String = "",
    val name:String = "",
    val username:String = "",
    val email:String = "",
    val bio:String = "",
    val birthdate:String = "",
    val gender:String = "",
    val profileImageUrl:String="",
    val followers:Int = 0,
    val following:Int = 0,
    val posts:Int = 0,
    val createdAt:Long = System.currentTimeMillis()
)
