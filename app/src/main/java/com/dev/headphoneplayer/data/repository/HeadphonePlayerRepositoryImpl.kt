package com.dev.headphoneplayer.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.dev.headphoneplayer.domain.model.AudioMetadata
import com.dev.headphoneplayer.domain.repository.HeadphonePlayerRepository
import com.dev.headphoneplayer.util.audio.MetadataHelper
import com.dev.headphoneplayer.util.audio.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HeadphonePlayerRepositoryImpl @Inject constructor(
    private val metadataHelper: MetadataHelper,
    private val userPreferences: UserPreferences
) : HeadphonePlayerRepository {

    override suspend fun loadCoverBitmap(context: Context, uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            metadataHelper.getAlbumArt(context = context, uri = uri)
        }
    }

    override suspend fun getAudios(): List<AudioMetadata> {
        return withContext(Dispatchers.IO) {
            metadataHelper.getAudios()
        }
    }

    override suspend fun likeOrNotSong(id: Long) {
        withContext(Dispatchers.IO){
            userPreferences.likeOrNotSong(id = id)
        }
    }

    override fun getLikedSongs(): Flow<List<Long>> {
        return userPreferences.likedSongs
    }
}