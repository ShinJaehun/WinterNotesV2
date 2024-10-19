package com.shinjaehun.winternotesv2.view.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shinjaehun.winternotesv2.model.INoteRepository
import kotlinx.coroutines.Dispatchers

class NoteListViewModelFactory(
    private val noteRepo: INoteRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteListViewModel(noteRepo, Dispatchers.Main) as T
    }
}