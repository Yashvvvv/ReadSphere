package com.bawp.freader.repository

import com.bawp.freader.data.Resource
import com.bawp.freader.model.Item
import com.bawp.freader.network.BooksApi
import javax.inject.Inject

class BookRepository @Inject constructor(private val api: BooksApi) {
    suspend fun getBooks(searchQuery: String): Resource<List<Item>> {
        return try {
            val itemList = api.getAllBooks(searchQuery).items ?: emptyList()
            Resource.Success(data = itemList)
        } catch (exception: Exception) {
            Resource.Error(message = exception.message.toString())
        }
    }

    suspend fun getBookInfo(bookId: String): Resource<Item> {
        return try {
            val response = api.getBookInfo(bookId)
            Resource.Success(data = response)
        } catch (exception: Exception) {
            Resource.Error(message = "An error occurred ${exception.message.toString()}")
        }
    }
}