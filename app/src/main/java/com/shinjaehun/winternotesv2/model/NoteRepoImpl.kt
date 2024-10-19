package com.shinjaehun.winternotesv2.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.shinjaehun.winternotesv2.common.Result
import com.shinjaehun.winternotesv2.common.awaitTaskResult
import com.shinjaehun.winternotesv2.common.toNote
import com.shinjaehun.winternotesv2.common.toNoteListFromRoomNoteList
import com.shinjaehun.winternotesv2.common.toRoomNote
import com.shinjaehun.winternotesv2.common.toUser

private const val COLLECTION_NAME = "notes"

class NoteRepoImpl(
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    val remote: FirebaseFirestore = FirebaseFirestore.getInstance(),
    val local: NoteDao
): INoteRepository {
    override suspend fun getNotes(): Result<Exception, List<Note>> {
        val user = getActiveUser()
        return if (user != null) getRemoteNotes(user)
        else getLocalNotes()
    }

    private fun getActiveUser(): User? {
        return firebaseAuth.currentUser?.toUser
    }

    private suspend fun getRemoteNotes(user: User): Result<Exception, List<Note>> {
        return try {
            val task = awaitTaskResult(
                remote.collection(COLLECTION_NAME)
                    .whereEqualTo("creator", user.uid)
                    .get()
            )
            resultToNoteList(task)
        } catch (e: Exception) {
            Result.build { throw e }
        }
    }

    private fun resultToNoteList(result: QuerySnapshot?): Result<Exception, List<Note>> {
        val noteList = mutableListOf<Note>()
        result?.forEach { documentSnapshot ->
            noteList.add(documentSnapshot.toObject(FirebaseNote::class.java).toNote)
        }
        return Result.build { noteList }
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