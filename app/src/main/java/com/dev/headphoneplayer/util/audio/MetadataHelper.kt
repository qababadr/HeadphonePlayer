package com.dev.headphoneplayer.util.audio

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import com.dev.headphoneplayer.domain.model.AudioMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MetadataHelper @Inject constructor(@ApplicationContext val context: Context) {

    private var cursor: Cursor? = null

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ARTIST
    )

    private var selectionClause: String? =
        "${MediaStore.Audio.AudioColumns.IS_MUSIC} = ?"
    private var selectionArg = arrayOf("1")

    private val sortOrder = "${MediaStore.Audio.AudioColumns.DISPLAY_NAME} ASC"

    @WorkerThread
    fun getAudios(): List<AudioMetadata>{
        return getCursorData()
    }

    @WorkerThread
    fun getAlbumArt(context: Context, uri: Uri): Bitmap?{
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(context, uri)
        val bitmap: Bitmap? = try{
            val data = mmr.embeddedPicture
            if (data != null){
                BitmapFactory.decodeByteArray(data, 0, data.size)
            } else{
                null
            }
        } catch (exp: Exception){
            null
        } finally {
            mmr.release()
        }
        return bitmap
    }

    private fun getCursorData(): MutableList<AudioMetadata> {
        val audioList = mutableListOf<AudioMetadata>()

        cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selectionClause,
            selectionArg,
            sortOrder
        )

        cursor?.let { cursor ->
            val idColumn =
                cursor.getColumnIndexOrThrow(projection.first())

            val durationColumn =
                cursor.getColumnIndexOrThrow(projection[2])

            val titleColumn =
                cursor.getColumnIndexOrThrow(projection[3])

            val artistColumn =
                cursor.getColumnIndexOrThrow(projection[5])

            cursor.apply {
                if (count > 0) {
                    while (cursor.moveToNext()) {

                        val id = cursor.getLong(idColumn)
                        val duration = cursor.getInt(durationColumn)
                        val title = cursor.getString(titleColumn)
                        val artist = cursor.getString(artistColumn)
                        val contentUri: Uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        audioList += AudioMetadata(
                            songId = id,
                            contentUri = contentUri,
                            cover = null,
                            songTitle = title,
                            artist = artist,
                            duration = duration
                        )
                    }
                }
            }
        }
        return audioList
    }

}