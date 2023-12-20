package com.example.message.util

import android.util.Log
import com.example.message.model.BigIntegerPair
import com.example.message.model.CommonInfor
import com.example.message.model.Message
import com.example.message.sqlUtils.DbHandler
import com.example.message.viewmodel.ChatRoomViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.math.BigInteger
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class HandShake {
    private var database: DatabaseReference = Firebase.database.reference
    private val messagesRef: DatabaseReference = database.child("handshake")

    val receiverID : String;
    val senderID : String;

    constructor(receiverID: String, senderID: String) {
        this.receiverID = receiverID
        this.senderID = senderID;
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

    public fun handShake() {


    }
}