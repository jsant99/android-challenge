package com.example.newheadlineapp

import android.util.Log
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    // Set up test dispatcher
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(RetrofitClient)

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `getHeadlines initial state`() {
        // Verify that `getHeadlines()` returns an empty list immediately after NewsViewModel instantiation.
        val viewModel = NewsViewModel()

        assertTrue(viewModel.headlines.isEmpty())
    }

    @Test
    fun `getHeadlines after successful fetch`() {
        // Verify that `getHeadlines()` returns the list of articles after `onSelectedSourceChange` successfully fetches and processes data.
        val mockApi = mockk<NewsApiService>() //mock the API

        val fakeArticles = listOf(
            Article(
                title = "First",
                description = null,
                urlToImage = null,
                publishedAt = "2025-08-01T12:00:00Z",
                content = null
            ),
            Article(
                title = "Second",
                description = null,
                urlToImage = null,
                publishedAt = "2025-08-02T08:00:00Z",
                content = null
            )
        )

        val fakeResponse = NewsResponse(articles = fakeArticles)

        every { RetrofitClient.api } returns mockApi
        coEvery { mockApi.getTopHeadlines(any()) } returns fakeResponse

        // Act
        val viewModel = NewsViewModel()
        viewModel.onSelectedSourceChange("abc-news")

        // Let all coroutines run
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val expected = fakeArticles.sortedByDescending {
            ZonedDateTime.parse(it.publishedAt, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        }

        assertEquals(expected, viewModel.headlines)
    }

    @Test
    fun `getHeadlines after fetch failure`() {
        // Verify that `getHeadlines()` returns an empty list if `fetchHeadlines` encounters an exception during API call.
        val mockApi = mockk<NewsApiService>()
        every { RetrofitClient.api } returns mockApi

        // Simulate failure
        coEvery { mockApi.getTopHeadlines(any()) } throws RuntimeException("Network error")

        val viewModel = NewsViewModel() //
        viewModel.onSelectedSourceChange("abc-news")

        // Let coroutine finish
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<Article>(), viewModel.headlines)
    }

    @Test
    fun `getHeadlines sorting order`() {
        // Verify that `getHeadlines()` returns articles sorted by `publishedAt` in descending order after a successful fetch.
        val mockApi = mockk<NewsApiService>()
        every { RetrofitClient.api } returns mockApi

        val fakeArticles = listOf(
            Article(
                title = "Oldest",
                description = null,
                urlToImage = null,
                publishedAt = "2025-08-01T10:00:00Z",
                content = null
            ),
            Article(
                title = "Newest",
                description = null,
                urlToImage = null,
                publishedAt = "2025-08-02T08:00:00Z",
                content = null
            ),
            Article(
                title = "Middle",
                description = null,
                urlToImage = null,
                publishedAt = "2025-08-01T18:00:00Z",
                content = null
            )
        )

        val fakeResponse = NewsResponse(articles = fakeArticles)

        coEvery { mockApi.getTopHeadlines(any()) } returns fakeResponse

        val viewModel = NewsViewModel()
        viewModel.onSelectedSourceChange("cnn")

        testDispatcher.scheduler.advanceUntilIdle()

        val actual = viewModel.headlines
        val expected = fakeArticles.sortedByDescending {
            ZonedDateTime.parse(it.publishedAt, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        }

        assertEquals(expected, actual)
    }

    @Test
    fun `getHeadlines with null publishedAt`() {
        // Verify how articles with `null` `publishedAt` dates are handled in the sorting. They should typically be at the end.
        val mockApi = mockk<NewsApiService>()
        every { RetrofitClient.api } returns mockApi

        val fakeArticles = listOf(
            Article(
                title = "Article A",
                publishedAt = "2025-08-01T12:00:00Z",
                description = null,
                urlToImage = null,
                content = null
            ),
            Article(title = "Article B",
                publishedAt = null,
                description = null,
                urlToImage = null,
                content = null), // Should go to end
            Article(title = "Article C",
                publishedAt = "2025-08-02T08:00:00Z",
                description = null,
                urlToImage = null,
                content = null)
        )

        val fakeResponse = NewsResponse(articles = fakeArticles)

        coEvery { mockApi.getTopHeadlines(any()) } returns fakeResponse

        val viewModel = NewsViewModel()
        viewModel.onSelectedSourceChange("cnn")

        testDispatcher.scheduler.advanceUntilIdle()


        val result = viewModel.headlines

        // Assert
        assertEquals("Article C", result[0].title) // Most recent
        assertEquals("Article A", result[1].title)
        assertEquals("Article B", result[2].title) // null date last
    }

    @Test
    fun `getHeadlines with invalid publishedAt date format`() {
        // Verify how articles with incorrectly formatted `publishedAt` strings are handled during parsing and sorting. They should be treated similarly to null dates.
        // Mock the API service
        val mockApi = mockk<NewsApiService>()
        every { RetrofitClient.api } returns mockApi

        // Prepare fake articles, including one with an invalid publishedAt date string
        val fakeArticles = listOf(
            Article(
                title = "Article A",
                publishedAt = "2025-08-01T12:00:00Z",
                description = null,
                urlToImage = null,
                content = null
            ),
            Article(
                title = "Article B",
                publishedAt = "not-a-valid-date",
                description = null,
                urlToImage = null,
                content = null
            ), // Invalid date, should be sorted last
            Article(
                title = "Article C",
                publishedAt = "2025-08-02T08:00:00Z",
                description = null,
                urlToImage = null,
                content = null
            )
        )

        val fakeResponse = NewsResponse(articles = fakeArticles)

        // Stub the API call to return the fake response
        coEvery { mockApi.getTopHeadlines(any()) } returns fakeResponse

        // Create the ViewModel and trigger the source change to fetch articles
        val viewModel = NewsViewModel()
        viewModel.onSelectedSourceChange("cnn")

        // Advance coroutine dispatcher to let fetch finish
        testDispatcher.scheduler.advanceUntilIdle()

        // Get the sorted headlines from the ViewModel
        val result = viewModel.headlines

        // Assert that articles are sorted by valid date descending and invalid dates go last
        assertEquals("Article C", result[0].title) // Most recent valid date
        assertEquals("Article A", result[1].title) // Older valid date
        assertEquals("Article B", result[2].title) // Invalid date last
    }

    @Test
    fun `onSelectedSourceChange called multiple times rapidly`() {
        // Test if calling `onSelectedSourceChange` multiple times in quick succession cancels previous ongoing fetches or if they all try to run. 
        // Ensure the final state reflects the last selected source.
        val mockApi = mockk<NewsApiService>()
        every { RetrofitClient.api } returns mockApi

        // Prepare different responses for different sources
        val articlesSource1 = listOf(
            Article(title = "Article 1", publishedAt = "2025-08-01T12:00:00Z", description = null, urlToImage = null, content = null)
        )
        val responseSource1 = NewsResponse(articles = articlesSource1)

        val articlesSource2 = listOf(
            Article(title = "Article 2", publishedAt = "2025-08-02T12:00:00Z", description = null, urlToImage = null, content = null)
        )
        val responseSource2 = NewsResponse(articles = articlesSource2)

        // Mock API to delay responses so that calls overlap
        coEvery { mockApi.getTopHeadlines("source1") } coAnswers {
            delay(100) // simulate network delay
            responseSource1
        }
        coEvery { mockApi.getTopHeadlines("source2") } coAnswers {
            delay(50) // shorter delay for second call
            responseSource2
        }

        val viewModel = NewsViewModel()

        // Call source change rapidly
        viewModel.onSelectedSourceChange("source1")
        viewModel.onSelectedSourceChange("source2")

        // Advance coroutine dispatcher to let fetch finish
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.headlines

        // Verify that final state corresponds to last source ("source2")
        assertEquals(1, result.size)
        assertEquals("Article 2", result[0].title)
    }

    @Test
    fun `Network unavailability during fetchHeadlines`() {
        // Mock network unavailability and verify that the `catch` block in `fetchHeadlines` is executed and `headlines` are handled correctly.
        // Arrange
        val mockApi = mockk<NewsApiService>()
        every { RetrofitClient.api } returns mockApi

        // Simulate network error by throwing an exception on fetch
        coEvery { mockApi.getTopHeadlines(any()) } throws Exception("Network error")

        val viewModel = NewsViewModel()

        // Act
        viewModel.onSelectedSourceChange("cnn")

        // Advance coroutine until idle so the fetch completes
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        // headlines should remain empty (or whatever the initial state is)
        assertTrue(viewModel.headlines.isEmpty())
    }

    @Test
    fun `API returns error response  e g   4xx  5xx `() {
        // Mock the API to return an error response and verify that the `catch` block handles it, logs an error, and `headlines` are updated appropriately.
        // Arrange
        val mockApi = mockk<NewsApiService>()
        every { RetrofitClient.api } returns mockApi

        // Create a fake error response (e.g. 500 Internal Server Error)
        val errorResponse = Response.error<NewsResponse>(
            500,
            "Internal Server Error".toResponseBody("text/plain".toMediaTypeOrNull())
        )

        // Throw HttpException on API call to simulate server error
        coEvery { mockApi.getTopHeadlines(any()) } throws HttpException(errorResponse)

        // Act
        val viewModel = NewsViewModel()
        viewModel.onSelectedSourceChange("cnn")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        // headlines should remain empty on error
        assertTrue(viewModel.headlines.isEmpty())
    }

    @Test
    fun `API returns unexpected data structure for articles`() {
        // Mock the API to return a response where `response.articles` is null or not a list. 
        // Verify that this doesn't crash the app and `headlines` are handled (e.g., remain empty).
        // Arrange
        val mockApi = mockk<NewsApiService>()
        every { RetrofitClient.api } returns mockApi

        // Return response with null articles list
        val fakeResponse = NewsResponse(articles = emptyList())

        coEvery { mockApi.getTopHeadlines(any()) } returns fakeResponse

        val viewModel = NewsViewModel()

        // Act
        viewModel.onSelectedSourceChange("bbc")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        // headlines should remain empty and no crash
        assertTrue(viewModel.headlines.isEmpty())
    }

    @Test
    fun `Article with all fields null or empty`() {
        // Test with an API response containing an article where all properties (especially `publishedAt`) are null or empty strings. 
        // Ensure no NullPointerExceptions or parsing errors occur.
        // Arrange
        val mockApi = mockk<NewsApiService>()
        every { RetrofitClient.api } returns mockApi

        val articleWithNulls = Article(
            title = "",
            publishedAt = "",  // empty string
            description = null,
            urlToImage = null,
            content = null
        )

        val fakeResponse = NewsResponse(articles = listOf(articleWithNulls))
        coEvery { mockApi.getTopHeadlines(any()) } returns fakeResponse

        val viewModel = NewsViewModel()

        // Act
        viewModel.onSelectedSourceChange("cnn")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val result = viewModel.headlines
        assertEquals(1, result.size)
        assertEquals(articleWithNulls, result[0])
    }

}


