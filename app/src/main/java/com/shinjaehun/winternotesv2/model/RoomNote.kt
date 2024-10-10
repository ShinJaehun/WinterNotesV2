package com.shinjaehun.winternotesv2.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "winter_notes",
    indices = [Index("noteId")]
)
data class RoomNote (
    @PrimaryKey(autoGenerate = true)
    val noteId: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "contents")
    val contents: String? = null,

    @ColumnInfo(name = "date_time")
    val dateTime: String,

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,

    @ColumnInfo(name = "color")
    val color: String? = null,

    @ColumnInfo(name = "web_link")
    val webLink: String? = null
)