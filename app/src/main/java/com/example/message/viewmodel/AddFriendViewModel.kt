package com.example.message.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.message.model.User
import com.google.firebase.database.*

class AddFriendViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("users")

    private var _receiver = MutableLiveData<User>()
    var receiver : LiveData<User> = _receiver

    private val _result = MutableLiveData<String>()
    val result: LiveData<String>
        get() = _result

    fun checkUserExists(email: String) {
        database.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    //Log.d("add friend", receiver.value.toString())
                    _receiver.value = snapshot.children.first().getValue(User::class.java)
                    //Log.d("addf,", snapshot.getValue(User::class.java).toString())
                    Log.d("addf1", snapshot.toString())
                    _result.value = "success"
                } else {
                    _result.value = "Email không tồn tại"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _result.value = "Có lỗi xảy ra: ${error.message}"
            }
        })
    }

    // Các phương thức khác như thêm bạn bè có thể được thêm vào đây
}

class AddFriendViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddFriendViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return AddFriendViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
