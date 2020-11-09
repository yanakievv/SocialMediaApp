package com.example.socialmediaappv2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addImage(image: ImageModel)

    @Query("SELECT * FROM image_data")
    suspend fun selectAll(): List<ImageModel>

    @Query("SELECT * FROM image_data WHERE picture_id = :picId")
    suspend fun getProfilePicture(picId: Int): ImageModel

    @Query("SELECT * FROM image_data WHERE publisher_id = :publisherId")
    suspend fun getPublisherPosts(publisherId: String): List<ImageModel>

    @Query("SELECT * FROM image_data WHERE publisher_id = :publisherId ORDER BY picture_id DESC LIMIT :count")
    suspend fun getPublisherLastPosts(publisherId: String, count: Int): List<ImageModel>

    @Query("SELECT * FROM image_data WHERE publisher_id != :publisherId")
    suspend fun getPosts(publisherId: String): List<ImageModel>

    @Query("SELECT COUNT(*) FROM image_data WHERE publisher_id = :publisherId")
    suspend fun getNumberOfPosts(publisherId: String) : Int

    @Query("DELETE FROM image_data WHERE picture_id = :picId AND publisher_id = :publisherId")
    suspend fun removePost(publisherId: String, picId: Int)

    @Query("DELETE FROM image_data")
    suspend fun nukeAll()
}