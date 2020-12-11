package com.example.socialmediaappv2.data.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.lang.NullPointerException

object StorageUtil {
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val currentUserRef: StorageReference
        get() = storageInstance.reference.child(FirebaseAuth.getInstance().uid ?: throw NullPointerException("FirebaseAuth.getInstance().uid returned null"))

    fun uploadPhoto(file: File) {
        val ref = currentUserRef.child("pictures/${file.name}")
        ref.putFile(Uri.fromFile(file)).addOnFailureListener {
            Log.e("StorageUtil", "Upload: ${it.message}")
        }
    }

    fun pathToReference(path: String) = storageInstance.getReference(path)
}