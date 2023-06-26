package app.shop.recptify.model

import java.io.Serializable

data class PostModel(
    val selectedCategory: String = "",
    val image: String = "",
    val title: String = "",
    val description: String = "",
    var id: String = "", // Unique ID for each record
): Serializable