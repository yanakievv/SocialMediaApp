package com.example.socialmediaappv2.home.content

import android.util.Log
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.data.ImageModel
import com.example.socialmediaappv2.data.App.currentProfile
import kotlinx.coroutines.runBlocking
import java.util.ArrayList

object PictureContent {

    var ITEMS: MutableList<ImageModel> = ArrayList()
    var initLoaded = false



    fun initLoadImagesFromDatabase() {
        Log.e("LOADFROM", "database_all_posts")
        currentProfile.imageDao = currentProfile.databaseInstance.imageDAO
        runBlocking {
            ITEMS =
                currentProfile.imageDao.getPublisherPosts(currentProfile.currentUser.publisherId) as MutableList<ImageModel>
            initLoaded = true
        }

    }

    fun loadRecentImages() {
        if (currentProfile.imagesTaken > 0) {
            runBlocking {
               addImages(currentProfile.imageDao.getLastPosts(currentProfile.currentUser.publisherId, currentProfile.imagesTaken))
            }
            currentProfile.imagesTaken = 0
        }

    }

    fun addImage(image: ImageModel) {
        ITEMS.add(image)
        Log.e("LOADFROM", "camera_recent_post")
    }

    fun addImages(images: List<ImageModel>) {
        for (i in images.reversed()) {
            addImage(i)
        }
    }

}