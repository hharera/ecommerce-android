package com.harera.features.cart

import java.util.Date

data class WishListItem(
    val uid: String,
    val productId: String,
    val time: Date
)