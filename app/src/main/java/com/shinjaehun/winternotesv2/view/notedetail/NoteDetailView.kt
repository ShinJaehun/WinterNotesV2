package com.shinjaehun.winternotesv2.view.notedetail

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil3.load
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shinjaehun.winternotesv2.R
import com.shinjaehun.winternotesv2.common.ColorBLACK
import com.shinjaehun.winternotesv2.common.ColorDARKBLUE
import com.shinjaehun.winternotesv2.common.ColorLIGHTBLUE
import com.shinjaehun.winternotesv2.common.ColorPINK
import com.shinjaehun.winternotesv2.common.ColorYELLOW
import com.shinjaehun.winternotesv2.common.FileUtils
import com.shinjaehun.winternotesv2.common.ImageStatus
import com.shinjaehun.winternotesv2.common.awaitTaskResult
import com.shinjaehun.winternotesv2.common.makeToast
import com.shinjaehun.winternotesv2.common.toEditable
import com.shinjaehun.winternotesv2.common.toUser
import com.shinjaehun.winternotesv2.databinding.FragmentNoteDetailBinding
import com.shinjaehun.winternotesv2.model.Note
import com.shinjaehun.winternotesv2.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "NoteDetailView"

class NoteDetailView : Fragment() {

    private lateinit var binding: FragmentNoteDetailBinding
    private lateinit var viewModel: NoteDetailViewModel
    private var note: Note? = null
    private lateinit var imageStatus: ImageStatus

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

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

                // remote에서만 동작하는 코드

