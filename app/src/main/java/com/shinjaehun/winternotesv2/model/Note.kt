package com.shinjaehun.winternotesv2.model

data class Note (
    val noteId: String,
    val title: String,
    val contents: String?,
    val dateTime: String,
    val imagePath: String?,
    val color: String?,
    val webLink: String?,
    val creator: User?
)