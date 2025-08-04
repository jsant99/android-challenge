package com.example.newheadlineapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

//###########################################################################################################
//                                            NewsViewModel                                                 #
// Handles fetching list of Articles from different news sources.                                           #
// Requests API call to news-api using RetrofitClient.  Orders list fetched by date published.              #
//###########################################################################################################

class NewsViewModel : ViewModel() {

    //List to save news articles fetched from API
    var headlines by mutableStateOf<List<Article>>(emptyList())
        private set

    //stores the current source of news. Starts by default on the first element of available sources.
    private var currSource = newsSources.entries.first().value

    //creates a job. Allows for the observing and controlling the lifecycle of the coroutine.
    private var fetchJob: kotlinx.coroutines.Job? = null

    //Initial fetch of headlines. When the ViewModel is created.
    init {
        onSelectedSourceChange(currSource)
    }

    private fun fetchHeadlines(source: String) {
        viewModelScope.launch {

            // Cancel any ongoing fetch
            fetchJob?.cancel()

            //Starts a new fetch job
            fetchJob = viewModelScope.launch {
                try {
                    val response = RetrofitClient.api.getTopHeadlines(source)
                    Log.d("NewsViewModel", "Response: $response")

                    //Saves the fetched articles and orders by date if available.
                    headlines = response.articles
                        .sortedByDescending { article ->
                            article.publishedAt?.let {
                                try {
                                    ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }
                } catch (e: Exception) {
                    Log.e("NewsViewModel", "Error: ${e.message}")
                }
            }
        }
    }

    //Called when the selected source changes. Requests a new fetch.
    fun onSelectedSourceChange(source: String) {
        viewModelScope.launch {
            currSource = source
            fetchHeadlines(currSource)
        }
    }
}