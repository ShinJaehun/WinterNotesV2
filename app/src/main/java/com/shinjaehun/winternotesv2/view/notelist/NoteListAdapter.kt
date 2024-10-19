package com.shinjaehun.winternotesv2.view.notelist

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shinjaehun.winternotesv2.common.ColorBLACK
import com.shinjaehun.winternotesv2.common.simpleDate
import com.shinjaehun.winternotesv2.databinding.ItemContainerNoteBinding
import com.shinjaehun.winternotesv2.model.Note

class NoteListAdapter(
    val event: MutableLiveData<NoteListEvent> = MutableLiveData()
): ListAdapter<Note, NoteListAdapter.NoteViewHolder>(NoteDiffUtilCallback()) {


    inner class NoteViewHolder(val binding: ItemContainerNoteBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return NoteViewHolder(
            ItemContainerNoteBinding.inflate(inflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        getItem(position).let { note ->
            with(holder){

                binding.tvTitle.text = note.title
                binding.tvDateTime.text = simpleDate(note.dateTime)

                if (note.imagePath.isNullOrEmpty()) {
                    binding.rivImagePreview.visibility = View.GONE
                } else {
                    binding.rivImagePreview.setImageURI(Uri.parse(note.imagePath))
                    binding.rivImagePreview.visibility = View.VISIBLE
                }

                val gradientDrawable = binding.layoutNote.background as GradientDrawable
                if (note.color.isNullOrEmpty()) {
                    gradientDrawable.setColor(Color.parseColor(ColorBLACK))
                } else {
                    gradientDrawable.setColor(Color.parseColor(note.color))
                }

                binding.layoutNote.setOnClickListener {
                    event.value = NoteListEvent.OnNoteItemClick(position)
                }
            }

        }
    }

}