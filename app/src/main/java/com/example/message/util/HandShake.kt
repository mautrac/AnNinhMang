package com.example.message.util

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.message.model.BigIntegerPair
import com.example.message.model.CommonInfor
import com.example.message.model.Message
import com.example.message.util.RSA;
import com.example.message.util.AES;
import com.example.message.viewmodel.ChatRoomViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HandShake {
    private var database: DatabaseReference = Firebase.database.reference
    private val messagesRef: DatabaseReference = database.child("messages")

    private val messageList = mutableListOf<CommonInfor>()
    private var _messages = MutableLiveData<List<CommonInfor>>()
    val messages: LiveData<List<CommonInfor>>
        get() = _messages

    val retrievedID : String;
    val senderID : String;

    lateinit var private_key_retriever : BigIntegerPair;

    constructor(retrievedID: String, senderID: String) {
        this.retrievedID = retrievedID
        this.senderID = senderID;
    }


    fun getMessages(
    ) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messageList.clear()

                for (snapshot in dataSnapshot.children) {
                    val message = snapshot.getValue(CommonInfor::class.java)
                    message?.let {
                        if (message.title!!.contains("RSA") && message.senderID == retrievedID) {
                            messagesRef.removeEventListener(this)
                            private_key_retriever = message.key!!
                        }
                    }
                }
                _messages.value = messageList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ChatRoomViewModel.TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        messagesRef.addValueEventListener(postListener)

    }

    fun sendMessage(
        message: CommonInfor
    ) {

        messagesRef
            .push()
            .setValue(
                message
            )
    }

    public fun handShake() {


        val keys = RSA.generateRSAKeys()
        //public key
        Log.d("public key", keys.first.toString())
        val public = keys.first
        //private key
        Log.d("private key", keys.second.toString())
        val private = keys.second

        val key_send = BigIntegerPair(public.first.toString(), public.second.toString())
        val message = CommonInfor("Public RSA key", senderID, retrievedID, key_send)

        getMessages()


    }
}