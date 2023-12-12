package com.example.message.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.message.model.BigIntegerPair
import com.example.message.model.User
import com.example.message.util.Temp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val auth = Firebase.auth

    private var database: DatabaseReference = Firebase.database.reference

    private val userRef: DatabaseReference = database.child("users")

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    fun login(email: String, password: String) {
        if (checkLogin(email, password)) {
            _loginResult.value = false
        } else {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginResult.value = true
                    Temp.currentUser = auth.currentUser
                    val publicKey = Temp.keyPair.first

                    Log.d(TAG, "${Temp.keyPair.first}\n${Temp.keyPair.second}")
                    viewModelScope.launch(Dispatchers.IO) {
                        saveUser(
                            User(
                                Temp.currentUser?.uid,
                                Temp.currentUser?.email,
                                BigIntegerPair(
                                    publicKey.first.toString(),
                                    publicKey.second.toString()
                                )
                            )
                        )
                    }
                } else {
                    _loginResult.value = false
                }
            }
        }
    }

    private fun saveUser(user: User) {

        userRef.orderByChild("email").equalTo(user.email)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userKey = dataSnapshot.children.first().key
                        userKey?.let {
                            userRef.child(userKey).child("publicKey")
                                .setValue(user.publicKey)
                        }
                        return
                    } else {
                        userRef.push().setValue(user)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // ...
                }
            })
    }

    private fun checkLogin(
        email: String,
        password: String
    ): Boolean = email.isBlank() || password.isBlank()

    companion object {
        const val TAG = "LoginViewModel"
    }
}

class LoginViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}