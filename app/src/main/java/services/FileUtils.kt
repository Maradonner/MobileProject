package services

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore

object FileUtils {

    fun getPath(context: Context, uri: Uri): String? {
        // Check if the URI is a content URI from the MediaStore.
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Check whether the URI is from the media store or other types of content URIs
            if (isMediaContentUri(uri)) {
                return getDataColumn(context, uri, null, null)
            } else if (DocumentsContract.isDocumentUri(context, uri)) {
                // DocumentProvider
                return handleDocumentUri(context, uri)
            }
        }
        // File
        else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun handleDocumentUri(context: Context, uri: Uri): String? {
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            if ("primary".equals(split[0], ignoreCase = true)) {
                return "${context.getExternalFilesDir(null)?.absolutePath}/${split[1]}"
            }
        } else if (isDownloadsDocument(uri)) {
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), DocumentsContract.getDocumentId(uri).toLong())
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            return getMediaDocumentPath(context, uri)
        }
        return null
    }

    private fun isMediaContentUri(uri: Uri): Boolean {
        return "media".equals(uri.authority, ignoreCase = true)
    }

    private fun getMediaDocumentPath(context: Context, uri: Uri): String? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":")
        val contentUri: Uri = when (split[0]) {
            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> return null
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])
        return getDataColumn(context, contentUri, selection, selectionArgs)
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents".equals(uri.authority)
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents".equals(uri.authority)
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents".equals(uri.authority)
    }
}