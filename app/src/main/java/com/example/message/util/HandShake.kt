package com.example.message.util


import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.message.model.CommonInfor
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigInteger
import javax.crypto.spec.SecretKeySpec

class HandShake {
    private var database: DatabaseReference = Firebase.database.reference
    private val handShakeRef: DatabaseReference = database.child("hand-shakes")
    //private val messagesRef: DatabaseReference = database.child("handshake")

    private val receiverID: String
    private val senderID: String
    private val receiverPK: Pair<BigInteger, BigInteger>
    val context: Context

    constructor(
        receiverID: String,
        senderID: String,
        receiverPK: Pair<BigInteger, BigInteger>,
        context: Context
    ) {
        this.receiverID = receiverID
        this.senderID = senderID
        this.receiverPK = receiverPK
        this.context = context
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

    fun sendHandShakeRequest() {
        Log.d("sending request", "sending...")
        //SecretKey
        val aesKey = AESEncryption.generateKey()

        //bytearray
        val encodedKey = aesKey.encoded
        val str_key = Base64.encodeToString(encodedKey, Base64.DEFAULT)

        Log.d("create key str", str_key)
        Log.d("create key", encodedKey.toString())
        Log.d("create key 1", encodedKey.size.toString())
        Log.d("create key", encodedKey.toString())

        //BigInteger
        val encodedKey_bigint = BigInteger(encodedKey)

        //bigint
        val encryptedAES1 = RSA.encrypt(BigInteger(1, encodedKey), receiverPK)
        val encryptedAES = RSA.encrypt(encodedKey_bigint, receiverPK)

        val message = CommonInfor("Handshake", senderID, receiverID, encryptedAES1.toString())

        sendMessage(message)

        saveAESKey(encodedKey)
        Log.d("request sent", message.toString())

    }

    fun acceptHandShakeRequest() {

        GlobalScope.launch(Dispatchers.IO) {
            var handShake = CommonInfor()

            val result = async {
                return@async Tasks.await<DataSnapshot?>(handShakeRef.get())
            }.await()
            result.children.forEach { ds ->
                val data = ds.getValue(CommonInfor::class.java)
                if (data!!.senderID ==receiverID  && data.retrieverID == senderID)
                    handShake = data
            }
            Log.d("accep request", handShake.toString())
            withContext(Dispatchers.Main) {
                val encryptedAES = handShake.encryptedAESKey
                val aesKey = RSA.decrypt(BigInteger(encryptedAES), Temp.keyPair!!.second)

                saveAESKey(aesKey.toByteArray())

            }
        }
    }

    fun saveAESKey(key: ByteArray) {

        val str_key = Base64.encodeToString(key, Base64.DEFAULT)

        val path = context.filesDir
        Log.d("saveAES", "saveAESKey: $path")
        val letDirectory = File(path, "AESKeys")
        letDirectory.mkdirs()

        val file = File(letDirectory, senderID + ".txt")
        file.createNewFile()

        file.writeText("$receiverID $str_key\n")

        Temp.aesKey = SecretKeySpec(key, "AES")
        Log.d("save aes key", senderID + " " + Temp.aesKey!!.encoded.size)
    }
}