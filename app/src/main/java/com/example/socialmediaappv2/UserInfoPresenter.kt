package com.example.socialmediaappv2

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class UserInfoPresenter(var view: Contract.MainView?): Contract.UserInfoPresenter {

    companion object {
        private lateinit var sharedPref: SharedPreference
        private lateinit var userInfo: UserInfoModel
        private lateinit var databaseInstance: UserDatabase
        private lateinit var userDao: UserDAO
        private lateinit var imageDao: ImageDAO
    }

    override suspend fun init(id: String, displayName: String, context: Context) {
        sharedPref = SharedPreference(context)
        databaseInstance = UserDatabase.getInstance(context)
        userDao = databaseInstance.userDAO
        imageDao = databaseInstance.imageDAO
        if (userDao.checkUser(id) == 0) {
            userDao.addUser(UserInfoModel(id, displayName, "Private", "", 0))
        }
        userInfo = userDao.getUser(id)
        sharedPref.save("publisherId", userInfo.publisherId)
        sharedPref.save("displayName", userInfo.displayName)
        sharedPref.save("birthDate", userInfo.birthDate)
        sharedPref.save("bio", userInfo.bio)
    }

    override fun reInit(id: String) {
        CoroutineScope(Dispatchers.IO).launch { userInfo = userDao.getUser(id) }
    }

    override fun refreshDb() {
        CoroutineScope(Dispatchers.IO).launch { userDao.updateUser(userInfo) }
    }

    override fun getUserPosts(): List<ImageModel>? {
        var posts: List<ImageModel>? = null
        runBlocking { posts =  imageDao.getPublisherPosts(userInfo.publisherId)}
        return posts
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun addPost(newPost: String, rotation: Int, latLong: DoubleArray, context: Context) {
        val formatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        CoroutineScope(Dispatchers.IO).launch {
            imageDao.addImage(
                ImageModel(
                    0,
                    userInfo.publisherId,
                    newPost,
                    userInfo.displayName,
                    formatted,
                    latLong[0],
                    latLong[1],
                    rotation
                )
            )
        }
    }

    override fun getCurrentUser(): UserInfoModel {
        return userInfo
    }

    override fun setProfilePicture(picId: Int) {
        runBlocking { userDao.setProfilePic(userInfo.publisherId, picId) }
    }

    override fun onDestroy() {
        this.view = null
    }
}