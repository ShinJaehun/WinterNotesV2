package com.shinjaehun.winternotesv2.note.notedetail

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build.VERSION_CODES.P
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
import com.shinjaehun.winternotesv2.model.Note
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "NoteDetailView"

enum class ImageStatus {
    LOADED,
    CHANGED,
    DELETED,
    NULL
}

class NoteDetailView : Fragment() {

    private lateinit var binding: FragmentNoteDetailBinding
    private lateinit var viewModel: NoteDetailViewModel
    //    private var imageFilePath: String? = null // 이렇게 하는게 맞는지는 모르겠음...
    private var note: Note? = null
    private lateinit var imageStatus: ImageStatus

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
                Log.i(TAG, "ivNote tag: ${binding.ivNote.tag}")

                val title = binding.etNoteTitle.text.toString()
                val contents = binding.etNoteContent.text.toString()
                val gradientDrawable = binding.viewSubtitleIndicator.background as GradientDrawable
                val colorCode = String.format("#%06X", (0xFFFFFF and gradientDrawable.color!!.defaultColor))

                val webUrl = binding.tvWebUrl.text.toString()

//                if (viewModel.noteChanged.value == true) {
//
//                } else {
//                    Log.i(TAG, "노트를 업데이트 하려고 했는데 변한게 없네요...")
//                }

                // new or update note
                if (note!!.title == title &&
                    note!!.contents == contents &&
                    binding.ivNote.tag == null && // 여기에 뭐가 남아 있다면 uri가 변경된 거고... 이미지 변경한 거지
                    imageStatus != ImageStatus.DELETED &&
                    imageStatus != ImageStatus.CHANGED &&
                    note!!.color == colorCode &&
                    note!!.webLink == webUrl) {
                    // 하나도 바뀌지 않은 경우...
                    Log.i(TAG, "노트 업데이트하려고 했는데 변화 없음!")
                    findNavController().navigate(R.id.noteListView)
                } else {
                    // 뭔가 변경된 경우
                    Log.i(TAG, "변경/업데이트")

                    val selectedImageFilePath = if (binding.ivNote.tag != null) {
                        // 이미지 pick으로 URI가 존재
                        Log.i(TAG, "uri of tag: ${(binding.ivNote.tag as Uri).path.toString()}")
                        FileUtils.fileFromContentUri(requireActivity(), binding.ivNote.tag as Uri).path
                    } else if (imageStatus != ImageStatus.DELETED) {
                        // 이미지를 삭제하지 않은 상태(원래 이미지... 이게 new note인 경우 null일 수도 있음), 이미지 pick은 없음
                        note!!.imagePath
                    } else {
                        // 이미지 삭제를 비롯한 경우
                        null
                    }

                    // 이 로직이 동작하지 않는 이유는...
                    if (
                        note!!.imagePath != null &&
                        note!!.imagePath != selectedImageFilePath && // 예전 이미지와 달라짐
                        binding.ivNote.tag != null && // 이미지 pick된 상황
                        imageStatus == ImageStatus.CHANGED// 이미지 변경되어서 예전 이미지 필요 없어짐
                        ) {
                        if(File(note!!.imagePath!!).exists())
                            File(note!!.imagePath!!).delete()
                    }

                    Log.i(TAG, "imageStatus: $imageStatus")

                    viewModel.handleEvent(
                        NoteDetailEvent.OnDoneClick(
                            title = title,
                            contents = contents,
                            imagePath = selectedImageFilePath,
                            color = colorCode,
                            webLink = webUrl
                        )
                    )
                }

//                val selectedImagePath: String? = binding.ivNote.tag as String?
//                Log.i(TAG, "selectedImagePath: $selectedImagePath")

