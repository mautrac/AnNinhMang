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
    private val messagesRef: DatabaseReference = database.child("messages")

    val receiverID : String;
    val senderID : String;

    lateinit var receiverRsaPublicKey : BigIntegerPair;

    constructor(receiverID: String, senderID: String) {
        this.receiverID = receiverID
        this.senderID = senderID;
    }

    fun exchangeSecretKey(
        senderPrivateRSAKey: Pair<BigInteger, BigInteger>
    ) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (snapshot in dataSnapshot.children) {
                    val message = snapshot.getValue(CommonInfor::class.java)
                    message?.let {
                        if (message.title!!.contains("AES") && message.senderID == receiverID) {
                            messagesRef.removeEventListener(this)
                            val encryptedAes = BigInteger(message.encryptedAESKey)

                            val aesBigInteger = RSA.decrypt(encryptedAes, senderPrivateRSAKey)

                            val dbHandler = DbHandler().writableDatabase


                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ChatRoomViewModel.TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        messagesRef.addValueEventListener(postListener)
    }
    fun exchangeKey(
        senderPublicRSAKey: Pair<BigInteger, BigInteger>,
        senderPrivateRSAKey: Pair<BigInteger, BigInteger>,
        senderAESKey: SecretKey
    ) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (snapshot in dataSnapshot.children) {
                    val message = snapshot.getValue(CommonInfor::class.java)
                    message?.let {
                        if (message.title!!.contains("RSA") && message.senderID == receiverID) {
                            messagesRef.removeEventListener(this)
                            receiverRsaPublicKey = message.publicRsaKey!!
                            val aesKey = BigInteger(senderAESKey.encoded!!)

                            val f = BigInteger(receiverRsaPublicKey.first)
                            val s = BigInteger(receiverRsaPublicKey.second)
                            val receiverPubK = Pair(f, s)

                            val encryptedAESKey = RSA.encrypt(aesKey, receiverPubK)

                            val mess = CommonInfor("ENCRYPTED AES KEY",senderID, receiverID, null, encryptedAESKey.toString())

                            sendMessage(mess)

                            exchangeSecretKey(senderPrivateRSAKey)
                        }
                    }
                }
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


        val RSAKeySender = RSA.generateRSAKeys()
        //public key
        Log.d("public key", RSAKeySender.first.toString())
        val senderPublicRSAKey = RSAKeySender.first
        //private key
        Log.d("private key", RSAKeySender.second.toString())
        val senderPrivateRSAKey = RSAKeySender.second

        val key_send = BigIntegerPair(senderPublicRSAKey.first.toString(), senderPublicRSAKey.second.toString())
        val message = CommonInfor("Public RSA key", senderID, receiverID, key_send)

        sendMessage(message)

        val aes : AES = AES()
        aes.init()

        exchangeKey(senderPublicRSAKey, senderPrivateRSAKey, aes.getKey())


    }
}