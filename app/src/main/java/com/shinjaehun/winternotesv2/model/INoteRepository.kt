package com.shinjaehun.winternotesv2.model

import com.shinjaehun.winternotesv2.common.Result

interface INoteRepository {
    suspend fun getNotes(): Result<Exception, List<Note>>
    suspend fun getNoteById(noteId: String): Result<Exception, Note>
    suspend fun deleteNote(note: Note): Result<Exception, Unit>
    suspend fun insertOrUpdateNote(note: Note): Result<Exception, Unit>
    suspend fun searchNote(keyword: String): Result<Exception, List<Note>>
}