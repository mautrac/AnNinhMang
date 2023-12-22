package com.example.message.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.message.model.CommonInfor
import com.example.message.model.User
import com.example.message.util.Temp
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.values
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.util.LinkedList
import kotlin.collections.ArrayList

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

        val handShakeRef = database.child("hand-shakes")
        var listHandShake: LinkedList<CommonInfor> = LinkedList<CommonInfor>()

        GlobalScope.launch(Dispatchers.IO) {
            val result = async {
                val snapshot = Tasks.await(handShakeRef.get())
                return@async snapshot
            }.await()
            result.children.forEach {ds ->
                val data = ds.getValue(CommonInfor::class.java)
                if (data!!.senderID == Temp.currentUser?.uid || data!!.retrieverID == Temp.currentUser?.uid)
                    listHandShake.add(data)
            }
        }

        var listFriend : ArrayList<String> = ArrayList()
        listHandShake.forEach {ele ->
            if (ele.retrieverID != Temp.currentUser?.uid)
                listFriend.add(ele.senderID!!)
            if (ele.senderID == Temp.currentUser?.uid)
                listFriend.add(ele.retrieverID!!)
        }

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        if (user.uid != Temp.currentUser?.uid && listFriend.contains(user.uid))
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


//        val db = Firebase.firestore
//        val docRef = db.collection("hand-shakes")
//
//        docRef.whereEqualTo("senderID", Temp.currentUser!!.uid)
//        docRef.whereEqualTo("retrieverID", Temp.currentUser!!.uid)

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