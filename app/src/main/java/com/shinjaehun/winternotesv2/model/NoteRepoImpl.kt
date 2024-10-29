package com.shinjaehun.winternotesv2.model

import android.net.Uri
import android.os.Build.VERSION_CODES.P
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shinjaehun.winternotesv2.common.Result
import com.shinjaehun.winternotesv2.common.awaitTaskCompletable
import com.shinjaehun.winternotesv2.common.awaitTaskResult
import com.shinjaehun.winternotesv2.common.toFirebaseNote
import com.shinjaehun.winternotesv2.common.toNote
import com.shinjaehun.winternotesv2.common.toUser
import java.io.File

private const val COLLECTION_NAME = "notes"
private const val TAG = "NoteRepoImpl"

class NoteRepoImpl(
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    val remote: FirebaseFirestore = FirebaseFirestore.getInstance(),
    val storageReference: StorageReference = FirebaseStorage.getInstance().reference,
    val local: NoteDao
): INoteRepository {
    override suspend fun getNotes(): Result<Exception, List<Note>> {
        val user = getActiveUser()
        return getRemoteNotes(user!!)

//        return getLocalNotes()

//        val user = getActiveUser()
//        return if (user != null) getRemoteNotes(user)
//        else getLocalNotes()
    }

    override suspend fun getNoteById(noteId: String): Result<Exception, Note> {
        val user = getActiveUser()
        return getRemoteNote(noteId, user)

//        return getLocalNote(noteId)
    }

    override suspend fun deleteNote(note: Note): Result<Exception, Unit> {
        val user = getActiveUser()
        return deleteRemoteNote(note.copy(creator = user))

//        return deleteLocalNote(note)

//        val user = getActiveUser()
//        return if (user != null) deleteRemoteNote(note.copy(creator = user))
//        else deleteLocalNote(note)
    }

    override suspend fun insertOrUpdateNote(note: Note): Result<Exception, Unit> {
        val user = getActiveUser()
        return updateRemoteNote(note.copy(creator = user))

//        return insertOrUpdateLocalNote(note)

//        val user = getActiveUser()
//        return if (user != null) updateRemoteNote(note.copy(creator = user))
//        else insertOrUpdateLocalNote(note)
    }

//    override suspend fun searchNote(keyword: String): Result<Exception, List<Note>> {
//        return searchLocalNote(keyword)
//    }

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
//        Log.i(TAG, "noteList: $noteList")
        return Result.build { noteList }
    }

    private suspend fun getRemoteNote(dateTime: String, user: User?): Result<Exception, Note> {
        return try {
            val task = awaitTaskResult(
                remote.collection(COLLECTION_NAME)
                    .document(dateTime + user!!.uid)
                    .get()
            )

            Result.build {
                task.toObject(FirebaseNote::class.java)?.toNote ?: throw Exception()
            }
        } catch (e: Exception) {
            Result.build { throw e }
        }
    }


    private suspend fun deleteRemoteNote(note: Note): Result<Exception, Unit> = Result.build {
        awaitTaskCompletable(
            remote.collection(COLLECTION_NAME)
                .document(note.dateTime + note.creator!!.uid)
                .delete()
        )
    }

    private suspend fun updateRemoteNote(note: Note): Result<Exception, Unit> {
        return try {

//            if(note.imagePath != null){
//                Log.i(TAG, "note.imagePaht: ${note.imagePath}")
////                val originFileExtension = note.imagePath.substringAfterLast('.', "")
////                val originFileName = note.imagePath.substringBeforeLast(".", "")
//
////                var file = Uri.fromFile(File(note.imagePath))
//                val file = note.imagePath.toUri()
//
//                val task = awaitTaskResult(
//                    storageReference.child("images/${file.lastPathSegment}")
//                        .putFile(file)
//                )
//
//                task.storage.downloadUrl.addOnSuccessListener {
//                    Log.i(TAG, "download url: ${it.toString()}")
//                }
//
////                awaitTaskCompletable(
////                    remote.collection(COLLECTION_NAME)
////                        .document(note.dateTime + note.creator!!.uid)
////                        .set(note.toFirebaseNote)
////                )
//
//            } else {
//                awaitTaskCompletable(
//                    remote.collection(COLLECTION_NAME)
//                        .document(note.dateTime + note.creator!!.uid)
//                        .set(note.toFirebaseNote)
//                )
//            }
            awaitTaskCompletable(
                remote.collection(COLLECTION_NAME)
                    .document(note.dateTime + note.creator!!.uid)
                    .set(note.toFirebaseNote)
            )
            Result.build { Unit }
        } catch (e: Exception) {
            Result.build { throw e }
        }
    }

//    private suspend fun getLocalNotes(): Result<Exception, List<Note>> = Result.build {
//        local.getNotes().toNoteListFromRoomNoteList()
//    }
//
//    private suspend fun getLocalNote(noteId: String): Result<Exception, Note> = Result.build {
//        local.getNoteById(noteId).toNote
//    }
//
//    private suspend fun deleteLocalNote(note: Note): Result<Exception, Unit> = Result.build {
//        local.deleteNote(note.toRoomNote)
//        Unit
//    }
//
//    private suspend fun insertOrUpdateLocalNote(note: Note): Result<Exception, Unit> = Result.build {
//        local.insertOrUpdateNote(note.toRoomNote)
//        Unit
//    }
//
//    private suspend fun searchLocalNote(keyword: String): Result<Exception, List<Note>> = Result.build {
//        local.searchNote(keyword).toNoteListFromRoomNoteList()
//    }
}