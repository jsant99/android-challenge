# Android Challenge
News APP for Android challenge.

## Features
* Responsive Screens:
  * Authentication Screen: Screen with a button to request authentication, only displayed if conditions are met.
  * News Screen: Screen that displays the list of news.
  * Article Screen: Screen that displays the selected article.

* Authentication Screen for fingerprint/faceID authentication. Is able to handle issues with biometrics errors:
  * Authentication Error: Displays error message.
  * Authentication hardware not available: Device does not have hardware support for biometrics. Will skip the authentication screen.
  * Authentication feature not supported: Device does not have the feature for biometric authentication. Will skip the authentication screen.
  * Authentication Success: Jumps to the news screen.
  * Authentication failed: Display error message.

* News Screen:
  * List of news articles from a selected source.
  * List of different sources.
  * List of articles ordered by date.
  * Images cached using async image.
  * News articles are fetched from news-api using retrofit.
  * Fetched articles from api are parsed from JSON to objects using GSON.
 
* Article Screen:
  * Displays Title, description, content and image if available.

* Navigation between screens:
  * Authentication ------>  News <------> Article
  * When navigating from an article back to news, the article is removed from the navigation stack.
