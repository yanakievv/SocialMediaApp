package com.example.socialmediaappv2.data

import com.example.socialmediaappv2.data.firebase.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}
    private val currentUserDoc: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().uid
            ?: throw NullPointerException("FirebaseAuth.getInstance().uid returned null")}")

    fun initUserFirstTime(onComplete: () -> Unit) {
        currentUserDoc.get().addOnSuccessListener {
            if (!it.exists()) {
                val firebaseCurrentUser = FirebaseAuth.getInstance().currentUser?: throw java.lang.NullPointerException("FirebaseAuth.getInstance().currentUser returned null")
                val newUser = UserModel(firebaseCurrentUser.displayName ?: "", "", "", null)
                currentUserDoc.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }
            else onComplete()
        }
    }

    fun updateCurrentUser(name: String = "", birth: String, bio: String = "", profilePicturePath: String? = null) {
        val fieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) fieldMap["name"] = name
        if (birth.isNotBlank()) fieldMap["birth"] = birth
        if (bio.isNotBlank()) fieldMap["bio"] = bio
        if (profilePicturePath != null) fieldMap["profilePic"]
    }
}