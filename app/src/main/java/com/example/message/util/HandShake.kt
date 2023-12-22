package com.example.message.util


import android.content.Context
import android.os.Environment
import com.example.message.model.CommonInfor
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.File
import java.math.BigInteger

class HandShake {
    private var database: DatabaseReference = Firebase.database.reference
    private val messagesRef: DatabaseReference = database.child("hand-shakes")
    //private val messagesRef: DatabaseReference = database.child("handshake")

    val receiverID : String
    val senderID : String
    val receiverPK: Pair<BigInteger, BigInteger>
    val context: Context;

    constructor(receiverID: String, senderID: String, receiverPK: Pair<BigInteger, BigInteger>, context: Context) {
        this.receiverID = receiverID
        this.senderID = senderID;
        this.receiverPK = receiverPK;
        this.context = context;
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

    public fun sendHandShakeRequest() {

        //SecretKey
        val aesKey = AESEncryption.generateKey()

        //bytearray
        val encodedKey = aesKey.encoded
        //BigInteger
        val encodedKey_bigint = BigInteger(encodedKey)

        //bigint
        val encryptedAES1 = RSA.encrypt(BigInteger(1, encodedKey), receiverPK)
        val encryptedAES = RSA.encrypt(encodedKey_bigint, receiverPK)

        val message = CommonInfor("Handshake", senderID, receiverID, encryptedAES.toString())

        sendMessage(message)

        saveAESKey(encodedKey)
    }

    public fun acceptAllHandShakeReqest() {
        val path = context.getFilesDir()

        val letDirectory = File(path, "AESKeys")
        letDirectory.mkdirs()

        val file = File(letDirectory, senderID + ".txt")

        file.appendText(receiverID + " " + key.toString() + "\n")
    }

    public fun saveAESKey(key: ByteArray) {
        val path = context.getFilesDir()

        val letDirectory = File(path, "AESKeys")
        letDirectory.mkdirs()

        val file = File(letDirectory, senderID + ".txt")

        file.appendText(receiverID + " " + key.toString() + "\n")
    }
}