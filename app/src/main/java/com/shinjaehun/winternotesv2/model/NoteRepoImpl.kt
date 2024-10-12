package com.shinjaehun.winternotesv2.model

import com.shinjaehun.winternotesv2.common.Result
import com.shinjaehun.winternotesv2.common.toNote
import com.shinjaehun.winternotesv2.common.toNoteListFromRoomNoteList
import com.shinjaehun.winternotesv2.common.toRoomNote

class NoteRepoImpl(
    val local: NoteDao
): INoteRepository {
    override suspend fun getNotes(): Result<Exception, List<Note>> {
        return getLocalNotes()
    }

    override suspend fun getNoteById(noteId: String): Result<Exception, Note> {
        return getLocalNote(noteId)
    }

    override suspend fun deleteNote(note: Note): Result<Exception, Unit> {
        return deleteLocalNote(note)
    }

    override suspend fun insertOrUpdateNote(note: Note): Result<Exception, Unit> {
        return insertOrUpdateLocalNote(note)
    }

    override suspend fun searchNote(keyword: String): Result<Exception, List<Note>> {
        return searchLocalNote(keyword)
    }

    private suspend fun getLocalNotes(): Result<Exception, List<Note>> = Result.build {
        local.getNotes().toNoteListFromRoomNoteList()
    }

    private suspend fun getLocalNote(noteId: String): Result<Exception, Note> = Result.build {
        local.getNoteById(noteId).toNote
    }

    private suspend fun deleteLocalNote(note: Note): Result<Exception, Unit> = Result.build {
        local.deleteNote(note.toRoomNote)
        Unit
    }

    private suspend fun insertOrUpdateLocalNote(note: Note): Result<Exception, Unit> = Result.build {
        local.insertOrUpdateNote(note.toRoomNote)
        Unit
    }

    private suspend fun searchLocalNote(keyword: String): Result<Exception, List<Note>> = Result.build {
        local.searchNote(keyword).toNoteListFromRoomNoteList()
    }
}