package com.example.socialmediaappv2.data

import android.app.Application

class App: Application() {
    companion object currentProfile {
        lateinit var currentUser: UserInfoModel
        lateinit var databaseInstance: UserDatabase
        lateinit var userDao: UserDAO
        lateinit var imageDao: ImageDAO
        var latLong: DoubleArray = doubleArrayOf(0.0, 0.0)

        var imagesTaken: Int = 0

    }

}
