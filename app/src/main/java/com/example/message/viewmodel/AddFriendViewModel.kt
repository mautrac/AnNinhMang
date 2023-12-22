package com.example.message.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.message.util.Temp
import com.google.firebase.database.*

class AddFriendViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("users")

    private val _result = MutableLiveData<String>()
    val result: LiveData<String>
        get() = _result

    fun checkUserExists(email: String) {
        database.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val uid =
                            snapshot.children.first().key // Assuming the email is unique and gets one child
                        _result.value = "success"
                        // Now add this uid to the current user's friend list
                        addFriend(email)
                    } else {
                        _result.value = "Email không tồn tại"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _result.value = "Có lỗi xảy ra: ${error.message}"
                }
            })
    }

    private fun addFriend(friendEmail: String) {
        // Tìm UID của bạn bè dựa trên email
        database.orderByChild("email").equalTo(friendEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d("key yes", "onDataChange: ${Temp.realtimeKey} ")
                        val friendKey = dataSnapshot.children.first().key
                        if (friendKey != null && friendKey != Temp.realtimeKey) {
                            // Thêm bạn vào danh sách bạn bè của người dùng hiện tại
                            database.child(Temp.realtimeKey.toString()).child("friends")
                                .child(friendKey).setValue(true)
                        }
                    } else {
                        Log.d("key no", "onDataChange: ${Temp.realtimeKey} ")
                        // Xử lý trường hợp không tìm thấy email của bạn
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Xử lý lỗi nếu cần
                }
            })
    }

    // Các phương thức khác như thêm bạn bè có thể được thêm vào đây
}