                // new or update note
                if (note!!.title == title &&
                    note!!.contents == contents &&
                    note!!.color == colorCode &&
                    note!!.webLink == webUrl &&
                    binding.ivNote.tag == null && // 여기에 뭐가 남아 있다면 uri가 변경된 거고... 이미지 변경한 거지
                    imageStatus != ImageStatus.DELETED
                ) {
                    // 하나도 바뀌지 않은 경우...
                    Log.i(TAG, "노트 업데이트하려고 했는데 변화 없음!")
                    Log.i(TAG, "imageStatus: $imageStatus")

                    findNavController().navigate(R.id.noteListView)
                } else {
                    // 뭔가 변경된 경우
                    Log.i(TAG, "변경/업데이트")
                    Log.i(TAG, "imageStatus: $imageStatus")

                    if (binding.ivNote.tag != null) {
                        if (imageStatus == ImageStatus.CHANGED && !note!!.imagePath.isNullOrEmpty()) {
                            //헐 note!!.imagePath != null 이렇게 imagePath의 상태 확인 불가
                            storage.getReferenceFromUrl(note!!.imagePath!!)
                                .delete()
                                .addOnSuccessListener {
                                    Log.i(
                                        TAG,
                                        "note update: 이미지 바꿨는데 예전에 이미지가 남아 있어서 예전 이미지를 삭제했어요..."
                                    )
                                }
                                .addOnFailureListener {
                                    Log.i(TAG, "이미지 삭제 실패")
                                }
                        }

                        val fileUri = binding.ivNote.tag as Uri // tag not null

                        // awaitTaskResult()를 사용하는 식으로 변경할 수 없을까요?
                        storage.reference.child("images/${fileUri.lastPathSegment}")
                            .putFile(fileUri)
                            .addOnSuccessListener { task ->
                                task.storage.downloadUrl.addOnSuccessListener {
                                    Log.i(TAG, "download url: ${it.toString()}")
                                    viewModel.handleEvent(
                                        NoteDetailEvent.OnDoneClick(
                                            title = title,
                                            contents = contents,
                                            imagePath = it.toString(),
                                            color = colorCode,
                                            webLink = webUrl
                                        )
                                    )
                                    Log.i(TAG, "note update: 이미지 업로드... ")
                                }
                            }
                            .addOnFailureListener {
                                Log.i(TAG, "이미지 업로드 실패?")
                            }

                    } else if (imageStatus == ImageStatus.LOADED) {
                        viewModel.handleEvent(
                            NoteDetailEvent.OnDoneClick(
                                title = title,
                                contents = contents,
                                imagePath = note!!.imagePath,
                                color = colorCode,
                                webLink = webUrl
                            )
                        )
                        Log.i(TAG, "note update: 원래 이미지로 ...")
                    } else {
                        if (imageStatus == ImageStatus.DELETED && !note!!.imagePath.isNullOrEmpty()) {
                            storage.getReferenceFromUrl(note!!.imagePath!!)
                                .delete()
                                .addOnSuccessListener {
                                    Log.i(TAG, "이미지 삭제했는데 예전에 이미지가 남아 있어서 예전 이미지를 삭제했어요...")
                                    imageStatus = ImageStatus.DELETED
                                }
                                .addOnFailureListener {
                                    Log.i(TAG, "이미지 삭제 실패")
                                }
                        }

                        viewModel.handleEvent(
                            NoteDetailEvent.OnDoneClick(
                                title = title,
                                contents = contents,
                                imagePath = null,
                                color = colorCode,
                                webLink = webUrl
                            )
                        )
                        Log.i(TAG, "note update: 이미지 null ...")
                    }

                    //                    val selectedImageFilePath = if (binding.ivNote.tag != null) {
//                        Log.i(TAG, "새로운 이미지 생성됨")
//                        // 이미지 pick으로 URI가 존재, 새로운 이미지 생성
////                        Log.i(TAG, "uri of tag: ${(binding.ivNote.tag as Uri).path.toString()}")
//                        if (imageStatus == ImageStatus.CHANGED && note!!.imagePath != null) {
//                            if (File(note!!.imagePath!!).exists()) {
//                                File(note!!.imagePath!!).delete()
//                                Log.i(TAG, "이미지 바꿨는데 예전에 이미지가 남아 있어서 예전 이미지를 삭제했어요...")
//                            }
//                        }
//                        FileUtils.fileFromContentUri(requireActivity(), binding.ivNote.tag as Uri).path
//                    } else if (imageStatus == ImageStatus.LOADED) {
//                        // 이미지를 삭제하지 않은 상태(원래 이미지)
//                        Log.i(TAG, "예전 이미지 그대로 가기")
//                        note!!.imagePath
//                    } else if (imageStatus == ImageStatus.NULL) {
//                        // 이미지가 없었어요
//                        Log.i(TAG, "예전 이미지 없었음")
//                        null
//                    } else {
//                        Log.i(TAG, "selectedImageFilePath가 null인디 이렇게 해도 되나요? 아마 이미지 삭제된 상태일 듯")
//                        null
//                    }


                }


//                // new or update note
//                if (note!!.title == title &&
//                    note!!.contents == contents &&
//                    note!!.color == colorCode &&
//                    note!!.webLink == webUrl &&
//                    binding.ivNote.tag == null
////                    binding.ivNote.tag == null && // 여기에 뭐가 남아 있다면 uri가 변경된 거고... 이미지 변경한 거지
////                    imageStatus != ImageStatus.DELETED
//                ) {
//                    // 하나도 바뀌지 않은 경우...
//                    Log.i(TAG, "노트 업데이트하려고 했는데 변화 없음!")
////                    Log.i(TAG, "imageStatus: $imageStatus")
//
//                    findNavController().navigate(R.id.noteListView)
//                } else {
//                    // 뭔가 변경된 경우
//                    Log.i(TAG, "변경/업데이트")

                    // local에서만 완벽하게 동작하는 코드
//                    val selectedImageFilePath = if (binding.ivNote.tag != null) {
//                        Log.i(TAG, "새로운 이미지 생성됨")
//                        // 이미지 pick으로 URI가 존재, 새로운 이미지 생성
////                        Log.i(TAG, "uri of tag: ${(binding.ivNote.tag as Uri).path.toString()}")
//                        if (imageStatus == ImageStatus.CHANGED && note!!.imagePath != null) {
//                            if (File(note!!.imagePath!!).exists()) {
//                                File(note!!.imagePath!!).delete()
//                                Log.i(TAG, "이미지 바꿨는데 예전에 이미지가 남아 있어서 예전 이미지를 삭제했어요...")
//                            }
//                        }
//                        FileUtils.fileFromContentUri(requireActivity(), binding.ivNote.tag as Uri).path
//                    } else if (imageStatus == ImageStatus.LOADED) {
//                        // 이미지를 삭제하지 않은 상태(원래 이미지)
//                        Log.i(TAG, "예전 이미지 그대로 가기")
//                        note!!.imagePath
//                    } else if (imageStatus == ImageStatus.NULL) {
//                        // 이미지가 없었어요
//                        Log.i(TAG, "예전 이미지 없었음")
//                        null
//                    } else {
//                        Log.i(TAG, "selectedImageFilePath가 null인디 이렇게 해도 되나요? 아마 이미지 삭제된 상태일 듯")
//                        null
//                    }

//                    // remote에서만 완벽하게 동작하는 코드
//                    val file = binding.ivNote.tag as Uri
//                    // awaitTaskResult()를 사용하는 식으로 변경할 수 없을까요?
//                    val ref = storageReference.child("images/${file.lastPathSegment}")
//                    ref.putFile(file)
//                        .addOnSuccessListener { task ->
//                            task.storage.downloadUrl.addOnSuccessListener {
//                                Log.i(TAG, "download url: ${it.toString()}")
//                                viewModel.handleEvent(
//                                    NoteDetailEvent.OnDoneClick(
//                                        title = title,
//                                        contents = contents,
//                                        imagePath = it.toString(),
//                                        color = colorCode,
//                                        webLink = webUrl
//                                    )
//                                )
//                            }
//                        }

