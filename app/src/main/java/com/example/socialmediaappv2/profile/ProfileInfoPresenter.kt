package com.example.socialmediaappv2.profile

import android.content.Context
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.data.*
import kotlinx.coroutines.runBlocking

class ProfileInfoPresenter(var view: Contract.ProfileView?): Contract.ProfileInfoPresenter  {

    private lateinit var userInfo: UserInfoModel
    private lateinit var databaseInstance: UserDatabase
    private lateinit var userDao: UserDAO
    private lateinit var imageDao: ImageDAO
    private lateinit var sharedPref: SharedPreference

    private var isCurrentUser: Boolean = false

    override fun init(userId: String, context: Context) {
        sharedPref = SharedPreference(context)
        databaseInstance = UserDatabase.getInstance(context)
        userDao = databaseInstance.userDAO
        imageDao = databaseInstance.imageDAO
        runBlocking {  userInfo = userDao.getUser(userId) }
        isCurrentUser = (sharedPref.getString("publisherId") == userId)
    }

    override fun reInit(id: String) {
        if (isCurrentUser) {
            runBlocking {
                userInfo = userDao.getUser(id)
            }
        }
    }

    override fun refreshDb() {
        if (isCurrentUser) {
            runBlocking{ userDao.updateUser(userInfo) }
            sharedPref.save("publisherId", userInfo.publisherId)
            sharedPref.save("displayName", userInfo.displayName)
            sharedPref.save("birthDate", userInfo.birthDate)
            sharedPref.save("bio", userInfo.bio)
            sharedPref.save("posts", userInfo.posts)
        }
    }

    override fun getDisplayName(): String {
        return userInfo.displayName
    }

    override fun setDisplayName(name: String) {
        userInfo.displayName = name
    }

    override fun getBirthDate(): String {
        return userInfo.birthDate
    }

    override fun setBirthDate(birthDate: String) {
        userInfo.birthDate = birthDate
    }

    override fun getBio(): String {
        return userInfo.bio
    }

    override fun setBio(bio: String) {
        userInfo.bio = bio
    }

    override fun getPictures(): List<ImageModel>? {
        var pics: List<ImageModel>? = null
        runBlocking { pics = imageDao.getPublisherPosts(userInfo.publisherId) }
        return pics
    }

    override fun getProfilePic(): ImageModel? {
        var image: ImageModel? = null
        runBlocking { image = imageDao.getProfilePicture(userInfo.profilePic) }
        return image
    }

    override fun getNumberOfPosts(): Int {
        return userInfo.posts
    }

    override fun isCurrentProfile(): Boolean {
        return isCurrentUser
    }

    override fun onDestroy() {
        view = null
    }
}