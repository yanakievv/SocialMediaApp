package com.example.socialmediaappv2.contract

import android.content.Context
import android.graphics.Bitmap
import com.example.socialmediaappv2.data.ImageModel
import com.example.socialmediaappv2.data.UserInfoModel

interface Contract {
    interface MainView: BaseView<UserInfoPresenter> {

    }
    interface UserInfoPresenter: BasePresenter {

        suspend fun init(id: String, displayName: String, context: Context)
        fun reInit(id: String)
        fun refreshDb()
        fun getUserPosts(): List<ImageModel>?
        fun addPost(newPost: Bitmap)

        fun getCurrentUser(): UserInfoModel

    }
}