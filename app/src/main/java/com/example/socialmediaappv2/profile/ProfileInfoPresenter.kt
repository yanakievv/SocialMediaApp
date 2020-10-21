package com.example.socialmediaappv2.profile

import android.content.Context
import android.graphics.Bitmap
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.data.*
import com.example.socialmediaappv2.data.App.currentProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ProfileInfoPresenter(var view: Contract.ProfileView?): Contract.ProfileInfoPresenter  {

    private lateinit var userInfo: UserInfoModel
    private lateinit var databaseInstance: UserDatabase
    private lateinit var userDao: UserDAO
    private lateinit var imageDao: ImageDAO

    private var isCurrentUser: Boolean = false

    override suspend fun init(userId: String, context: Context) {
        databaseInstance = UserDatabase.getInstance(context)
        userDao = databaseInstance.userDAO
        imageDao = databaseInstance.imageDAO
        userInfo = userDao.getUser(userId)
        isCurrentUser = (currentProfile.currentUser.publisherId == userId)
    }

    override fun reInit(id: String) {
        if (isCurrentUser) {
            CoroutineScope(Dispatchers.IO).launch {
                currentProfile.currentUser = userDao.getUser(id)
                userInfo = currentProfile.currentUser
            }
        }
    }

    override fun refreshDb() {
        if (isCurrentUser) {
            CoroutineScope(Dispatchers.IO).launch { userDao.updateUser(userInfo) }
        }
    }

    override fun getDisplayName(): String {
        return userInfo.displayName
    }

    override fun setDisplayName(name: String) {
        userInfo.displayName = name
    }

    override fun getBirthDate(): String {
        return userInfo.birthDate
    }

    override fun setBirthDate(birthDate: String) {
        userInfo.birthDate = birthDate
    }

    override fun getBio(): String {
        return userInfo.bio
    }

    override fun setBio(bio: String) {
        userInfo.bio = bio
    }

    override fun getPictures(): List<ImageModel>? {
        var pics: List<ImageModel>? = null
        runBlocking { pics = imageDao.getPublisherPosts(userInfo.publisherId) }
        return pics
    }

    override fun getProfilePic(): ImageModel? {
        var image: ImageModel? = null
        runBlocking { image = imageDao.getProfilePicture(userInfo.profilePic) }
        return image
    }

    override fun onDestroy() {
        view = null
    }
}