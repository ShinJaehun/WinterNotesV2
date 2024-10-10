package com.shinjaehun.winternotesv2.note.notelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.shinjaehun.winternotesv2.model.INoteRepository
import com.shinjaehun.winternotesv2.model.NoteRepoImpl
import com.shinjaehun.winternotesv2.model.RoomNoteDatabase

class NoteListInjector(application: Application): AndroidViewModel(application) {
    private fun getNoteRepository(): INoteRepository {
        return NoteRepoImpl(
            local = RoomNoteDatabase.getInstance(getApplication()).roomNoteDao()
        )
    }

    fun provideNoteListViewModelFactory(): NoteListViewModelFactory =
        NoteListViewModelFactory(getNoteRepository())
}