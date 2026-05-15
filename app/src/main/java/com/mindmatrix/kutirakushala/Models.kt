package com.mindmatrix.kutirakushala

import com.google.firebase.Timestamp

data class Artisan(
    val id: String = "",
    val name: String = "",
    val specialty: String = "",
    val location: String = "",
    val phone: String = "",
    val rating: Double = 0.0,
    val cluster: String = ""
)

data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val stock: Int = 0,
    val price: Double = 0.0,
    val description: String = ""
)

data class Buyer(
    val id: String = "",
    val name: String = "",
    val region: String = "",
    val type: String = "",
    val contact: String = ""
)