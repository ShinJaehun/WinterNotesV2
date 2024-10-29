package com.shinjaehun.winternotesv2.view.notelist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.shinjaehun.winternotesv2.R
import com.shinjaehun.winternotesv2.common.makeToast
import com.shinjaehun.winternotesv2.common.toUser
import com.shinjaehun.winternotesv2.databinding.FragmentNoteListBinding
import com.shinjaehun.winternotesv2.model.User

private const val TAG = "NoteListView"

class NoteListView : Fragment() {

    private lateinit var binding: FragmentNoteListBinding
    private lateinit var viewModel: NoteListViewModel
    private lateinit var adapter: NoteListAdapter
    private var user: User? = null

//    private var timer: Timer? = null
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity?.finish()
        }

        binding = FragmentNoteListBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.notesRecyclerView.adapter = null
    }

    override fun onStart() {
        super.onStart()

        user = firebaseAuth.currentUser?.toUser

        viewModel = ViewModelProvider(
            this,
            NoteListInjector(requireActivity().application).provideNoteListViewModelFactory()
        ).get(
            NoteListViewModel::class.java
        )

        setupAdapter()
        observeViewModel()

        binding.fabAddNote.setOnClickListener {
//            viewModel.handleEvent(
//                NoteListEvent.OnNewNoteClick
//            )
            startNoteDetailWithArgs("")
        }

        viewModel.handleEvent(
            NoteListEvent.OnStart
        )
    }

    private fun setupAdapter() {
        adapter = NoteListAdapter()
        adapter.event.observe(
            viewLifecycleOwner,
            Observer {
//                Log.i(TAG, "what is it? $it")
                viewModel.handleEvent(it)
            }
        )
        binding.notesRecyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.notesRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.error.observe(
            viewLifecycleOwner,
            Observer { errorMessage ->
                showErrorState(errorMessage)
            }
        )

        viewModel.noteList.observe(
            viewLifecycleOwner,
            Observer { noteList ->
                Log.i(TAG, "noteList $noteList")
                adapter.submitList(noteList)
            }
        )

        viewModel.editNote.observe(
            viewLifecycleOwner,
            Observer { noteId ->
                startNoteDetailWithArgs(noteId)
            }
        )

        viewModel.searchNoteList.observe(
            viewLifecycleOwner,
            Observer { noteList ->
                adapter.submitList(noteList)
            }
        )
    }

    // 왜 이게 안 되는 거죠? 안드로이드 navigation은 병신인가요?
//    private fun startNoteDetailWithArgs(noteId: String?) = findNavController().navigate(
//        NoteListViewDirections.actionNoteListViewToNoteDetailView(noteId)
//    )

    private fun startNoteDetailWithArgs(noteId: String?) {
        val bundle = Bundle()
        bundle.putString("noteId", noteId)
        findNavController().navigate(R.id.noteDetailView, bundle)
    }

    private fun showErrorState(errorMessage: String?) = makeToast(errorMessage!!)

}