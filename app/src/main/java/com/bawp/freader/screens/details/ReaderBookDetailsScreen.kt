package com.bawp.freader.screens.details

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.bawp.freader.components.ReaderAppBar
import com.bawp.freader.components.RoundedButton
import com.bawp.freader.data.Resource
import com.bawp.freader.model.Item
import com.bawp.freader.model.MBook
import com.bawp.freader.navigation.ReaderScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(navController: NavController,
                      bookId: String,
                      viewModel: DetailsViewModel = hiltViewModel()) {

    Scaffold(topBar = {
        ReaderAppBar(title = "Book Details",
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            showProfile = false,
            navController = navController){
            navController.navigate(ReaderScreens.SearchScreen.name)
        }
    }) { paddingValues ->

        Surface(modifier = Modifier
            .padding(paddingValues)
            .padding(3.dp)
            .fillMaxSize()) {
            Column(modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally) {

                val bookInfo = produceState<Resource<Item>>(initialValue = Resource.Loading()){
                    value = viewModel.getBookInfo(bookId)
                }.value

                if (bookInfo.data == null) {
                    Row() {
                        LinearProgressIndicator()
                        Text(text = "Loading...")
                    }

                }else{
                    ShowBookDetails(bookInfo, navController)
                }
                //  Log.d("Deets", "BookDetailsScreen: ${bookInfo.data.toString()}")


            }


        }
    }

}

@Composable
fun ShowBookDetails(bookInfo: Resource<Item>,
                    navController: NavController) {
    val bookData = bookInfo.data?.volumeInfo
    val googleBookId = bookInfo.data?.id

    if (bookData == null) {
        Text("Book details not available")
        return
    }

    val imageUrl = bookData.imageLinks?.thumbnail
        ?: "https://images.unsplash.com/photo-1541963463532-d68292c34b19"

    Card(modifier = Modifier.padding(34.dp),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Image(painter = rememberAsyncImagePainter(model = imageUrl),
            contentDescription = "Book Image",
            modifier = Modifier
                .width(90.dp)
                .height(90.dp)
                .padding(1.dp))


    }
    Text(text = bookData.title ?: "Unknown Title",
        style = MaterialTheme.typography.titleLarge,
        overflow = TextOverflow.Ellipsis,
        maxLines = 19)

    Text(text = "Authors: ${bookData.authors?.joinToString() ?: "N/A"}")
    Text(text = "Page Count: ${bookData.pageCount ?: "N/A"}")
    Text(text = "Categories: ${bookData.categories?.joinToString() ?: "N/A"}",
        style = MaterialTheme.typography.titleMedium,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis)
    Text(text = "Published: ${bookData.publishedDate ?: "N/A"}",
        style = MaterialTheme.typography.titleMedium)

    Spacer(modifier = Modifier.height(5.dp))

    val cleanDescription = HtmlCompat.fromHtml(bookData.description ?: "No description available",
        HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    val localDims = LocalContext.current.resources.displayMetrics
    Surface(modifier = Modifier
        .height(localDims.heightPixels.dp.times(0.09f))
        .padding(4.dp),
        shape = RectangleShape,
        border = BorderStroke(1.dp, Color.DarkGray)) {

        LazyColumn(modifier = Modifier.padding(3.dp)) {
            item {

                Text(text = cleanDescription)
            }

        }
    }

    //Buttons
    Row(modifier = Modifier.padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround) {
        RoundedButton(label = "Save"){
            //save this book to the firestore database
            val book = MBook(
                title = bookData.title,
                authors = bookData.authors?.joinToString() ?: "",
                description = bookData.description,
                categories = bookData.categories?.joinToString() ?: "",
                notes = "",
                photoUrl = bookData.imageLinks?.thumbnail,
                publishedDate = bookData.publishedDate,
                pageCount = bookData.pageCount?.toString() ?: "",
                rating = 0.0,
                googleBookId = googleBookId,
                userId = FirebaseAuth.getInstance().currentUser?.uid.toString())

            saveToFirebase(book, navController = navController)

        }
        Spacer(modifier = Modifier.width(25.dp))
        RoundedButton(label = "Cancel"){
            navController.popBackStack()
        }

    }


}


fun saveToFirebase(book: MBook, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val dbCollection = db.collection("books")

    if (book.toString().isNotEmpty()){
        dbCollection.add(book)
            .addOnSuccessListener { documentRef ->
                val docId = documentRef.id
                dbCollection.document(docId)
                    .update(hashMapOf("id" to docId) as Map<String, Any>)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navController.popBackStack()
                        }
                    }.addOnFailureListener {
                        Log.w("Error", "SaveToFirebase:  Error updating doc",it )
                    }
            }
            .addOnFailureListener {
                Log.w("Error", "SaveToFirebase: Error adding book", it)
            }
    }

}