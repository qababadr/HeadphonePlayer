package com.dev.headphoneplayer.domain.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.dev.headphoneplayer.domain.model.AudioMetadata
import kotlinx.coroutines.flow.Flow

interface HeadphonePlayerRepository {

   suspend fun loadCoverBitmap(context: Context, uri: Uri): Bitmap?

   suspend fun getAudios(): List<AudioMetadata>

   suspend fun likeOrNotSong(id: Long)

   fun getLikedSongs(): Flow<List<Long>>

}