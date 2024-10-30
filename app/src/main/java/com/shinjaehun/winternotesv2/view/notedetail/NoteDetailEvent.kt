package com.shinjaehun.winternotesv2.view.notedetail

import android.net.Uri

sealed class NoteDetailEvent {
    data class OnStart(val noteId: String): NoteDetailEvent()

    data class OnDoneClick(
        val title: String,
        val contents: String?,
        val imagePath: String?,
//        val imageUri: Uri?,
        val color: String?,
        val webLink: String?
    ): NoteDetailEvent()

//    data class OnNoteImageChange(val imagePath: String?): NoteDetailEvent()
    data class OnNoteImageChange(val imageUri: Uri?): NoteDetailEvent()
//    data class OnNoteImageDeleteClick(val imagePath: String?): NoteDetailEvent()
//    data class OnNoteImageDeleteClick(val imageUri: Uri?): NoteDetailEvent()
    data object OnNoteImageDeleteClick: NoteDetailEvent()

    data class OnNoteColorChange(val color: String?): NoteDetailEvent()

    data class OnWebLinkChange(val webLink: String?): NoteDetailEvent()
    data object OnWebLinkDeleteClick: NoteDetailEvent()

    data object OnDeleteClick: NoteDetailEvent()
}