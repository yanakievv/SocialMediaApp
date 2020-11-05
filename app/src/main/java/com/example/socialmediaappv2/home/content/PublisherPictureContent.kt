package com.example.socialmediaappv2.home.content

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
    var TEMP: MutableList<ImageModel> = ArrayList()
    var initLoaded = false


    private lateinit var sharedPref: SharedPreference
    private lateinit var userInfo: UserInfoModel
    private lateinit var databaseInstance: UserDatabase
    private lateinit var userDao: UserDAO
    private lateinit var imageDao: ImageDAO


    private var isCurrentUser: Boolean = false


    private fun reinitUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            userInfo = userDao.getUser(sharedPref.getString("publisherId")!!)
        }
    }


    fun initLoadImagesFromDatabase(userId: String, context: Context) {
        Log.e("LOAD_FROM_USER", userId)

        sharedPref = SharedPreference(context)
        databaseInstance = UserDatabase.getInstance(context)
        imageDao = databaseInstance.imageDAO
        userDao = databaseInstance.userDAO

        if (userId == sharedPref.getString("publisherId")!! && ITEMS != TEMP) {
            isCurrentUser = true
            ITEMS = TEMP
            Log.e("LOAD_QUANTITY", "load_from_temp")
        }
        else {
            runBlocking {
                userInfo = userDao.getUser(userId)
                ITEMS = imageDao.getPublisherPosts(userInfo.publisherId) as MutableList<ImageModel>
                isCurrentUser = sharedPref.getString("publisherId")!! == userId
                initLoaded = true
                Log.e("LOAD_QUANTITY", "database_all_publisher_posts")
            }
            if (isCurrentUser) {
                TEMP = ITEMS
            }
            Log.e("CURRENT_USER", userInfo.publisherId)
        }
    }

    fun loadRecentImages(userId: String, context: Context) {

        reinitUserInfo()
        if (SharedPreference.imagesTaken > 0 && isCurrentUser) {
            runBlocking {
                addImages(
                    imageDao.getPublisherLastPosts(
                        sharedPref.getString("publisherId")!!,
                        SharedPreference.imagesTaken
                    )
                )
            }
            SharedPreference.imagesTaken = 0
        }
        else if (!isCurrentUser) {
            initLoadImagesFromDatabase(userId, context)
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
            userInfo.profilePic = picId
            CoroutineScope(Dispatchers.IO).launch {
                userDao.updateUser(userInfo)
            }
        }
    }

    fun removePost(picId: Int, absolutePath: String) {
        if (isCurrentUser) {
            ITEMS.removeAt(binarySearchIterative(ITEMS, picId))
            CoroutineScope(Dispatchers.IO).launch {
                imageDao.removePost(sharedPref.getString("publisherId")!!, picId)
                val target = File(absolutePath)
                Log.e(" target_path", "" + android.R.attr.path)
                if (target.exists() && target.isFile && target.canWrite()) {
                    target.delete()
                    Log.e("d_file", "" + target.name)
                }
            }
        }
    }

    private fun binarySearchIterative(input: MutableList<ImageModel>, picId: Int): Int {  // because when loading pictures from db to view we get them in ascending order of ids,
        var low = 0                                                                       // and when taking new pictures they are added in the same order we can use bsearch
        var high = input.size - 1                                                    // to find the index of the picture in arraylist
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