package com.example.socialmediaappv2.data

import android.service.autofill.UserData
import android.util.Log
import com.example.socialmediaappv2.data.firebase.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking

object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}
    private val currentUserDoc: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().uid
            ?: throw NullPointerException("FirebaseAuth.getInstance().uid returned null")}")

    fun initUserFirstTime(onComplete: () -> Unit) {
        currentUserDoc.get().addOnSuccessListener {
            if (!it.exists()) {
                val firebaseCurrentUser = FirebaseAuth.getInstance().currentUser?: throw java.lang.NullPointerException("FirebaseAuth.getInstance().currentUser returned null")
                val newUser = UserModel(firebaseCurrentUser.displayName ?: "", "Private", "", null, "0")
                currentUserDoc.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }
            else onComplete()
        }
    }

    fun updateCurrentUser(user: UserModel) {
        updateCurrentUser(user.name, user.birth, user.bio, user.profilePicturePath, user.posts)
    }

    fun updateCurrentUser(user: UserInfoModel, profilePicPath: String? = null) {
        updateCurrentUser(user.displayName, user.birthDate, user.bio, profilePicPath, user.posts.toString())
    }

    fun updateCurrentUser(name: String = "", birth: String = "", bio: String = "", profilePicturePath: String? = null, posts: String = "") {
        val fieldMap = mutableMapOf<String, Any>()

        if (name.isNotBlank()) fieldMap["name"] = name
        if (birth.isNotBlank()) fieldMap["birth"] = birth
        if (bio.isNotBlank()) fieldMap["bio"] = bio
        if (profilePicturePath != null) fieldMap["profilePicturePath"] = profilePicturePath
        if (posts.isNotBlank()) fieldMap["posts"] = posts
        currentUserDoc.update(fieldMap)
    }

    fun getCurrentUser(onComplete: (UserModel) -> Unit) {
        currentUserDoc.get().addOnSuccessListener {
            it.toObject(UserModel::class.java)?.let { it1 -> onComplete(it1) }
        }
    }

    fun getUID(): String {
        return currentUserDoc.id
    }
}