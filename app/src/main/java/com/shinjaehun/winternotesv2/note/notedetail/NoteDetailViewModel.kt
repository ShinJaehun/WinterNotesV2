package com.shinjaehun.winternotesv2.note.notedetail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shinjaehun.winternotesv2.common.BaseViewModel
import com.shinjaehun.winternotesv2.common.FileUtils
import com.shinjaehun.winternotesv2.common.GET_NOTE_ERROR
import com.shinjaehun.winternotesv2.common.Result
import com.shinjaehun.winternotesv2.common.currentTime
import com.shinjaehun.winternotesv2.model.INoteRepository
import com.shinjaehun.winternotesv2.model.Note
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

    private val noteImageDeletedState = MutableLiveData<String?>()
    val noteImageDeleted: LiveData<String?> get() = noteImageDeletedState

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

    override fun handleEvent(event: NoteDetailEvent) {
        when (event){
            is NoteDetailEvent.OnStart -> getNote(event.noteId)
            is NoteDetailEvent.OnDoneClick -> updateNote(event.title, event.contents, event.imagePath, event.color, event.webLink)
            is NoteDetailEvent.OnNoteImageChange -> changeNoteImage(event.imageUri)
            is NoteDetailEvent.OnNoteImageDeleteClick -> onNoteImageDelete(event.imagePath)
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
        if (noteId == "0" || noteId.isNullOrEmpty()) {
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
            Note("0", "", "", currentTime(), null, null, null)
//        noteChangedState.value = true
    }

    private fun updateNote(
        title: String,
        contents: String?,
        imagePath: String?,
        color: String?,
        webLink: String?
    ) = launch {
        Log.i(TAG, "note before save: ${note.value!!}")

//        val updateResult = noteRepo.insertOrUpdateNote(
//            note.value!!.copy(
//                title = title,
//                contents = contents,
//                dateTime = currentTime(),
//                imagePath = if (imagePath == null && note.value!!.imagePath != null) note.value!!.imagePath else imagePath,
//                color = color,
//                webLink = webLink
//            )
//        )

        val updateResult = noteRepo.insertOrUpdateNote(
            note.value!!.copy(
                title = title,
                contents = contents,
                dateTime = currentTime(),
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


    private fun onNoteImageDelete(imagePath: String?) {
        noteImageDeletedState.value = imagePath
        launch {
            //로컬 이미지처리 때문에 좀 복잡하게 움직임...
            noteRepo.insertOrUpdateNote(note.value!!.copy(imagePath = null))
        }
//        noteState.value = note.value!!.copy(imagePath=null)

//        launch {
//            val updateResult = noteRepo.insertOrUpdateNote(note.value!!.copy(imagePath = null))
//            when (updateResult) {
//                is Result.Value -> updatedState.value = true
//                is Result.Error -> updatedState.value = false
//            }
//        }
        // 이렇게 하면 뷰에서 viewModel.updated.observe 가 트리거 -> noteListView로 navigate
    }

    private fun changeNoteColor(color: String?) {
        noteColorState.value = color
//        note.value!!.copy(color = color) // 뭐 이런 식으로 바로 note를 저장한다?

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