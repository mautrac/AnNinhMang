package com.example.message.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.message.model.User
import com.example.message.util.Temp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeViewModel : ViewModel() {

    private var database: DatabaseReference = Firebase.database.reference

    private val userList = mutableListOf<User>()

    private var _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>>
        get() = _users

    init {
        getUsers()
    }

    private fun getUsers() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        if (user.uid != Temp.currentUser?.uid)
                            userList.add(user)
                    }
                }
                _users.value = userList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // ...
            }
        }

        val usersRef = database.child("users")
        usersRef.addValueEventListener(postListener)
    }

    companion object {
        const val TAG = "HomeViewModel"
    }
}

class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HomeViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}