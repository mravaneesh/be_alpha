package com.example.profile_domain.model

data class UserProfile(
    val userId:String="",
    val name:String="",
    val userName:String="",
    val bio:String="",
    val birthdate:String = "",
    val gender:String = "",
    val profileImageUrl:String="",
    val followers:List<String> = emptyList(),
    val following:List<String> = emptyList(),
    val posts:Int = 0,
)