                // 이미지 선택하고/삭제할 때는 파일을 복사하지 말고
                // tag에 uri 정보를 넣어뒀다가 save 할 때만 파일 경로를 추출하고 싶었음!
                // 이렇게 했을 때 문제는
                // 이미지 선택 외에 정상적인 정보 갱신의 경우 tag에 아무것도 없기 때문에 이미지가 NULL이 되고 만다는 것!!!!
                // 방법이 없을까?

//                val selectedImageFile = if (binding.ivNote.tag != null) {
//                    Log.i(TAG, "uri of tag: ${(binding.ivNote.tag as Uri).path.toString()}")
//                    FileUtils.fileFromContentUri(requireActivity(), binding.ivNote.tag as Uri)
////                    Log.i(TAG, "selectedImageFile path: ${selectedImageFile.path}")
//                } else null

                //어디서 발생하는지 모르겠는데 uri로 바꾼 다음부터 노트 저장할 때 꼭 이런 오류메시지가...
                //ContextImpl             com.shinjaehun.winternotesv2         W  Failed to ensure /storage/0EF8-2D15/Android/data/com.shinjaehun.winternotesv2/files: android.os.ServiceSpecificException:  (code -22)

//                val gradientDrawable = binding.viewSubtitleIndicator.background as GradientDrawable
//                val colorCode = String.format("#%06X", (0xFFFFFF and gradientDrawable.color!!.defaultColor))

//                val webUrl = binding.tvWebUrl.text.toString().ifEmpty { null }

//                viewModel.handleEvent(
//                    NoteDetailEvent.OnDoneClick(
//                        title = binding.etNoteTitle.text.toString(),
//                        contents = binding.etNoteContent.text.toString(),
//                        imagePath = selectedImageFile?.path,
//                        color = colorCode,
//                        webLink = webUrl
//                    )
//                )
            }
        }

        initMisc()
    }

    private fun setTitleIndicatorColor(selectedColor: String) {
        val gradientDrawable = binding.viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedColor))
    }

    private fun showImageFromPath(path: String) {
        Log.i(TAG, "[showImageFromPath]path: $path")
        Log.i(TAG, "[showImageFromPath]path from uri: ${Uri.parse(path).path.toString()}")
        Log.i(TAG, "[showImageFromPath]uri of path: ${Uri.parse(path)}")

        binding.ivNote.setImageURI(Uri.parse(path))

        binding.ivNote.visibility = View.VISIBLE

        binding.ivDeleteImage.visibility = View.VISIBLE
        binding.ivDeleteImage.setOnClickListener {
            viewModel.handleEvent(
                NoteDetailEvent.OnNoteImageDeleteClick(path)
            )
        }
    }

