package com.example.message.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.message.model.BigIntegerPair
import com.example.message.model.User
import com.example.message.util.RSA
import com.example.message.util.Temp
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel: ViewModel() {


    private val auth = Firebase.auth

    private var database: DatabaseReference = Firebase.database.reference

    private val userRef: DatabaseReference = database.child("users")

    private val _loginResult = MutableLiveData<Boolean>()
    private var _publicKey = MutableLiveData<BigIntegerPair>()
    private var _checkKeyExist = MutableLiveData<Boolean>(false)

    val loginResult: LiveData<Boolean> = _loginResult
    val publicKey: LiveData<BigIntegerPair> = _publicKey
    val checKeyExist : LiveData<Boolean> = _checkKeyExist


    fun login(email: String, password: String) {
        if (checkLogin(email, password)) {
            _loginResult.value = false
        } else {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    Temp.currentUser = auth.currentUser
                    Log.d("Login", Temp.currentUser.toString())

                    GlobalScope.launch(Dispatchers.IO) {
                        val result = async {
                            val snapshot = Tasks.await(userRef.orderByChild("email").equalTo(email).get())
                            if (snapshot.exists()) {
                                snapshot.children.first().getValue(User::class.java)!!.publicKey!!
                            } else {
                                null
                            }
                        }.await()

                        withContext(Dispatchers.Main) {
                            _publicKey.value = result!!
                        }
                    }
                    Temp.keyPair = RSA.generateRSAKeys()
                    var publicKey = Temp.keyPair!!.first

                    userRef.orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    _checkKeyExist.value = true
                                    Log.d("check", checKeyExist.toString())
                                    Log.d("login key", dataSnapshot.children.first().key.toString())
                                    Temp.realtimeKey=dataSnapshot.children.first().key
                                    _publicKey.value = dataSnapshot.children.first().getValue(User::class.java)!!.publicKey!!
                                    //publicKey = dataSnapshot.children.first().key
                                }

                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                // ...
                            }
                        })



//                    viewModelScope.launch(Dispatchers.IO) {
//                        saveUser(
//                            User(
//                                Temp.currentUser?.uid,
//                                Temp.currentUser?.email,
//                                BigIntegerPair(
//                                    publicKey.first.toString(),
//                                    publicKey.second.toString()
//                                )
//                            )
//                        )
//                    }




                    Log.d(TAG, "${Temp.keyPair!!.first}\n${Temp.keyPair!!.second}")

                    _loginResult.value = true
                } else {
                    _loginResult.value = false
                }
            }
        }
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