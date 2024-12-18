package com.shinjaehun.winternotesv2.view.notedetail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.shinjaehun.winternotesv2.common.BaseViewModel
import com.shinjaehun.winternotesv2.common.FileUtils
import com.shinjaehun.winternotesv2.common.GET_NOTE_ERROR
import com.shinjaehun.winternotesv2.common.Result
import com.shinjaehun.winternotesv2.common.awaitTaskResult
import com.shinjaehun.winternotesv2.common.currentTime
import com.shinjaehun.winternotesv2.common.toUser
import com.shinjaehun.winternotesv2.model.INoteRepository
import com.shinjaehun.winternotesv2.model.Note
import com.shinjaehun.winternotesv2.model.User
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

private const val TAG = "NoteDetailViewModel"

class NoteDetailViewModel(
    val noteRepo: INoteRepository,
    uiContext: CoroutineContext
) : BaseViewModel<NoteDetailEvent>(uiContext) {

    private val noteState = MutableLiveData<Note>()
    val note: LiveData<Note> get() = noteState

//    private val noteImageState = MutableLiveData<String?>()
//    val noteImage: LiveData<String?> get() = noteImageState

    private val noteImageState = MutableLiveData<Uri?>()
    val noteImage: LiveData<Uri?> get() = noteImageState

//    private val noteImageDeletedState = MutableLiveData<String?>()
//    val noteImageDeleted: LiveData<String?> get() = noteImageDeletedState

//    private val noteImageDeletedState = MutableLiveData<Uri?>()
//    val noteImageDeleted: LiveData<Uri?> get() = noteImageDeletedState

    private val noteImageDeletedState = MutableLiveData<Boolean>()
    val noteImageDeleted: LiveData<Boolean> get() = noteImageDeletedState

    private val noteColorState = MutableLiveData<String?>()
    val noteColor: LiveData<String?> get() = noteColorState

    private val webLinkState = MutableLiveData<String?>()
    val webLink: LiveData<String?> get() = webLinkState

    private val webLinkDeletedState = MutableLiveData<Boolean>()
    val webLinkDeleted: LiveData<Boolean> get() = webLinkDeletedState

    private val deletedState = MutableLiveData<Boolean>()
    val deleted: LiveData<Boolean> get() = deletedState

    private val updatedState = MutableLiveData<Boolean>()
    val updated: LiveData<Boolean> get() = updatedState

//    private val noteChangedState = MutableLiveData<Boolean>()
//    val noteChanged: LiveData<Boolean> get() = noteChangedState

//    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
//    private var user: User? = null
//
//    init {
//        user = getActiveUser()
//    }

//    private fun getActiveUser(): User? {
//        return firebaseAuth.currentUser?.toUser
//    }

    override fun handleEvent(event: NoteDetailEvent) {
        when (event){
            is NoteDetailEvent.OnStart -> getNote(event.noteId)
            is NoteDetailEvent.OnDoneClick -> updateNote(event.title, event.contents, event.imagePath, event.color, event.webLink)
//            is NoteDetailEvent.OnDoneClick -> updateNote(event.title, event.contents, event.imageUri, event.color, event.webLink)
            is NoteDetailEvent.OnNoteImageChange -> changeNoteImage(event.imageUri)
            is NoteDetailEvent.OnNoteImageDeleteClick -> onNoteImageDelete()
//            is NoteDetailEvent.OnNoteImageDeleteClick -> onNoteImageDelete(event.imageUri)
            is NoteDetailEvent.OnNoteColorChange -> changeNoteColor(event.color)
            is NoteDetailEvent.OnWebLinkChange -> changeWebLink(event.webLink)
            is NoteDetailEvent.OnWebLinkDeleteClick -> onWebLinkDelete()
            is NoteDetailEvent.OnDeleteClick -> onDelete()
        }
    }

//    private fun onNoteChanged(changed: Boolean) {
//        noteChangedState.value = changed
//    }

    private fun getNote(noteId: String) = launch {
//        if (noteId == "0" || noteId.isNullOrEmpty()) {
        if (noteId == "") {
            newNote()
        } else {
            val noteResult = noteRepo.getNoteById(noteId)
            when (noteResult) {
                is Result.Value -> noteState.value = noteResult.value
                is Result.Error -> errorState.value = GET_NOTE_ERROR
            }
        }
    }

    private fun newNote() {
        noteState.value =
            Note("", "", currentTime(), null, null, null, null)
    }

    private fun updateNote(
        title: String,
        contents: String?,
        imagePath: String?,
//        imageUri: Uri?,
        color: String?,
        webLink: String?
    ) = launch {
//        Log.i(TAG, "note before save: ${note.value!!}")

        val updateResult = noteRepo.insertOrUpdateNote(
            note.value!!.copy(
                title = title,
                contents = contents,
//                dateTime = currentTime(),
                imagePath = imagePath,
                color = color,
                webLink = webLink
            )
        )

        when(updateResult){
            is Result.Value -> updatedState.value = true
            is Result.Error -> updatedState.value = false
        }
    }

//    private fun changeNoteImage(imagePath: String?) {
//        noteImageState.value = imagePath
//    }

    private fun changeNoteImage(imageUri: Uri?) {
        noteImageState.value = imageUri
    }

    private fun onNoteImageDelete() {
//    private fun onNoteImageDelete(imageUri: Uri?) {
        noteImageDeletedState.value = true
//        noteImageDeletedState.value = imageUri
//        launch {
//            //로컬 이미지처리 때문에 좀 복잡하게 움직임...
////            noteRepo.insertOrUpdateNote(note.value!!.copy(imagePath = null))
//            noteRepo.insertOrUpdateNote(note.value!!.copy(imageUri = null))
//        }
    }

    private fun changeNoteColor(color: String?) {
        noteColorState.value = color
    }

    private fun changeWebLink(webLink: String?) {
        webLinkState.value = webLink
    }

    private fun onWebLinkDelete() {
        webLinkDeletedState.value = true
    }

    private fun onDelete() = launch {
        val deletedResult = noteRepo.deleteNote(note.value!!)
        when(deletedResult){
            is Result.Value -> deletedState.value = true
            is Result.Error -> deletedState.value = false
        }
    }
}