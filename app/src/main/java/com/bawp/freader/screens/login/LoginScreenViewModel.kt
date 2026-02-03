package com.bawp.freader.screens.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bawp.freader.model.MUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginScreenViewModel: ViewModel() {
   // val loadingState = MutableStateFlow(LoadingState.IDLE)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading


    fun signInWithEmailAndPassword(email: String, password: String, home: () -> Unit )
    = viewModelScope.launch{
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Log.d("FB", "signInWithEmailAndPassword: Yayayay! ${task.result.toString()}")
                        //Todo: take them home
                        home()
                    }else {
                        Log.d("FB", "signInWithEmailAndPassword: ${task.result.toString()}")
                    }


                }

        }catch (ex: Exception){
            Log.d("FB", "signInWithEmailAndPassword: ${ex.message}")
        }


    }



    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        home: () -> Unit) {
        if (_loading.value == false) {
             _loading.value = true
            Log.d("FB", "createUserWithEmailAndPassword: Attempting signup with email: $email")
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                     if (task.isSuccessful) {
                         Log.d("FB", "createUserWithEmailAndPassword: SUCCESS! User: ${task.result?.user?.uid}")
                         val displayName = task.result?.user?.email?.split('@')?.get(0)
                         createUser(displayName)
                         home()
                     }else {
                         Log.e("FB", "createUserWithEmailAndPassword FAILED: ${task.exception?.message}")
                         Log.e("FB", "createUserWithEmailAndPassword exception: ${task.exception}")
                     }
                    _loading.value = false
                }
                .addOnFailureListener { exception ->
                    Log.e("FB", "createUserWithEmailAndPassword FAILURE: ${exception.message}")
                    _loading.value = false
                }
        }

    }

    private fun createUser(displayName: String?) {
        val userId = auth.currentUser?.uid
        Log.d("FB", "createUser: Creating user document for userId: $userId, displayName: $displayName")

        val user = MUser(userId = userId.toString(),
            displayName = displayName.toString(),
            avatarUrl = "",
            quote = "Life is great",
            profession = "Android Developer",
            id = null).toMap()


        FirebaseFirestore.getInstance().collection("users")
            .add(user)
            .addOnSuccessListener { documentRef ->
                Log.d("FB", "createUser: SUCCESS! Document ID: ${documentRef.id}")
            }
            .addOnFailureListener { exception ->
                Log.e("FB", "createUser FAILED: ${exception.message}")
            }
    }


}