                    // local / remote에서 동시에 동작할 수 있게 하려면?

//                    if (binding.ivNote.tag != null) {
//                        val file = binding.ivNote.tag as Uri
//                        if (user != null) {
//                            Log.i(TAG, "일단 파일 업로드!")
//
//                            // awaitTaskResult()를 사용하는 식으로 변경할 수 없을까요?
//                            val ref = storageReference.child("images/${file.lastPathSegment}")
//                            ref.putFile(file)
//                                .addOnSuccessListener { task ->
//                                    task.storage.downloadUrl.addOnSuccessListener {
//                                        Log.i(TAG, "download url: ${it.toString()}")
//                                        viewModel.handleEvent(
//                                            NoteDetailEvent.OnDoneClick(
//                                                title = title,
//                                                contents = contents,
//                                                imagePath = it.toString(),
//                                                color = colorCode,
//                                                webLink = webUrl
//                                            )
//                                        )
//                                    }
//                                }
//
////                            viewLifecycleOwner.lifecycleScope.launch {
////                                val task = awaitTaskResult(
////                                    storageReference.child("images/${file.lastPathSegment}")
////                                        .putFile(file)
////                                )
////                                task.storage.downloadUrl.addOnSuccessListener {
////                                    Log.i(TAG, "download url: ${it.toString()}")
////                                }
////                            }
////                            null
//
//                        } else {
//                            Log.i(TAG, "일단 파일 복사!")
//                            val selectedFilePath = FileUtils.fileFromContentUri(requireActivity(), binding.ivNote.tag as Uri).path
//                            viewModel.handleEvent(
//                                NoteDetailEvent.OnDoneClick(
//                                    title = title,
//                                    contents = contents,
//                                    imagePath = selectedFilePath,
//                                    color = colorCode,
//                                    webLink = webUrl
//                                )
//                            )
//                        }
//                    } else {
//                        Log.i(TAG, "이미지 파일 없음!")
//                        viewModel.handleEvent(
//                            NoteDetailEvent.OnDoneClick(
//                                title = title,
//                                contents = contents,
//                                imagePath = null,
//                                color = colorCode,
//                                webLink = webUrl
//                            )
//                        )
//                    }

                    // 업로드가 끝나지 않았기 때문에 파일 업로드 중이라도 null이라고 나옴...
//                    Log.i(TAG, "selectedImagePath: $selectedImagePath")

//                    viewModel.handleEvent(
//                        NoteDetailEvent.OnDoneClick(
//                            title = title,
//                            contents = contents,
////                            imagePath = selectedImageFilePath,
//                            imageUri = selectedImagePath,
//                            color = colorCode,
//                            webLink = webUrl
//                        )
//                    )
//                }

            }
        }

        initMisc()
    }

    private fun setTitleIndicatorColor(selectedColor: String) {
        val gradientDrawable = binding.viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedColor))
    }

