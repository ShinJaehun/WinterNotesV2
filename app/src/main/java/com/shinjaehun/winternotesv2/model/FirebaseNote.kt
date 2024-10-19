package com.shinjaehun.winternotesv2.model

data class FirebaseNote (
    val noteId: String? = "",
    val title: String? = "",
    val contents: String? = "",
    val dateTime: String? = "",
    val imageUrl: String? = "",
    val color: String? = "",
    val webLink: String? = "",
    val creator: String? = ""
)