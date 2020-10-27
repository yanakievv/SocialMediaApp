package com.example.socialmediaappv2.home.content

import android.R
import android.media.ExifInterface
import android.util.Log
import com.example.socialmediaappv2.data.ImageModel
import com.example.socialmediaappv2.data.App.currentProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.util.ArrayList

object PictureContent {

    var ITEMS: MutableList<ImageModel> = ArrayList()
    var initLoaded = false


    fun initLoadImagesFromDatabase() {
        Log.e("LOADFROM", "database_all_posts")
        currentProfile.imageDao = currentProfile.databaseInstance.imageDAO
        runBlocking {
            ITEMS = currentProfile.imageDao.getPublisherPosts(currentProfile.currentUser.publisherId) as MutableList<ImageModel>
            initLoaded = true
        }

    }

    fun loadRecentImages() {
        if (currentProfile.imagesTaken > 0) {
            runBlocking {
                addImages(
                    currentProfile.imageDao.getPublisherLastPosts(
                        currentProfile.currentUser.publisherId,
                        currentProfile.imagesTaken
                    )
                )
            }
            currentProfile.imagesTaken = 0
        }

    }

    private fun addImage(image: ImageModel) {
        ITEMS.add(image)
        Log.e("LOADFROM", "camera_recent_post")
    }

    private fun addImages(images: List<ImageModel>) {
        for (i in images.reversed()) {
            addImage(i)
        }
    }

    fun setProfilePicture(picId: Int) {
        currentProfile.currentUser.profilePic = picId
        CoroutineScope(Dispatchers.IO).launch {
            currentProfile.userDao.updateUser(currentProfile.currentUser)
        }
    }

    fun removePost(picId: Int, absolutePath: String) {
        ITEMS.removeAt(binarySearchIterative(ITEMS, picId))
        CoroutineScope(Dispatchers.IO).launch {
            currentProfile.imageDao.removePost(currentProfile.currentUser.publisherId, picId)
            val target = File(absolutePath)
            Log.e(" target_path", "" + R.attr.path)
            if (target.exists() && target.isFile && target.canWrite()) {
                target.delete()
                Log.e("d_file", "" + target.name)
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
}