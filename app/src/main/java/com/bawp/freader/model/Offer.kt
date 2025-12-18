package com.bawp.freader.model

data class Offer(
    val finskyOfferType: Int? = null,
    val giftable: Boolean? = null,
    val listPrice: ListPriceX? = null,
    val retailPrice: RetailPrice? = null
)