package com.bawp.freader.screens.update

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.bawp.freader.R
import com.bawp.freader.components.InputField
import com.bawp.freader.components.ReaderAppBar
import com.bawp.freader.components.RoundedButton
import com.bawp.freader.data.DataOrException
import com.bawp.freader.model.MBook
import com.bawp.freader.navigation.ReaderScreens
import com.bawp.freader.screens.home.HomeScreenViewModel
import com.bawp.freader.utils.formatDate
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.bawp.freader.components.RatingBar
import com.bawp.freader.components.showToast


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BookUpdateScreen(navController: NavHostController, bookItemId: String, viewModel: HomeScreenViewModel = hiltViewModel()) {
    Scaffold(topBar = {
        ReaderAppBar(
            title = "Update Book",
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            showProfile = false,
            navController = navController
        ) {
            navController.popBackStack()
        }
    }) { padding ->
        val bookInfo = viewModel.data.value

        Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (bookInfo.loading == true) {
                    LinearProgressIndicator()
                } else {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = CircleShape, tonalElevation = 4.dp) {
                        ShowBookUpdate(bookInfo = viewModel.data.value, bookItemId = bookItemId)
                    }
                    viewModel.data.value.data?.firstOrNull { it.googleBookId == bookItemId }?.let {
                        ShowSimpleForm(book = it, navController = navController)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShowSimpleForm(book: MBook, navController: NavHostController) {
    val context = LocalContext.current
    val notesText = remember { mutableStateOf(book.notes ?: "") }
    val isStartedReading = remember { mutableStateOf(false) }
    val isFinishedReading = remember { mutableStateOf(false) }
    val ratingVal = remember { mutableStateOf(book.rating?.toInt() ?: 0) }

    SimpleForm(defaultValue = notesText.value) { notesText.value = it }

    Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { isStartedReading.value = true }, enabled = book.startedReading == null) {
            Text(if (book.startedReading == null) if (!isStartedReading.value) "Start Reading" else "Started Reading!" else "Started on: ${formatDate(book.startedReading!!)}")
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = { isFinishedReading.value = true }, enabled = book.finishedReading == null) {
            Text(if (book.finishedReading == null) if (!isFinishedReading.value) "Mark as Read" else "Finished Reading!" else "Finished on: ${formatDate(book.finishedReading!!)}")
        }
    }

    Text("Rating", modifier = Modifier.padding(vertical = 4.dp))
    RatingBar(rating = ratingVal.value) { ratingVal.value = it }

    Spacer(modifier = Modifier.height(16.dp))

    val changed = book.notes != notesText.value || book.rating?.toInt() != ratingVal.value || isStartedReading.value || isFinishedReading.value
    val bookToUpdate = mapOf(
        "finished_reading_at" to (if (isFinishedReading.value) Timestamp.now() else book.finishedReading),
        "started_reading_at" to (if (isStartedReading.value) Timestamp.now() else book.startedReading),
        "rating" to ratingVal.value,
        "notes" to notesText.value
    )


    Row(verticalAlignment = Alignment.CenterVertically) {
        RoundedButton("Update") {
            if (changed) {
                FirebaseFirestore.getInstance().collection("books").document(book.id!!).update(bookToUpdate)
                    .addOnCompleteListener {
                        showToast(context, "Book Updated Successfully!")
                        navController.navigate(ReaderScreens.ReaderHomeScreen.name)
                    }.addOnFailureListener {
                        Log.e("BookUpdate", "Error updating book", it)
                    }
            }
        }

        Spacer(modifier = Modifier.width(100.dp))

        val openDialog = remember { mutableStateOf(false) }
        if (openDialog.value) {
            ShowAlertDialog(stringResource(id = R.string.sure) + "\n" + stringResource(id = R.string.action), openDialog) {
                FirebaseFirestore.getInstance().collection("books").document(book.id!!).delete().addOnSuccessListener {
                    openDialog.value = false
                    navController.navigate(ReaderScreens.ReaderHomeScreen.name)
                }
            }
        }

        RoundedButton("Delete") { openDialog.value = true }
    }
}

@Composable
fun ShowAlertDialog(message: String, openDialog: MutableState<Boolean>, onYesPressed: () -> Unit) {
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text("Delete Book") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onYesPressed) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { openDialog.value = false }) { Text("No") }
            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SimpleForm(modifier: Modifier = Modifier, defaultValue: String = "", onSearch: (String) -> Unit) {
    val textFieldValue = rememberSaveable { mutableStateOf(defaultValue) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val valid = remember(textFieldValue.value) { textFieldValue.value.trim().isNotEmpty() }

    InputField(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color.White, CircleShape)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        valueState = textFieldValue,
        labelId = "Enter Your thoughts",
        enabled = true,
        onAction = KeyboardActions {
            if (valid) {
                onSearch(textFieldValue.value.trim())
                keyboardController?.hide()
            }
        }
    )
}

@Composable
fun ShowBookUpdate(bookInfo: DataOrException<List<MBook>, Boolean, Exception>, bookItemId: String) {
    bookInfo.data?.firstOrNull { it.googleBookId == bookItemId }?.let { book ->
        Column(modifier = Modifier.padding(8.dp)) {
            CardListItem(book = book, onPressDetails = {})
        }
    }
}

@Composable
fun CardListItem(book: MBook, onPressDetails: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onPressDetails),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = book.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .height(100.dp)
                    .width(120.dp)
                    .clip(RoundedCornerShape(topStart = 120.dp, topEnd = 20.dp))
            )

            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = book.title ?: "N/A",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.authors ?: "Unknown",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = book.publishedDate ?: "",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