//    private fun showImageFromPath(path: String) {
////        Log.i(TAG, "[showImageFromPath]path: $path")
////        Log.i(TAG, "[showImageFromPath]path from uri: ${Uri.parse(path).path.toString()}")
////        Log.i(TAG, "[showImageFromPath]uri of path: ${Uri.parse(path)}")
//
////        if (user != null) {
////            binding.ivNote.load(path)
////        } else {
////            binding.ivNote.setImageURI(Uri.parse(path))
////        }
//
//        binding.ivNote.setImageURI(Uri.parse(path))
//        binding.ivNote.visibility = View.VISIBLE
//
//        binding.ivDeleteImage.visibility = View.VISIBLE
//        // FB에서 이미지 파일 삭제는 어떻게 하지?
//        binding.ivDeleteImage.setOnClickListener {
//            viewModel.handleEvent(
//                NoteDetailEvent.OnNoteImageDeleteClick(Uri.parse(path))
//            )
//        }
//    }

    private fun showImageFromUrl(url: String) {
        binding.ivNote.load(url)
        binding.ivNote.visibility = View.VISIBLE

        binding.ivDeleteImage.visibility = View.VISIBLE
        // FB에서 이미지 파일 삭제는 어떻게 하지?
        binding.ivDeleteImage.setOnClickListener {
            viewModel.handleEvent(
                NoteDetailEvent.OnNoteImageDeleteClick
            )
        }
    }

    private fun showImageFromUri(uri: Uri) {
        binding.ivNote.setImageURI(uri)

        binding.ivNote.visibility = View.VISIBLE
        binding.ivNote.tag = uri

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
            Observer {
                note = it // 항상 받아오게 된다... noteId가 0인 경우에도 NewNote()를 실행하기 때문!
                if (note!!.imagePath != null){
                    imageStatus = ImageStatus.LOADED
                } else {
                    imageStatus = ImageStatus.NULL
                }

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
                    showImageFromUrl(it.imagePath)
                }

                if (!it.webLink.isNullOrEmpty()) {
                    showWebLink(it.webLink)
                }

                binding.misc.layoutDeleteNote.visibility = View.VISIBLE
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
            Observer {
                // 여기에 이미지를 삭제하는 로직이 있어서 그래...
                // 이미지 삭제 + note update는 동시에 이루어져야 함!
//                if (imagePath != null) {
//                    storage.getReferenceFromUrl(imagePath)
//                        .delete()
//                        .addOnSuccessListener {
//                            Log.i(TAG,"이미지 삭제...")
//                        }
//                        .addOnFailureListener {
//                            Log.i(TAG, "이미지 삭제 실패")
//                        }
//                        // 중요
//                        // 이미지 삭제한 다음에 뒤로가기 버튼을 누르면..
//                        // 이미지는 삭제되고 링크만 남게 되어서 문제가 생김
//                        imageStatus = ImageStatus.DELETED
//                } else {
//                    if(imageStatus == ImageStatus.CHANGED && note!!.imagePath != null) {
//                        storage.getReferenceFromUrl(note!!.imagePath!!)
//                            .delete()
//                            .addOnSuccessListener {
//                                Log.i(TAG, "이미지 바꿨는데 예전에 이미지가 남아 있어서 예전 이미지를 삭제했어요...")
//                                imageStatus = ImageStatus.DELETED
//                            }
//                            .addOnFailureListener {
//                                Log.i(TAG, "이미지 삭제 실패")
//                            }
//                    }
//                }
                imageStatus = ImageStatus.DELETED

                binding.ivNote.tag = null

                binding.ivNote.visibility = View.GONE
                binding.ivDeleteImage.visibility = View.GONE

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
                // FB에서 이미지 파일 삭제는 어떻게 하지?
//                if (note!!.imagePath != null) { // //헐 note!!.imagePath != null 이렇게 imagePath의 상태 확인 불가
                if (!note!!.imagePath.isNullOrEmpty()) {
                    storage.getReferenceFromUrl(note!!.imagePath!!)
                        .delete()
                        .addOnSuccessListener {
                            Log.i(TAG,"note 삭제하면서 이미지 삭제...")
                        }
                        .addOnFailureListener {
                            Log.i(TAG, "이미지 삭제 실패")
                        }
                }
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

        binding.misc.layoutAddUrl.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showAddURLDialog()
        }

        binding.misc.layoutDeleteNote.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showDeleteNoteDialog()
        }
    }

    private fun showAddURLDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val v: View = LayoutInflater.from(requireContext()).inflate(
            R.layout.layout_add_url, view?.findViewById(R.id.layout_addUrlContainer)
        )
        builder.setView(v)
        val dialogAddURL: AlertDialog = builder.create()
        if (dialogAddURL.window != null) {
            dialogAddURL.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        val inputURL = v.findViewById<EditText>(R.id.et_url)
        inputURL.requestFocus()

        v.findViewById<TextView>(R.id.tv_AddUrl).setOnClickListener {
            if(inputURL.text.toString().trim().isEmpty()){
                showErrorState("Enter URL")
            } else if (!Patterns.WEB_URL.matcher(inputURL.text.toString()).matches()) {
                showErrorState("Enter valid URL")
            } else {
                viewModel.handleEvent(
                    NoteDetailEvent.OnWebLinkChange(inputURL.text.toString().trim())
                )
                dialogAddURL.dismiss()
            }
        }

        v.findViewById<TextView>(R.id.tv_AddUrl_Cancel).setOnClickListener {
            dialogAddURL.dismiss()
        }

        dialogAddURL.show()
    }

    private fun showDeleteNoteDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val v: View = LayoutInflater.from(requireContext()).inflate(
            R.layout.layout_delete_note, view?.findViewById(R.id.layout_DeleteNoteContainer)
        )
        builder.setView(v)
        val dialogDeleteNote: AlertDialog = builder.create()
        if (dialogDeleteNote.window != null) {
            dialogDeleteNote.window!!.setBackgroundDrawable(ColorDrawable(0))
        }

        v.findViewById<TextView>(R.id.tv_DeleteNote).setOnClickListener {
            viewModel.handleEvent(
                NoteDetailEvent.OnDeleteClick
            )
            dialogDeleteNote.dismiss()
        }

        v.findViewById<TextView>(R.id.tv_DeleteNote_Cancel).setOnClickListener {
            dialogDeleteNote.dismiss()
        }

        dialogDeleteNote.show()
    }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
