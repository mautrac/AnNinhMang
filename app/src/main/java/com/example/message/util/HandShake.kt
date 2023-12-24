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


        //BigInteger
        val encodedKey_bigint = BigInteger(1, encodedKey)
        Log.d("key int", encodedKey_bigint.toString())

        //bigint
        val encryptedAES1 = RSA.encrypt(BigInteger(encodedKey), receiverPK)
        val encryptedAES = RSA.encrypt(encodedKey_bigint, receiverPK)

        Log.d("ec by receiver", encryptedAES1.toString())
        val ec2 = RSA.encrypt(encodedKey_bigint, Temp.keyPair!!.first)
        Log.d("ec by sender", ec2.toString())
        val dc2 = RSA.decrypt(ec2, Temp.keyPair!!.second)
        Log.d("dc by sender", dc2.toString())
        val orgKey = SecretKeySpec(dc2.toByteArray(), "AES")
        Log.d("dc key", orgKey.encoded.toString())
        Log.d("dc key int", BigInteger(1, orgKey.encoded).toString())
        Log.d("dc key length", orgKey.encoded.size.toString())
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
                if (data!!.senderID == receiverID && data.retrieverID == senderID)
                    handShake = data
            }
            Log.d("accep request", handShake.toString())
            withContext(Dispatchers.Main) {
                val encryptedAES = handShake.encryptedAESKey
                Log.d("received e aeskey", encryptedAES.toString())
                val aesKey = RSA.decrypt(encryptedAES!!.toBigInteger(), Temp.keyPair!!.second)
                Log.d("encerypt aeskey", aesKey.toString())

                saveAESKey(aesKey.toByteArray())

            }
        }
    }

    fun saveAESKey(key: ByteArray) {

        val str_key = Base64.encodeToString(key, Base64.DEFAULT)

        val path = context.filesDir
        Log.d("Path", path.toString())

        val letDirectory = File(path, "AESKeys")
        letDirectory.mkdirs()

        val file = File(letDirectory, senderID + ".txt")
        file.createNewFile()

        //file.writeText(receiverID + " " + BigInteger(1, key).toString() + "\n")
        file.writeText(receiverID + " " + str_key + "\n")
        Temp.aesKey = SecretKeySpec(key, "AES")
        Log.d("save aes key", senderID + " " + Temp.aesKey!!.encoded.toString())
    }
}