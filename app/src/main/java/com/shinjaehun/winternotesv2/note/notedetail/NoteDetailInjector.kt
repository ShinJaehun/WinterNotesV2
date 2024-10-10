package com.shinjaehun.winternotesv2.note.notedetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.shinjaehun.winternotesv2.model.INoteRepository
import com.shinjaehun.winternotesv2.model.NoteRepoImpl
import com.shinjaehun.winternotesv2.model.RoomNoteDatabase

class NoteDetailInjector(application: Application): AndroidViewModel(application) {
    private fun getNoteRepository(): INoteRepository {
        return NoteRepoImpl(
            local = RoomNoteDatabase.getInstance(getApplication()).roomNoteDao()
        )
    }

    fun provideNoteDetailViewModelFactory(): NoteDetailViewModelFactory =
        NoteDetailViewModelFactory(getNoteRepository())
}