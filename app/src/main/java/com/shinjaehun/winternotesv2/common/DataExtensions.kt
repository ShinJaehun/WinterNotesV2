package com.shinjaehun.winternotesv2.common

import android.text.Editable
import com.shinjaehun.winternotesv2.model.Note
import com.shinjaehun.winternotesv2.model.RoomNote

//internal suspend fun <T> awaitTaskResult(task: Task<T>)

internal val RoomNote.toNote: Note
    get() = Note(
        this.noteId.toString(),
        this.title,
        this.contents,
        this.dateTime,
        this.imagePath,
        this.color,
        this.webLink
    )

internal val Note.toRoomNote: RoomNote
    get() = RoomNote(
        this.noteId.toInt(),
        this.title,
        this.contents,
        this.dateTime,
        this.imagePath,
        this.color,
        this.webLink
    )

internal fun List<RoomNote>.toNoteListFromRoomNoteList(): List<Note> = this.flatMap {
    listOf(it.toNote)
}

internal fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
