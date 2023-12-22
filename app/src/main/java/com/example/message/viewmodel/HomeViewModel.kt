package com.example.message.viewmodel

import android.util.Log
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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.LinkedList

class HomeViewModel : ViewModel() {

    private var database: DatabaseReference = Firebase.database.reference

    private val userList = mutableListOf<User>()
    private val handShakeList = mutableListOf<CommonInfor>()
    private val friendList = mutableListOf<User>()

    private var _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>>
        get() = _users

    private val _handShakes = MutableLiveData<List<CommonInfor>>()
    private val _friends = MutableLiveData<List<User>>()

    val handShakes: LiveData<List<CommonInfor>>
        get() = _handShakes

    val firends: LiveData<List<User>>
        get() = _friends

    init {
        getUsers()
    }

    private fun getUsers() {

        val handShakeRef = database.child("hand-shakes")
        val usersRef = database.child("users")
//        var listHandShake: ArrayList<CommonInfor> = ArrayList<CommonInfor>()
//
//        GlobalScope.launch(Dispatchers.IO) {
//            var list = ArrayList<CommonInfor>()
//            val result = async {
//                val snapshot = Tasks.await(handShakeRef.get())
//                return@async snapshot
//            }.await()
//            result.children.forEach {ds ->
//                val data = ds.getValue(CommonInfor::class.java)
//                Log.d("hs", data.toString() + " " + (data!!.senderID == Temp.currentUser?.uid).toString())
//                if (data!!.senderID == Temp.currentUser?.uid || data!!.retrieverID == Temp.currentUser?.uid) {
//                    Log.d("check", "True")
//                    list.add(data!!)
//                }
//            }
//            withContext(Dispatchers.Main) {
//
//            }
//        }
//
//        var listFriend : ArrayList<String> = ArrayList()
//        listHandShake.forEach {ele ->
//            if (ele.retrieverID != Temp.currentUser?.uid)
//                listFriend.add(ele.senderID!!)
//            if (ele.senderID == Temp.currentUser?.uid)
//                listFriend.add(ele.retrieverID!!)
//        }



        val postListenerHandShake = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                handShakeList.clear()
                for (snapshot in dataSnapshot.children) {
                    val hs = snapshot.getValue(CommonInfor::class.java)
                    hs?.let {
                        if (hs.senderID == Temp.currentUser?.uid || hs.retrieverID == Temp.currentUser?.uid)
                            handShakeList.add(hs)
                    }
                }
                //_users.value = userList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // ...
            }
        }
        handShakeRef.addValueEventListener(postListenerHandShake)

        _handShakes.observeForever {
            friendList.clear()
            it.forEach {hs ->
                users.value!!.forEach {user ->
                    val id = user.uid
                    if (id != Temp.currentUser?.uid) {
                        if (id == hs.senderID || id == hs.retrieverID)
                            friendList.add(user)
                    }
                }
            }
            _friends.value = friendList
        }


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
        if (modelClass.isAssignableFrom(HomeViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}