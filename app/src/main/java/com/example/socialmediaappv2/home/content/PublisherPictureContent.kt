package com.example.socialmediaappv2.home.content

import android.content.Context
import android.util.Log
import com.example.socialmediaappv2.data.*
import com.example.socialmediaappv2.home.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

object PublisherPictureContent {

    var IMAGES: MutableList<ImageBitmap> = ArrayList()
    var ITEMS: MutableList<ImageModel> = ArrayList()
    var TEMP: MutableList<ImageBitmap> = ArrayList()
    var initLoaded = false


    private lateinit var sharedPref: SharedPreference
    //private var userInfo: UserInfoModel? = null
    private lateinit var databaseInstance: UserDatabase
    private lateinit var userDAO: UserDAO
    private lateinit var imageDAO: ImageDAO


    var isCurrentUser: Boolean = false

    fun initLoadImagesFromDatabase(userId: String, context: Context) {
        Log.e("LOAD_FROM_USER", userId)

        sharedPref = SharedPreference(context)
        databaseInstance = UserDatabase.getInstance(context)
        imageDAO = databaseInstance.imageDAO
        userDAO = databaseInstance.userDAO

        IMAGES.clear()

        if (userId == sharedPref.getString("publisherId")!! && IMAGES != TEMP) {
            isCurrentUser = true
            IMAGES = TEMP
            Log.e("LOAD_QUANTITY", "load_from_temp")
        }
        else {
            CoroutineScope(Dispatchers.Main).launch {
                (context as HomeActivity).showSkeleton()
            }
            CoroutineScope(Dispatchers.IO).launch {
                ITEMS = imageDAO.getPublisherPosts(userId) as MutableList<ImageModel>
                for (i in ITEMS) {
                    val file = File(i.path)
                    if (file.exists()) {
                        IMAGES.add(ImageBitmap(i))
                        Log.e("IO","Loaded image ${i.picId}")
                    }
                }
                ITEMS.clear()
                CoroutineScope(Dispatchers.Main).launch {
                    (context as HomeActivity).hideSkeleton()
                }
                Log.e("LOAD_QUANTITY", "database_all_publisher_posts")
            }

            isCurrentUser = userId == sharedPref.getString("publisherId")
            initLoaded = true
            if (isCurrentUser) {
                TEMP = IMAGES
            }
            Log.e("SAME_USER", isCurrentUser.toString())
        }
    }

    fun loadRecentImages(userId: String, context: Context) {

        Log.e("CURRENT_USER", sharedPref.getString("publisherId") as String)
        Log.e("SAME_USER", isCurrentUser.toString())

        if (SharedPreference.imagesTaken > 0 && isCurrentUser) {
            Log.e("IMGS_TKN", SharedPreference.imagesTaken.toString())
            CoroutineScope(Dispatchers.Main).launch {
                (context as HomeActivity).showSkeleton()
            }
            CoroutineScope(Dispatchers.IO).launch {
                ITEMS = imageDAO.getPublisherLastPosts(
                    sharedPref.getString("publisherId") as String,
                    SharedPreference.imagesTaken
                ) as MutableList<ImageModel>
                SharedPreference.imagesTaken = 0
                for (i in ITEMS.reversed()) {
                    val file = File(i.path)
                    if (file.exists()) {
                        IMAGES.add(ImageBitmap(i))
                        Log.e("LOAD_FROM", "recent picture")
                        sharedPref.incInt("posts")
                    }
                }
                ITEMS.clear()
                CoroutineScope(Dispatchers.Main).launch {
                    (context as HomeActivity).notifyAdapter()
                    (context).hideSkeleton()
                }
            }
        }
        else if (!isCurrentUser) {
            initLoadImagesFromDatabase(userId, context)
        }
    }

    private fun updateUser() {
        val userInfo = UserInfoModel(sharedPref.getString("publisherId") as String,
            sharedPref.getString("displayName") as String,
            sharedPref.getString("birthDate") as String,
            sharedPref.getString("bio") as String,
            sharedPref.getInt("profilePic"),
            sharedPref.getInt("posts")
        )

        CoroutineScope(Dispatchers.IO).launch {
            userDAO.updateUser(userInfo)
        }
    }

    fun setProfilePicture(picId: Int) {
        if (isCurrentUser) {
            val userInfo = UserInfoModel(sharedPref.getString("publisherId") as String,
                sharedPref.getString("displayName") as String,
                sharedPref.getString("birthDate") as String,
                sharedPref.getString("bio") as String,
                picId,
                sharedPref.getInt("posts")
            )
            CoroutineScope(Dispatchers.IO).launch {
                userDAO.updateUser(userInfo)
            }
        }
    }

    fun removePost(picId: Int, absolutePath: String, context: Context) {
        if (isCurrentUser) {
            IMAGES.removeAt(binarySearchIterative(IMAGES, picId))
            CoroutineScope(Dispatchers.IO).launch {
                imageDAO.removePost(sharedPref.getString("publisherId")!!, picId)
                val target = File(absolutePath)
                Log.e(" target_path", "" + android.R.attr.path)
                if (target.exists() && target.isFile && target.canWrite()) {
                    target.delete()
                    Log.e("d_file", "" + target.name)
                }
                CoroutineScope(Dispatchers.Main).launch {
                    (context as HomeActivity).notifyAdapter()
                }
            }
            sharedPref.decInt("posts")
            updateUser()
        }
    }

    private fun binarySearchIterative(input: MutableList<ImageBitmap>, picId: Int): Int {  // because when loading pictures from db to view we get them in ascending order of ids,
        var low = 0                                                                       // and when taking new pictures they are added in the same order we can use bsearch
        var high = input.size - 1                                                    // to find the index of the picture in arraylist
        var mid: Int
        while (low <= high) {
            mid = low + ((high - low) / 2)
            when {
                picId > input[mid].imageModel.picId -> low = mid + 1
                picId == input[mid].imageModel.picId -> return mid
                picId < input[mid].imageModel.picId -> high = mid - 1
            }
        }
        return -1
    }

    fun nuke() {
        initLoaded = false
        TEMP.clear()
        IMAGES.clear()
    }
}