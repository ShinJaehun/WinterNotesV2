package com.shinjaehun.winternotesv2.common

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.MimeTypeFilter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

private const val TAG = "FileUtils"

object FileUtils {
//    lateinit var application: Application
//    lateinit var cRes: ContentResolver

//    @Throws(IOException::class)
//    fun getInputStream(uri: Uri): InputStream? {
//        return if (isVirtualFile(uri)) {
//            getInputStreamForVirtualFile(uri, getMimeType(uri))
//        } else {
//            cRes.openInputStream(uri)
//        }
//    }
//
//    private fun getMimeType(uri: Uri): String? {
//        return cRes.getType(uri)
//    }
//
//    private fun isVirtualFile(uri: Uri): Boolean {
//        if (!DocumentsContract.isDocumentUri(application, uri)) {
//            return false
//        }
//
//        val cursor = cRes.query(
//            uri,
//            arrayOf(DocumentsContract.Document.COLUMN_FLAGS),
//            null,
//            null,
//            null
//        )
//        val flags: Int = cursor?.use {
//            if (cursor.moveToFirst()) {
//                cursor.getInt(0)
//            } else {
//                0
//            }
//        } ?: 0
//
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            flags and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT != 0
//        } else {
//            false
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun getInputStreamForVirtualFile(uri: Uri, mimeTypeFilter: String?):FileInputStream? {
//        if (mimeTypeFilter==null) {
//            throw FileNotFoundException()
//        }
//        Log.i(TAG, "mimeTypeFilter: $mimeTypeFilter")
//        val openableMimeTypes: Array<String>? = cRes.getStreamTypes(uri, mimeTypeFilter)
//        Log.i(TAG, "openableMimeTypes: $openableMimeTypes")
//
//        return if (openableMimeTypes?.isNotEmpty() == true) {
//            cRes.openTypedAssetFileDescriptor(uri, openableMimeTypes[0], null)?.createInputStream()
//        } else {
//            throw FileNotFoundException()
//        }
//    }

    ////////////////////////////////////////////////////////////////////////////

    private fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024)
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
            outputStream.close()
        }
    }


    fun fileFromContentUri(context: Context, contentUri: Uri): File {

        val fileExtension = getFileExtension(context, contentUri)
        val fileName = "temporary_file_" + currentTimeInfo() + if (fileExtension != null) ".$fileExtension" else ""
//        val folder = File(context.filesDir, "images")
        // 경로가 여기서부터 시작함 /data/user/0/com.shinjaehun.winternotesv2/files/images/파일명
        val path = context.getExternalFilesDir(null)
        val folder = File(path, "images")
        // 이게 폰의 루트인듯 함 /storage/emulated/0/Android/data/com.shinjaehun.winternotesv2/files/images/파일명
        folder.mkdirs()
        val outputFile = File(folder, fileName)


        try {
            val inputStream = context.contentResolver.openInputStream(contentUri)
            copyStreamToFile(inputStream!!, outputFile)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return outputFile

    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        val fileType: String? = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }


}