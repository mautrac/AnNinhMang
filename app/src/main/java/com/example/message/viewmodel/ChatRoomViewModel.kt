package com.example.message.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.message.model.Message
import com.example.message.util.RSA
import com.example.message.util.Temp
import com.example.message.util.utf8ToBigInteger
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatRoomViewModel : ViewModel() {

    private var database: DatabaseReference = Firebase.database.reference

    private val messagesRef: DatabaseReference = database.child("messages")

    private val messageList = mutableListOf<Message>()

    private var _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>>
        get() = _messages

    fun getMessages(
        senderID: String,
        retrievedID: String
    ) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messageList.clear()
                for (snapshot in dataSnapshot.children) {
                    val message = snapshot.getValue(Message::class.java)
                    Log.d(TAG, message.toString())
                    message?.let {
                        if (
                            message.retrieverID == retrievedID &&
                            message.senderID == senderID ||
                            message.retrieverID == senderID &&
                            message.senderID == retrievedID
                        )
                            messageList.add(message)
                    }
                }
                _messages.value = messageList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        messagesRef.addValueEventListener(postListener)
    }

    fun sendMessage(
        senderID: String,
        retrievedID: String,
        text: String
    ) {

        val textEncrypted = RSA.encrypt(
            utf8ToBigInteger(text),
            Temp.retrieverPublicKey!!
        ).toString()

        messagesRef
            .push()
            .setValue(
                Message(
                    senderID, retrievedID, textEncrypted
                )
            )
    }

    fun checkInput(message: String): Boolean = message.isBlank()

    companion object {
        const val TAG = "ChatRoomViewModel"
    }
}

class ChatRoomViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return ChatRoomViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}