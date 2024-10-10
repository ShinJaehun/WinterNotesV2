package com.shinjaehun.winternotesv2.note.notelist

sealed class NoteListEvent {
    object OnStart: NoteListEvent()
//    object OnNewNoteClick: NoteListEvent()
    data class OnNoteItemClick(val position: Int): NoteListEvent()
    data class OnSearchTextChange(val searchKeyword: String): NoteListEvent()
}