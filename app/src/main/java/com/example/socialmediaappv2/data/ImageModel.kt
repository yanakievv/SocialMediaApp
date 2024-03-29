package com.example.socialmediaappv2.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_data")
data class ImageModel(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "picture_id")
    val picId: Int,

    @ColumnInfo(name = "publisher_id")
    val publisherId: String,

    @ColumnInfo(name = "image")
    val path: String,

    @ColumnInfo(name = "publisher_display_name")
    val publisherDisplayName: String,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "latitude")      //latitude and longitude are stored in decimal degrees, radian = dd * 0.0175
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "rotation")
    val rotation: Int
)