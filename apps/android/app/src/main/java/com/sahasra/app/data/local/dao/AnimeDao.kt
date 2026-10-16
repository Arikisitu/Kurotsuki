package com.sahasra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sahasra.app.data.local.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime_library ORDER BY savedAt DESC")
    fun getAllLibraryAnime(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime_library WHERE mediaType = :mediaType ORDER BY savedAt DESC")
    fun getLibraryByMediaType(mediaType: String): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime_library WHERE id = :id LIMIT 1")
    fun getAnimeById(id: Int): Flow<AnimeEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM anime_library WHERE id = :id)")
    fun isSaved(id: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anime: AnimeEntity)

    @Update
    suspend fun update(anime: AnimeEntity)

    @Delete
    suspend fun delete(anime: AnimeEntity)

    @Query("DELETE FROM anime_library WHERE id = :id")
    suspend fun deleteById(id: Int)
}
