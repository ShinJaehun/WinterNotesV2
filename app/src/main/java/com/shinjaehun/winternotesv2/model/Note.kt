package com.shinjaehun.winternotesv2.model

import android.net.Uri

data class Note (
    val title: String,
    val contents: String?,
    val dateTime: String,
    val imagePath: String?,
//    val imageUri: Uri?,
    val color: String?,
    val webLink: String?,
    val creator: User?
)