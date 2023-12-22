package com.example.message.util


import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.message.model.CommonInfor
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigInteger
import java.util.LinkedList

class HandShake {
    private var database: DatabaseReference = Firebase.database.reference
    private val handShakeRef: DatabaseReference = database.child("hand-shakes")
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

        handShakeRef
            .push()
            .setValue(
                message
            )
    }

    public fun sendHandShakeRequest() {
        Log.d("sending request", "sending...")
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
        Log.d("request sent", message.toString())

    }

    public fun acceptHandShakeRequest() {
        var handShake = CommonInfor()

        GlobalScope.launch(Dispatchers.IO) {
            val result = async {
                val snapshot = Tasks.await(handShakeRef.get())
                return@async snapshot
            }.await()
            result.children.forEach {ds ->
                val data = ds.getValue(CommonInfor::class.java)
                if (data!!.senderID == receiverID && data!!.retrieverID == senderID)
                    handShake = data
            }
        }
        val encryptedAES = handShake.encryptedAESKey
        val aesKey = RSA.decrypt(encryptedAES!!.toBigInteger(), Temp.keyPair!!.second)
        saveAESKey(aesKey.toByteArray())
    }

    public fun saveAESKey(key: ByteArray) {
        val path = context.getFilesDir()

        val letDirectory = File(path, "AESKeys")
        letDirectory.mkdirs()

        val file = File(letDirectory, senderID + ".txt")

        file.appendText(receiverID + " " + key.toString() + "\n")
    }
}