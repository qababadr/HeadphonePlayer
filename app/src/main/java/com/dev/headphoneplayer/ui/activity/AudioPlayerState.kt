package com.dev.headphoneplayer.ui.activity

import com.dev.headphoneplayer.domain.model.AudioMetadata

data class AudioPlayerState(
    val isLoading: Boolean = false,
    val audios: List<AudioMetadata> = emptyList(),
    val isPlaying: Boolean = false,
    val selectedAudio: AudioMetadata = AudioMetadata.emptyMetadata(),
    val currentPosition: Int = 0,
    val likedSongs: List<Long> = emptyList()
)
