package com.example.socialmediaappv2

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.data.*
import com.example.socialmediaappv2.data.firebase.FirestoreUtil
import com.example.socialmediaappv2.data.firebase.UserModel
import com.example.socialmediaappv2.data.roomdb.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class UserInfoPresenter(var view: Contract.MainView?): Contract.UserInfoPresenter {

    companion object {
        private lateinit var sharedPref: SharedPreference
        private lateinit var userInfo: UserModel
        private lateinit var localUserInfo: UserInfoModel
        private lateinit var databaseInstance: UserDatabase
        private lateinit var userDao: UserDAO
        private lateinit var imageDao: ImageDAO
    }

    override fun init(id: String, displayName: String, context: Context) {
        sharedPref = SharedPreference(context)
        databaseInstance = UserDatabase.getInstance(context)
        imageDao = databaseInstance.imageDAO
            FirestoreUtil.getCurrentUser { userModel ->
                userInfo = userModel
                sharedPref.save("publisherId", FirestoreUtil.getUID())
                sharedPref.save("displayName", userInfo.name)
                sharedPref.save("birthDate", userInfo.birth)
                sharedPref.save("bio", userInfo.bio)
                userInfo.profilePicturePath?.let { sharedPref.save("profilePic", it) }
                sharedPref.save("posts", userInfo.posts)
            }



    }

    override fun reInit(id: String) {
        FirestoreUtil.getCurrentUser {
            userInfo = it
        }
    }

    override suspend fun refreshDb() {
        FirestoreUtil.updateCurrentUser(userInfo)
    }

    override fun getUserPosts(): List<ImageModel>? {

        //TODO get posts from firebase storage and remove local implementation

        var posts: List<ImageModel>? = null
        runBlocking { posts =  imageDao.getPublisherPosts(localUserInfo.publisherId)}
        return posts
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun addPost(newPost: String, rotation: Int, latLong: DoubleArray, context: Context) {

        databaseInstance = UserDatabase.getInstance(context)
        imageDao = databaseInstance.imageDAO

        val formatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        CoroutineScope(Dispatchers.IO).launch {
            imageDao.addImage(
                ImageModel(
                    0,
                    FirestoreUtil.getUID(),
                    newPost,
                    userInfo.name,
                    formatted,
                    latLong[0],
                    latLong[1],
                    rotation
                )
            )
            refreshDb()
        }
    }

    override fun getCurrentUser(): UserModel {
        return userInfo
    }

    override fun onDestroy() {
        this.view = null
    }
}