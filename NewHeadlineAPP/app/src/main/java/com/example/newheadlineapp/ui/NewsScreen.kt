package com.example.newheadlineapp.ui

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.newheadlineapp.NewsViewModel

//###########################################################################################################
//                                            NewsScreen                                                    #
// NewsScreen composable function.                                                                          #
// Calls NewsDropdown composable function. Contains dropdown list of different news sources and page title  #
// Prints list of headlines from selected news source.                                                      #
// Image caching is handled via asynchronous image loading library Coil.                                    #
//###########################################################################################################

@Composable
fun NewsScreen(navController: NavHostController, viewModel: NewsViewModel = viewModel()) {
    val headlines = viewModel.headlines

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.DarkGray
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            //Custom DropDown menu for news sources selection
            NewsDropdown(viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(headlines) { article ->
                    //Displays clickable article.
                    //Navigates to new screen sending all information to be displayed via route.
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable{navController.navigate(
                                "article/" +
                                        Uri.encode(article.title) + "/" +
                                        Uri.encode(article.description ?: "") + "/" +
                                        Uri.encode(article.content ?: "") + "/" +
                                        Uri.encode(article.urlToImage ?: "")
                            )},
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.LightGray
                        )
                    ) {
                        Column {
                            Text(
                                text = article.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .align(Alignment.CenterHorizontally),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )

                            // Show image if urlToImage is not null or empty.
                            article.urlToImage?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Article image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}