package com.example.socialmediaappv2.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_data")
data class ImageModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "image_id")
    val imageId: Int = 0,

    @ColumnInfo(name = "image_list")
    val image: String,

    @ColumnInfo(name = "publisher_id")
    val publisherId: String,

    @ColumnInfo(name = "publisher_display_name")
    val publisherDisplayName: String,

    @ColumnInfo(name = "date")
    val date: String

)