package com.example.newheadlineapp

data class Article(
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

data class NewsResponse(val articles: List<Article>)

val newsSources = mapOf(
    "BBC News" to "bbc-news",
    "CNN" to "cnn",
    "The Washington Post" to "the-washington-post",
    "The Wall Street Journal" to "the-wall-street-journal")