package com.shinjaehun.winternotesv2.model

import android.net.Uri

data class FirebaseNote (
    val title: String? = "",
    val contents: String? = "",
    val dateTime: String? = "",
    val imageUrl: String? = "",
    val color: String? = "",
    val webLink: String? = "",
    val creator: String? = ""
)