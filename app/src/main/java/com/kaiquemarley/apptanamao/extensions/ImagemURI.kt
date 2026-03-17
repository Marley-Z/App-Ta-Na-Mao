package com.kaiquemarley.apptanamao.extensions

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object ImagemURI {

    fun getImagemUriPeloNome(context: Context, nomeArquivo: String): Uri? {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(nomeArquivo)

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                return Uri.withAppendedPath(collection, id.toString())
            }
        }
        return null
    }
}
