package com.example.message.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*

class AddFriendViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("users")

    private val _result = MutableLiveData<String>()
    val result: LiveData<String>
        get() = _result

    fun checkUserExists(email: String) {
        database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
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
