package com.example.socialmediaappv2.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data")
data class UserInfoModel(
    @PrimaryKey()
    @ColumnInfo(name = "id")
    var publisherId: String,

    @ColumnInfo(name = "display_name")
    var displayName: String,

    @ColumnInfo(name = "birth_date")
    var birthDate: String,

    @ColumnInfo(name = "bio")
    var bio: String,

    @ColumnInfo(name = "profile_pic_id")
    var profilePic: Int,

    @ColumnInfo(name = "posts")
    var posts: Int
)