//            Log.i(TAG, "[registerForActivityResult]uri: $uri")
//            Log.i(TAG, "[registerForActivityResult]path from uri: ${uri.path.toString()}")

            Log.i(TAG, "pick uri: $uri")
//            fileInfo(requireActivity(), uri)

            viewModel.handleEvent(
                NoteDetailEvent.OnNoteImageChange(uri)
            )
            imageStatus = ImageStatus.CHANGED
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    // 결국 이렇게 해도 media picker는 uri만 전달할 뿐!
//    private fun fileInfo(context: Context, uri: Uri) {
//        val skipQuery = if(Build.VERSION.SDK_INT <= 32) {
//            ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED
//        } else false
//
////        if(skipQuery) {
////            return emptyList()
////        }
//
//        val queryUri = if(Build.VERSION.SDK_INT >= 29) {
//            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        } else MediaStore.Files.getContentUri("external")
//
//        val projection = arrayOf(
//            MediaStore.Files.FileColumns._ID,
//            MediaStore.Files.FileColumns.DISPLAY_NAME,
//            MediaStore.Files.FileColumns.MIME_TYPE,
//        )
//
//        context.contentResolver.query(
//            queryUri,
//            projection,
//            null,
//            null,
//            null
//        )?.use { cursor ->
//            val idColumn = cursor.getColumnIndexOrThrow(
//                MediaStore.Files.FileColumns._ID
//            )
//            val nameColumn = cursor.getColumnIndexOrThrow(
//                MediaStore.Files.FileColumns.DISPLAY_NAME
//            )
//            val mimeTypeColumn = cursor.getColumnIndexOrThrow(
//                MediaStore.Files.FileColumns.MIME_TYPE
//            )
//
//            while (cursor.moveToNext()) {
//                val id = cursor.getLong(idColumn)
//                val name = cursor.getString(nameColumn)
//                val mimeType = cursor.getString(mimeTypeColumn)
//
//                if (name != null && mimeType != null) {
//                    val contentUri = ContentUris.withAppendedId(queryUri, id)
//                    Log.i(TAG, "fileInfo uri: $contentUri")
//                }
//
//                Log.i(TAG, "fileInfo name: $name")
//                Log.i(TAG, "fileInfo mimeType: $mimeType")
//
//            }
//
//        }
//
//    }

    private fun showErrorState(errorMessage: String) = makeToast(errorMessage)
}