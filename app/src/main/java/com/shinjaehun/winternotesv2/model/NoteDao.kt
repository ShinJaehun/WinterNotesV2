package com.shinjaehun.winternotesv2.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NoteDao {
    @Query("SELECT * FROM winter_notes ORDER BY date_time DESC")
    suspend fun getNotes(): List<RoomNote>

    @Query("SELECT * FROM winter_notes WHERE noteId = :noteId")
    suspend fun getNoteById(noteId: String): RoomNote

    @Query("SELECT * FROM winter_notes WHERE title LIKE '%' || :keyword || '%' OR contents LIKE '%' || :keyword || '%'")
    suspend fun searchNote(keyword: String): List<RoomNote>

    @Delete
    suspend fun deleteNote(note: RoomNote)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNote(note: RoomNote): Long
}