package com.example.socialmediaappv2.data

import androidx.room.*

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: UserInfoModel)

    @Query("SELECT * FROM user_data WHERE id = :id")
    suspend fun getUser(id: String): UserInfoModel

    @Update
    suspend fun updateUser(newInfo: UserInfoModel)

    @Query("SELECT EXISTS(SELECT 1 FROM user_data WHERE id = :id LIMIT 1)")
    suspend fun checkUser(id: String) : Int

    @Query("UPDATE user_data SET display_name = :newName WHERE id = :id")
    suspend fun setDisplayName(id: String, newName: String)

    @Query("UPDATE user_data SET birth_date = :newDate WHERE id = :id")
    suspend fun setBirthDate(id: String, newDate: String)

    @Query("UPDATE user_data SET bio = :newBio WHERE id = :id")
    suspend fun setBio(id: String, newBio: String)

    @Query("UPDATE user_data SET profile_pic_id = :newPic WHERE id = :id")
    suspend fun setProfilePic(id: String, newPic: Int)
}