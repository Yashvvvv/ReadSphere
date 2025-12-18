package com.bawp.freader.model

data class Book(
    val items: List<Item>? = null,
    val kind: String? = null,
    val totalItems: Int? = null
)