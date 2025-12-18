package com.bawp.freader.model

data class Item(
    val accessInfo: AccessInfo? = null,
    val etag: String? = null,
    val id: String? = null,
    val kind: String? = null,
    val saleInfo: SaleInfo? = null,
    val searchInfo: SearchInfo? = null,
    val selfLink: String? = null,
    val volumeInfo: VolumeInfo? = null
)