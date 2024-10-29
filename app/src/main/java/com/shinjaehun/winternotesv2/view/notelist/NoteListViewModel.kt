package com.shinjaehun.winternotesv2.view.notelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shinjaehun.winternotesv2.common.BaseViewModel
import com.shinjaehun.winternotesv2.common.GET_NOTES_ERROR
import com.shinjaehun.winternotesv2.common.Result
import com.shinjaehun.winternotesv2.model.INoteRepository
import com.shinjaehun.winternotesv2.model.Note
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

private const val TAG = "NoteListViewModel"

class NoteListViewModel(
    val noteRepo: INoteRepository,
    uiContext: CoroutineContext
): BaseViewModel<NoteListEvent>(uiContext) {

    private val noteListState = MutableLiveData<List<Note>>()
    val noteList: LiveData<List<Note>> get() = noteListState

    private val editNoteState = MutableLiveData<String>()
    val editNote: LiveData<String> get() = editNoteState

    private val searchNoteListState = MutableLiveData<List<Note>>()
    val searchNoteList: LiveData<List<Note>> get() = searchNoteListState

    override fun handleEvent(event: NoteListEvent) {
        when (event) {
            is NoteListEvent.OnStart -> getNotes()
            is NoteListEvent.OnNoteItemClick -> editNote(event.position)
            is NoteListEvent.OnSearchTextChange -> changeSearchText(event.searchKeyword)
        }
    }

    private fun getNotes() = launch {
        val notesResult = noteRepo.getNotes()
        when (notesResult) {
            is Result.Value -> noteListState.value = notesResult.value
            is Result.Error -> errorState.value = GET_NOTES_ERROR
        }

    }

    private fun editNote(position: Int) {
//        editNoteState.value = noteList.value!![position].noteId
        editNoteState.value = noteList.value!![position].dateTime
    }

    private fun changeSearchText(searchKeyword: String) {
    }
}