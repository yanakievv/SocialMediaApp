package com.example.socialmediaappv2.data.firebase

data class UserModel(
    val name: String,
    val birth: String,
    val bio: String,
    val profilePicturePath: String?
) {
    constructor(): this("", "", "", null)
}
