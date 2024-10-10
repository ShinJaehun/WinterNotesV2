package com.shinjaehun.winternotesv2.note.notedetail

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.PickerMediaColumns.DISPLAY_NAME
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.shinjaehun.winternotesv2.R
import com.shinjaehun.winternotesv2.common.ColorBLACK
import com.shinjaehun.winternotesv2.common.ColorDARKBLUE
import com.shinjaehun.winternotesv2.common.ColorLIGHTBLUE
import com.shinjaehun.winternotesv2.common.ColorPINK
import com.shinjaehun.winternotesv2.common.ColorYELLOW
import com.shinjaehun.winternotesv2.common.FileUtils
import com.shinjaehun.winternotesv2.common.makeToast
import com.shinjaehun.winternotesv2.common.toEditable
import com.shinjaehun.winternotesv2.databinding.FragmentNoteDetailBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "NoteDetailView"

class NoteDetailView : Fragment() {

    private lateinit var binding: FragmentNoteDetailBinding
    private lateinit var viewModel: NoteDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.noteListView)
        }

        binding = FragmentNoteDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        viewModel = ViewModelProvider(
            this,
            NoteDetailInjector(requireActivity().application).provideNoteDetailViewModelFactory()
        ).get(NoteDetailViewModel::class.java)

        observeViewModel()

        viewModel.handleEvent(
            NoteDetailEvent.OnStart(
                arguments?.getString("noteId").toString()
            )
        )

        binding.ivBack.setOnClickListener {
            findNavController().navigate(R.id.noteListView)
        }

        binding.ivSave.setOnClickListener {
            if (binding.etNoteTitle.text.toString().trim().isEmpty()) {
                showErrorState("Note title can't be empty.")
            } else {

                val selectedImagePath: String? = binding.ivNote.tag as String?
                Log.i(TAG, "selectedImagePath: $selectedImagePath")

                val gradientDrawable = binding.viewSubtitleIndicator.background as GradientDrawable
                val colorCode = String.format("#%06X", (0xFFFFFF and gradientDrawable.color!!.defaultColor))

                val webUrl = binding.tvWebUrl.text.toString().ifEmpty { null }

                viewModel.handleEvent(
                    NoteDetailEvent.OnDoneClick(
                        title = binding.etNoteTitle.text.toString(),
                        contents = binding.etNoteContent.text.toString(),
                        imagePath = selectedImagePath,
                        color = colorCode,
                        webLink = webUrl
                    )
                )
            }
        }

        initMisc()
    }

    private fun setTitleIndicatorColor(selectedColor: String) {
        val gradientDrawable = binding.viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedColor))
    }

    private fun showImage(path: String) {

        binding.ivNote.setImageURI(Uri.parse(path))

        binding.ivNote.visibility = View.VISIBLE
        binding.ivNote.tag = path

        binding.ivDeleteImage.visibility = View.VISIBLE
        binding.ivDeleteImage.setOnClickListener {
            viewModel.handleEvent(
                NoteDetailEvent.OnNoteImageDeleteClick
            )
        }
    }

    private fun showWebLink(url: String) {
        binding.tvWebUrl.text = url
        binding.layoutWebUrl.visibility = View.VISIBLE
        binding.ivDeleteWebUrl.visibility = View.VISIBLE
        binding.ivDeleteWebUrl.setOnClickListener {
            viewModel.handleEvent(
                NoteDetailEvent.OnWebLinkDeleteClick
            )
        }
    }

    private fun observeViewModel() {
        viewModel.error.observe(
            viewLifecycleOwner,
            Observer { errorMessage ->
                showErrorState(errorMessage)
            }
        )

        viewModel.note.observe(
            viewLifecycleOwner,
            Observer { note ->
                binding.etNoteTitle.text = note.title.toEditable()
                binding.tvDateTime.text = note.dateTime

                if (!note.contents.isNullOrEmpty()) {
                    binding.etNoteContent.text = note.contents.toEditable()
                } else {
                    binding.etNoteContent.text = "".toEditable()
                }

                if (!note.color.isNullOrEmpty()){
                    when(note.color){
                       ColorPINK -> binding.misc.layoutMisc.findViewById<ImageView>(R.id.iv_color2).performClick()
                       ColorDARKBLUE -> binding.misc.layoutMisc.findViewById<ImageView>(R.id.iv_color3).performClick()
                       ColorYELLOW -> binding.misc.layoutMisc.findViewById<ImageView>(R.id.iv_color4).performClick()
                       ColorLIGHTBLUE -> binding.misc.layoutMisc.findViewById<ImageView>(R.id.iv_color5).performClick()
                       else -> binding.misc.layoutMisc.findViewById<ImageView>(R.id.iv_color1).performClick()
                    }
                    setTitleIndicatorColor(note.color)
                } else {
                    setTitleIndicatorColor(ColorBLACK)
                }

                if (!note.imagePath.isNullOrEmpty()) {
                    showImage(note.imagePath)
                }

                if (!note.webLink.isNullOrEmpty()) {
                    showWebLink(note.webLink)
                }

                if (note.noteId != "0") {
                    binding.misc.layoutDeleteNote.visibility = View.VISIBLE
                }
            }
        )

        viewModel.noteColor.observe(
            viewLifecycleOwner,
            Observer { noteColor ->
                if (!noteColor.isNullOrEmpty()){
                    setTitleIndicatorColor(noteColor)
                } else {
                    setTitleIndicatorColor(ColorBLACK)
                }
            }
        )

        viewModel.noteImage.observe(
            viewLifecycleOwner,
            Observer { imagePath ->
                if(!imagePath.isNullOrEmpty()) {
                    showImage(imagePath)
                } else {
                    binding.ivNote.visibility = View.GONE
                    binding.ivDeleteImage.visibility = View.GONE
                }
            }
        )

        viewModel.noteImageDeleted.observe(
            viewLifecycleOwner,
            Observer {
                binding.ivNote.visibility = View.GONE
                binding.ivDeleteImage.visibility = View.GONE

                if (File(binding.ivNote.tag as String).exists()) {
                    Log.i(TAG, "image deleted!")
                    File(binding.ivNote.tag as String).delete()
                }

                binding.ivNote.tag = null
            }
        )

        viewModel.webLink.observe(
            viewLifecycleOwner,
            Observer { webLink ->
                if (!webLink.isNullOrEmpty()) {
                    showWebLink(webLink)
                } // else?
            }
        )

        viewModel.webLinkDeleted.observe(
            viewLifecycleOwner,
            Observer {
                binding.tvWebUrl.text = ""
                binding.tvWebUrl.visibility = View.GONE
                binding.layoutWebUrl.visibility = View.GONE
            }
        )

        viewModel.updated.observe(
            viewLifecycleOwner,
            Observer {
                findNavController().navigate(R.id.noteListView)
            }
        )

        viewModel.deleted.observe(
            viewLifecycleOwner,
            Observer {
                findNavController().navigate(R.id.noteListView)
            }
        )
    }


    private fun initMisc() {
        val layoutMisc : LinearLayout = binding.misc.layoutMisc
        val bottomSheetBehavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(layoutMisc)

        binding.misc.tvMiscellaneous.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        val imageColor1 = binding.misc.ivColor1
        val imageColor2 = binding.misc.ivColor2
        val imageColor3 = binding.misc.ivColor3
        val imageColor4 = binding.misc.ivColor4
        val imageColor5 = binding.misc.ivColor5

        binding.misc.ivColor1.setOnClickListener {
            imageColor1.setImageResource(R.drawable.ic_done)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            viewModel.handleEvent(
                NoteDetailEvent.OnNoteColorChange(ColorBLACK)
            )
        }

        binding.misc.ivColor2.setOnClickListener {
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(R.drawable.ic_done)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            viewModel.handleEvent(
                NoteDetailEvent.OnNoteColorChange(ColorPINK)
            )
        }

        binding.misc.ivColor3.setOnClickListener {
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(R.drawable.ic_done)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            viewModel.handleEvent(
                NoteDetailEvent.OnNoteColorChange(ColorDARKBLUE)
            )
        }

        binding.misc.ivColor4.setOnClickListener {
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(R.drawable.ic_done)
            imageColor5.setImageResource(0)
            viewModel.handleEvent(
                NoteDetailEvent.OnNoteColorChange(ColorYELLOW)
            )
        }

        binding.misc.ivColor5.setOnClickListener {
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(R.drawable.ic_done)
            viewModel.handleEvent(
                NoteDetailEvent.OnNoteColorChange(ColorLIGHTBLUE)
            )
        }

        binding.misc.layoutAddImage.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.i(TAG, "mime type: ${requireActivity().contentResolver.getType(uri)}")
            Log.i(TAG, "file extensions: ${MimeTypeMap.getSingleton().getExtensionFromMimeType(requireActivity().contentResolver.getType(uri))}")
            Log.d("PhotoPicker", "Selected URI: $uri")

            val selectedImageFile =
                    FileUtils.fileFromContentUri(requireActivity(), uri)
                Log.i(TAG, "selectedImageFile path: ${selectedImageFile.path}")

            viewModel.handleEvent(
                NoteDetailEvent.OnNoteImageChange(selectedImageFile.path)
            )

//            var originFileName = ""
//            var originFileExtension = ""
//            var outputFileName = ""
//
//            uri.let {
//
//                requireActivity().contentResolver.query(it, null, null, null, null)
////                requireActivity().contentResolver.query(uri, arrayOf(DISPLAY_NAME), null, null, null)
//            }?.use { cursor ->
//                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
////                val nameIndex = cursor.getColumnIndexOrThrow(DISPLAY_NAME)
//                cursor.moveToFirst()
//                originFileName = cursor.getString(nameIndex)
//
//                Log.i(TAG, "$originFileName")
//
//                originFileExtension = originFileName.substringAfterLast('.', "")
//                originFileName = originFileName.substringBeforeLast(".", "")
//                val currentTime = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(
//                    Date(System.currentTimeMillis())
//                )
//                outputFileName = "$originFileName$currentTime.$originFileExtension"
//                Log.i(TAG, "outputFileName : $outputFileName")
//
//                try {
//                    FileUtils.application = requireActivity().application
//                    FileUtils.cRes = requireActivity().contentResolver
//
//                    val inputStream = FileUtils.getInputStream(uri)
//                    val path = requireActivity().getExternalFilesDir(null)
//                    val folder = File(path, "images")
//                    folder.mkdirs()
//                    val outputFile = File(folder, outputFileName)
//                    FileUtils.copyStreamToFile(inputStream!!, outputFile)
//
//                    showImage(outputFile.path)
//
//                    viewModel.handleEvent(
//                        NoteDetailEvent.OnNoteImageChange(outputFile.path)
//                    )
//                } catch (e: Exception) {
//                    showErrorState(e.toString())
//                }
//            }

        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }


    private fun showErrorState(errorMessage: String) = makeToast(errorMessage)
}