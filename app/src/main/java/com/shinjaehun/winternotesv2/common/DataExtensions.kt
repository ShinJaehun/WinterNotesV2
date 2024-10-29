package com.shinjaehun.winternotesv2.common

import android.text.Editable
import androidx.core.net.toUri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.shinjaehun.winternotesv2.model.FirebaseNote
import com.shinjaehun.winternotesv2.model.Note
import com.shinjaehun.winternotesv2.model.RoomNote
import com.shinjaehun.winternotesv2.model.User
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal suspend fun <T> awaitTaskResult(task: Task<T>): T = suspendCoroutine { continuation ->
    task.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result!!)
        } else {
            continuation.resumeWithException(task.exception!!)
        }
    }
}

internal suspend fun <T> awaitTaskCompletable(task: Task<T>): Unit = suspendCoroutine { continuation ->
    task.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(Unit)
        } else {
            continuation.resumeWithException(task.exception!!)
        }
    }
}

internal val FirebaseUser.toUser: User
    get() = User(
        uid = this.uid,
        email = this.email ?: ""
    )

internal val FirebaseNote.toNote: Note
    get() = Note(
        this.title ?: "",
        this.contents ?: "",
        this.dateTime ?: "",
        this.imageUrl ?: "",
        this.color ?: "",
        this.webLink ?: "",
        User(this.creator ?: "")
    )

internal val Note.toFirebaseNote: FirebaseNote
    get() = FirebaseNote(
        this.title,
        this.contents,
        this.dateTime,
        this.imagePath,
        this.color,
        this.webLink,
        this.safeGetUid
    )

internal val Note.safeGetUid: String
    get() = this.creator?.uid ?: ""

//internal val RoomNote.toNote: Note
//    get() = Note(
//        this.title,
//        this.contents,
//        this.dateTime,
//        this.imagePath,
////        this.imagePath?.toUri(),
//        this.color,
//        this.webLink,
//        User(this.creatorId)
//    )
//
//internal val Note.toRoomNote: RoomNote
//    get() = RoomNote(
//        noteId
//        this.title,
//        this.contents,
//        this.dateTime,
//        this.imagePath,
//        this.color,
//        this.webLink,
//        this.safeGetUid
//    )

//internal fun List<RoomNote>.toNoteListFromRoomNoteList(): List<Note> = this.flatMap {
//    listOf(it.toNote)
//}

internal fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
