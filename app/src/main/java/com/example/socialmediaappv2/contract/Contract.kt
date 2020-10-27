package com.example.socialmediaappv2.contract

import android.content.Context
import android.graphics.Bitmap
import com.example.socialmediaappv2.data.App
import com.example.socialmediaappv2.data.ImageModel
import com.example.socialmediaappv2.data.UserInfoModel

interface Contract {
    interface ProfileView: BaseView<ProfileInfoPresenter> {
        fun update()
    }
    interface MainView: BaseView<UserInfoPresenter> {

    }
    interface UserInfoPresenter: BasePresenter {

        suspend fun init(id: String, displayName: String, context: Context)
        fun reInit(id: String)
        fun refreshDb()

        fun getUserPosts(): List<ImageModel>?
        fun addPost(newPost: String, latLong: DoubleArray)

        fun getCurrentUser(): UserInfoModel

        fun setProfilePicture(picId: Int)


    }
    interface ProfileInfoPresenter: BasePresenter {

        suspend fun init(userId: String, context: Context)
        fun reInit(id: String)
        fun refreshDb()

        fun getDisplayName(): String
        fun setDisplayName(name: String)

        fun getBirthDate(): String
        fun setBirthDate(birthDate: String)

        fun getBio(): String
        fun setBio(bio: String)

        fun getPictures(): List<ImageModel>?
        fun getProfilePic(): ImageModel?

        fun getNumberOfPosts(): Int
        fun isCurrentProfile(): Boolean
    }
}