//    private fun showImage(uri: Uri) {
//        binding.ivNote.setImageURI(uri)
//
//        binding.ivNote.visibility = View.VISIBLE
//        binding.ivNote.tag = uri
//
//        binding.ivDeleteImage.visibility = View.VISIBLE
//        binding.ivDeleteImage.setOnClickListener {
//            if(File(uri.path).exists()){
//                Log.i(TAG, "image deleted")
//                File(uri.path).delete()
//            }
//            viewModel.handleEvent(
//                NoteDetailEvent.OnNoteImageDeleteClick
//            )
//        }
//    }

    private fun showImageFromUri(uri: Uri) {
//  Selected URI: content://media/picker/0/com.android.providers.media.photopicker/media/1000000036
//  [showImageFromUri]uri: content://media/picker/0/com.android.providers.media.photopicker/media/1000000036
//  [showImageFromUri]path from uri: /picker/0/com.android.providers.media.photopicker/media/1000000036

        Log.i(TAG, "[showImageFromUri]uri: $uri")
        Log.i(TAG, "[showImageFromUri]path from uri: ${uri.path.toString()}")

        binding.ivNote.setImageURI(uri)

        binding.ivNote.visibility = View.VISIBLE
        binding.ivNote.tag = uri

        binding.ivDeleteImage.visibility = View.VISIBLE
        binding.ivDeleteImage.setOnClickListener {
            viewModel.handleEvent(
                NoteDetailEvent.OnNoteImageDeleteClick(null)
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
            Observer {
                note = it // 항상 받아오게 된다... noteId가 0인 경우에도 NewNote()를 실행하기 때문!
                if (note!!.imagePath != null){
                    imageStatus = ImageStatus.LOADED
                } else {
                    imageStatus = ImageStatus.NULL
                }

                Log.i(TAG, "note in observe: $note")
                binding.etNoteTitle.text = it.title.toEditable()
                binding.tvDateTime.text = it.dateTime

                if (!it.contents.isNullOrEmpty()) {
                    binding.etNoteContent.text = it.contents.toEditable()
                } else {
                    binding.etNoteContent.text = "".toEditable()
                }

                if (!it.color.isNullOrEmpty()){
                    when(it.color){
                        ColorPINK -> binding.misc.layoutMisc.findViewById<ImageView>(R.id.iv_color2).performClick()
                        ColorDARKBLUE -> binding.misc.layoutMisc.findViewById<ImageView>(R.id.iv_color3).performClick()
                        ColorYELLOW -> binding.misc.layoutMisc.findViewById<ImageView>(R.id.iv_color4).performClick()
                        ColorLIGHTBLUE -> binding.misc.layoutMisc.findViewById<ImageView>(R.id.iv_color5).performClick()
                        else -> binding.misc.layoutMisc.findViewById<ImageView>(R.id.iv_color1).performClick()
                    }
                    setTitleIndicatorColor(it.color)
                } else {
                    setTitleIndicatorColor(ColorBLACK)
                }

                if (!it.imagePath.isNullOrEmpty()) {
                    showImageFromPath(it.imagePath)
                }

                if (!it.webLink.isNullOrEmpty()) {
                    showWebLink(it.webLink)
                }

                if (it.noteId != "0") {
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
            Observer { imageUri ->
                if (imageUri != null) {
                    showImageFromUri(imageUri)
                    imageStatus = ImageStatus.CHANGED
                }
            }
        )

        viewModel.noteImageDeleted.observe(
            viewLifecycleOwner,
            Observer { imagePath ->
                // local에서만 해야 하는 작업
                if (imagePath != null) {
                    if(File(imagePath).exists()) {
                        File(imagePath).delete()
                    }
                    imageStatus = ImageStatus.DELETED
                } else {
                    Log.i(TAG, "이미지 파일이 없는 상황... 또는 uri만 주어지거나 ")
                }

//                if (note != null) {
//                    if (note!!.imagePath != null) {
//                        if (File(note!!.imagePath!!).exists()) {
//                            Log.i(TAG, "image exists!!")
//                            File(note!!.imagePath!!).delete()
//                            Log.i(TAG, "image deleted!")
//                        }
//                    } else {
//                        Log.i(TAG, "이미지 파일이 없는 상황... 또는 uri만 주어지거나 ")
//                    }
//                }

                binding.ivNote.visibility = View.GONE
                binding.ivDeleteImage.visibility = View.GONE

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
            Log.i(TAG, "[registerForActivityResult]uri: $uri")
            Log.i(TAG, "[registerForActivityResult]path from uri: ${uri.path.toString()}")

//            Log.i(TAG, "mime type: ${requireActivity().contentResolver.getType(uri)}")
//            Log.i(TAG, "file extensions: ${MimeTypeMap.getSingleton().getExtensionFromMimeType(requireActivity().contentResolver.getType(uri))}")
//            Log.d("PhotoPicker", "Selected URI: $uri")

//            val selectedImageFile =
//                    FileUtils.fileFromContentUri(requireActivity(), uri)
//                Log.i(TAG, "selectedImageFile path: ${selectedImageFile.path}")

            viewModel.handleEvent(
                NoteDetailEvent.OnNoteImageChange(uri)
            )

        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }


    private fun showErrorState(errorMessage: String) = makeToast(errorMessage)
}