package com.example.socialmediaappv2.profile

import android.content.Context
import android.graphics.Bitmap
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.data.ImageModel
import com.example.socialmediaappv2.data.UserDatabase
import com.example.socialmediaappv2.data.UserInfoModel
import com.example.socialmediaappv2.data.currentProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ProfileInfoPresenter(var view: Contract.ProfileView?): Contract.ProfileInfoPresenter  {
    override suspend fun init(id: String, displayName: String, context: Context) {
        currentProfile.databaseInstance = UserDatabase.getInstance(context)
        currentProfile.userDao = currentProfile.databaseInstance.userDAO
        currentProfile.imageDao = currentProfile.databaseInstance.imageDAO
        if (currentProfile.userDao.checkUser(id) == 0) {
            currentProfile.userDao.addUser(UserInfoModel(id, displayName, "Private", "", ""))
        }
        currentProfile.currentUser = currentProfile.userDao.getUser(id)
    }

    override fun reInit(id: String) {
        CoroutineScope(Dispatchers.IO).launch { currentProfile.currentUser = currentProfile.userDao.getUser(id) }
    }

    override fun refreshDb() {
        CoroutineScope(Dispatchers.IO).launch { currentProfile.userDao.updateUser(currentProfile.currentUser) }
    }

    override fun getBirthDate(): String {
        TODO("Not yet implemented")
    }

    override fun setBirthDate(birthDate: String) {
        TODO("Not yet implemented")
    }

    override fun getBio(): String {
        TODO("Not yet implemented")
    }

    override fun setBio(bio: String) {
        TODO("Not yet implemented")
    }

    override fun getPicture(): String {
        var path: List<ImageModel>? = null
        runBlocking { path = currentProfile.imageDao.getPublisherPosts(currentProfile.currentUser.publisherId) }
        return path?.get(path?.lastIndex!!)?.image as String
    }

    override fun onDestroy() {
        view = null
    }
}