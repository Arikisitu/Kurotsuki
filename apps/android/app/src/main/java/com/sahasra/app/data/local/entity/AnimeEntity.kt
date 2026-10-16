package com.sahasra.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime_library")
data class AnimeEntity(
    @PrimaryKey
    val id: Int,
    val titleEnglish: String?,
    val titleRomaji: String?,
    val coverImageUrl: String?,
    val bannerImageUrl: String?,
    val format: String?,
    val status: String?,
    val averageScore: Int?,
    val episodes: Int?,
    val genres: String, // Comma-delimited list
    val synopsis: String?,
    val mediaType: String = "ANIME", // "ANIME" or "MANGA"
    val libraryStatus: String = "PLANNING", // "WATCHING", "COMPLETED", "PLANNING", "DROPPED"
    val savedAt: Long = System.currentTimeMillis()
)
