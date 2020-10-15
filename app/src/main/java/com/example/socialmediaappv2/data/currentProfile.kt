package com.example.socialmediaappv2.data

object currentProfile {
    lateinit var currentUser: UserInfoModel
    lateinit var databaseInstance: UserDatabase
    lateinit var userDao: UserDAO
    lateinit var imageDao: ImageDAO
}