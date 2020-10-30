package com.example.socialmediaappv2.home.content

import android.R
import android.content.Context
import android.util.Log
import com.example.socialmediaappv2.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*

object PublisherPictureContent {

    var ITEMS: MutableList<ImageModel> = ArrayList()
    var initLoaded = false

    //TODO add variable for storing personal posts so we dont have to load them from the database every time we view other person's posts and come back to ours

    private lateinit var userInfo: UserInfoModel
    private lateinit var databaseInstance: UserDatabase
    private lateinit var userDAO: UserDAO
    private lateinit var imageDao: ImageDAO


    private var isCurrentUser: Boolean = false


    fun initLoadImagesFromDatabase(userId: String, context: Context) {
        Log.e("CURRENT_USER", App.currentUser.publisherId)
        Log.e("LOAD_FROM_USER", userId)
        Log.e("LOAD_QUANTITY", "database_all_publisher_posts")

        databaseInstance = UserDatabase.getInstance(context)
        imageDao = databaseInstance.imageDAO
        userDAO = databaseInstance.userDAO

        runBlocking {
            userInfo = userDAO.getUser(userId)
            ITEMS = imageDao.getPublisherPosts(userInfo.publisherId) as MutableList<ImageModel>
            isCurrentUser = App.currentUser.publisherId == userId
            initLoaded = true
        }
    }

    fun loadRecentImages() {
        if (App.imagesTaken > 0) {
            runBlocking {
                addImages(
                    App.imageDao.getPublisherLastPosts(
                        App.currentUser.publisherId,
                        App.imagesTaken
                    )
                )
            }
            App.imagesTaken = 0
        }
    }

    private fun addImage(image: ImageModel) {
        ITEMS.add(image)
        Log.e("LOAD_QUANTITY", "camera_recent_post")
    }

    private fun addImages(images: List<ImageModel>) {
        for (i in images.reversed()) {
            addImage(i)
        }
    }

    fun setProfilePicture(picId: Int) {
        if (isCurrentUser) {
            App.currentUser.profilePic = picId
            CoroutineScope(Dispatchers.IO).launch {
                App.userDao.updateUser(App.currentUser)
            }
        }
    }

    fun removePost(picId: Int, absolutePath: String) {
        if (isCurrentUser) {
            ITEMS.removeAt(binarySearchIterative(ITEMS, picId))
            CoroutineScope(Dispatchers.IO).launch {
                App.imageDao.removePost(App.currentUser.publisherId, picId)
                val target = File(absolutePath)
                Log.e(" target_path", "" + R.attr.path)
                if (target.exists() && target.isFile && target.canWrite()) {
                    target.delete()
                    Log.e("d_file", "" + target.name)
                }
            }
        }
    }

    private fun binarySearchIterative(input: MutableList<ImageModel>, picId: Int): Int {  // because when loading pictures from db to view we get them in ascending order of ids,
        var low = 0                                                               // and when taking new pictures they are added in the same order we can use bsearch
        var high = input.size - 1                                            // to find the index of the picture in arraylist
        var mid: Int
        while (low <= high) {
            mid = low + ((high - low) / 2)
            when {
                picId > input[mid].picId -> low = mid + 1
                picId == input[mid].picId -> return mid
                picId < input[mid].picId -> high = mid - 1
            }
        }
        return -1
    }

    fun isCurrentUser(): Boolean {
        return isCurrentUser
    }
}