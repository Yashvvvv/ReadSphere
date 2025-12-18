package com.bawp.freader.screens.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.sharp.Person
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bawp.freader.components.ReaderAppBar
import com.bawp.freader.model.MBook
import com.bawp.freader.screens.home.HomeScreenViewModel
import com.bawp.freader.utils.formatDate
import com.google.firebase.auth.FirebaseAuth
import java.util.*

@Composable
fun ReaderStatsScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val books = viewModel.data.value.data?.filter { it.userId == currentUser?.uid } ?: emptyList()

    Scaffold(
        topBar = {
            ReaderAppBar(
                title = "Book Stats",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                showProfile = false,
                navController = navController
            ) {
                navController.popBackStack()
            }
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                // Greeting Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Sharp.Person,
                        contentDescription = "User Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hi, ${currentUser?.email?.split("@")?.get(0)?.uppercase(Locale.getDefault())}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Stats Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    val readBooks = books.filter { it.finishedReading != null }
                    val readingBooks = books.filter { it.startedReading != null && it.finishedReading == null }

                    Column(
                        modifier = Modifier.padding(start = 25.dp, top = 8.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Your Stats", style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 1.dp,                         // ✅ Use a valid Dp value
                            color = Color.Gray                        // ✅ Use a valid Color value
                        )
                        Text("You're reading: ${readingBooks.size} books")
                        Text("You've read: ${readBooks.size} books")
                    }
                }

                // Loading Indicator
                if (viewModel.data.value.loading == true) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp,                  // Set desired thickness
                        color = Color.LightGray            // Set desired color
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        val finishedBooks = books.filter { it.finishedReading != null }
                        items(finishedBooks) { book ->
                            BookRowStats(book)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookRowStats(book: MBook) {
    val imageUrl = book.photoUrl.takeIf { !it.isNullOrEmpty() }
        ?: "https://images.unsplash.com/photo-1541963463532-d68292c34b19"

    Card(
        modifier = Modifier
            .clickable { /* navigate to details if needed */ }
            .fillMaxWidth()
            .height(100.dp)
            .padding(4.dp),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Book Cover",
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = book.title ?: "Unknown",
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if ((book.rating ?: 0.0) >= 4.0) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Thumbs up",
                            tint = Color.Green.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "Author: ${book.authors ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1
                )
                Text(
                    text = "Started: ${book.startedReading?.let { formatDate(it) } ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
                Text(
                    text = "Finished: ${book.finishedReading?.let { formatDate(it) } ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}
