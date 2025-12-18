package com.bawp.freader.model

data class VolumeInfo(
    val allowAnonLogging: Boolean? = null,
    val authors: List<String>? = null,
    val averageRating: Double? = null,
    val canonicalVolumeLink: String? = null,
    val categories: List<String>? = null,
    val contentVersion: String? = null,
    val description: String? = null,
    val imageLinks: ImageLinks? = null,
    val industryIdentifiers: List<IndustryIdentifier>? = null,
    val infoLink: String? = null,
    val language: String? = null,
    val maturityRating: String? = null,
    val pageCount: Int? = null,
    val panelizationSummary: PanelizationSummary? = null,
    val previewLink: String? = null,
    val printType: String? = null,
    val publishedDate: String? = null,
    val publisher: String? = null,
    val ratingsCount: Int? = null,
    val readingModes: ReadingModes? = null,
    val subtitle: String? = null,
    val title: String? = null
)