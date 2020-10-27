package com.example.socialmediaappv2.data

import androidx.room.*

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

    @Query("SELECT * FROM image_data WHERE acos(sin(:lat)*sin(radians(latitude)) + cos(:lat)*cos(radians(latitude))*cos(radians(longitude)-:long)) * 6371 < :radius AND publisher_id != :publisherId") // 6371 = radius of Earth
    suspend fun getPostsInRadius(publisherId: String, long: Double, lat: Double, radius: Int): List<ImageModel> //rework to save the sin and cos in db

    @Query("DELETE FROM image_data WHERE picture_id = :picId AND publisher_id = :publisherId")
    suspend fun removePost(publisherId: String, picId: Int)

    @Query("DELETE FROM image_data")
    suspend fun nukeAll()
}