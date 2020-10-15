package com.example.socialmediaappv2.contract

import android.content.Context
import android.graphics.Bitmap
import com.example.socialmediaappv2.data.ImageModel
import com.example.socialmediaappv2.data.UserInfoModel

interface Contract {
    interface ProfileView: BaseView<ProfileInfoPresenter> {
        fun inflate()
    }
    interface MainView: BaseView<UserInfoPresenter> {

    }
    interface UserInfoPresenter: BasePresenter {

        suspend fun init(id: String, displayName: String, context: Context)
        fun reInit(id: String)
        fun refreshDb()

        fun getUserPosts(): List<ImageModel>?
        fun addPost(newPost: Bitmap)
        fun addPost(newPost: String)

        fun getCurrentUser(): UserInfoModel

    }
    interface ProfileInfoPresenter: BasePresenter {

        suspend fun init(id: String, displayName: String, context: Context)
        fun reInit(id: String)
        fun refreshDb()

        fun getBirthDate(): String
        fun setBirthDate(birthDate: String)

        fun getBio(): String
        fun setBio(bio: String)

        fun getPicture(): String
    }
}