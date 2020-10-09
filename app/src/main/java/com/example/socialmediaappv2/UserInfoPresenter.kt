package com.example.socialmediaappv2

import android.content.Context
import android.graphics.Bitmap
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
        private lateinit var currentUser: UserInfoModel
        private lateinit var databaseInstance: UserDatabase
        private lateinit var userDao: UserDAO
        private lateinit var imageDao: ImageDAO
    }

    override suspend fun init(id: String, displayName: String, context: Context) {
        databaseInstance = UserDatabase.getInstance(context)
        userDao = databaseInstance.userDAO
        imageDao = databaseInstance.imageDAO
        if (userDao.checkUser(id) == 0) {
            userDao.addUser(UserInfoModel(id, displayName, "Private", ""))
        }
        currentUser = userDao.getUser(id)
    }

    override fun reInit(id: String) {
        CoroutineScope(Dispatchers.IO).launch { currentUser = userDao.getUser(id) }
    }

    override fun refreshDb() {
        CoroutineScope(Dispatchers.IO).launch { userDao.updateUser(currentUser) }
    }

    override fun getUserPosts(): List<ImageModel>? {
        var posts: List<ImageModel>? = null
        runBlocking { posts =  imageDao.getPublisherPosts(currentUser.publisherId)}
        return posts
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun addPost(newPost: Bitmap) {
        val formatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        CoroutineScope(Dispatchers.IO).launch {
            imageDao.addImage(
                ImageModel(
                    0,
                    currentUser.publisherId,
                    ImageBitmapString().bitMapToString(newPost) as String,
                    currentUser.displayName,
                    formatted
                )
            )
        }
    }

    override fun getCurrentUser(): UserInfoModel {
        return currentUser
    }

    override fun onDestroy() {
        this.view = null
    }
}