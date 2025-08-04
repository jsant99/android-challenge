package com.example.newheadlineapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.*
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.newheadlineapp.ui.ArticleDetailScreen
import com.example.newheadlineapp.ui.AuthScreen
import com.example.newheadlineapp.ui.NewsScreen

//###########################################################################################################
//                                            MainActivity                                                  #
// Create main activity class that extends AppCompatActivity                                                #
// Manage navigation between screens using Jetpack Navigation:                                              #
//      1 -> Navigation starts in AuthScreen (authentication screen with fingerprint or faceID).            #
//           In case device does not support fingerprint or faceID, this view is skipped to NewsScreen      #
//      2 -> NewsScreen displays list of news articles from selected source. When user clicks on article,   #
//           it is navigated to ArticleDetailScreen. Swiping/pressing back button on NewsScreen exits app.  #
//      3 -> ArticleDetailScreen displays details of selected article. Swiping/pressing back button on      #
//           ArticleDetailScreen returns to NewsScreen.                                                     #
//###########################################################################################################

class MainActivity : AppCompatActivity() {

    val activity: MainActivity = this

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {

                WindowCompat.setDecorFitsSystemWindows(window, true)

                //Create navController
                val navController = rememberNavController()

                //Navigation Handling. Starts in auth screen.
                NavHost(navController = navController, startDestination = "auth") {
                    composable("auth") {
                        AuthScreen(navController, activity)
                    }
                    composable("news") {
                        NewsScreen(navController)
                    }
                    composable(
                        "article/{title}/{description}/{content}/{urlToImage}",
                        arguments = listOf(
                            navArgument("title") { type = NavType.StringType },
                            navArgument("description") { type = NavType.StringType; nullable = true },
                            navArgument("content") { type = NavType.StringType; nullable = true },
                            navArgument("urlToImage") { type = NavType.StringType; nullable = true }
                        )
                    ) { backStackEntry ->
                        val title = backStackEntry.arguments?.getString("title") ?: ""
                        val description = backStackEntry.arguments?.getString("description")
                        val content = backStackEntry.arguments?.getString("content")
                        val urlToImage = backStackEntry.arguments?.getString("urlToImage")

                        ArticleDetailScreen(navController, title, description, content, urlToImage)
                    }
                }
            }
        }
    }
}





