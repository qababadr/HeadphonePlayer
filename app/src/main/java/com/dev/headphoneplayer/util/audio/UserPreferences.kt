package com.dev.headphoneplayer.util.audio

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferences @Inject constructor(@ApplicationContext val context: Context) {

    private val gson: Gson = Gson()

    private val _likedSongs: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[LIKED_SONGS_KEY]
        }

    val likedSongs: Flow<List<Long>> = flow {
        _likedSongs.collect {
            emit(value = fromStringToListLong(string = it ?: "[]"))
        }
    }

    suspend fun likeOrNotSong(id: Long) {
        context.dataStore.edit { preferences ->
            val songs = likedSongs.first().toMutableList()
            if (songs.contains(id)) songs.remove(id) else songs.add(id)
            preferences[LIKED_SONGS_KEY] = fromListLongToString(listLong = songs)
        }
    }

    private fun fromStringToListLong(string: String): List<Long> {
        return try {
            gson.fromJson(
                string,
                object : TypeToken<List<Long>>() {}.type
            )
        } catch (exp: JsonSyntaxException) {
            ArrayList()
        }
    }

    private fun fromListLongToString(listLong: List<Long>): String {
        return try {
            gson.toJson(
                listLong,
                object : TypeToken<List<Long>>() {}.type
            )
        } catch (exp: JsonSyntaxException) {
            "[]"
        }
    }

    companion object {
        private const val HEADPHONE_PLAYER_PREFERENCES = "HeadphonePlayer"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            HEADPHONE_PLAYER_PREFERENCES
        )
        private val LIKED_SONGS_KEY = stringPreferencesKey("LIKED_SONGS")
    }

}