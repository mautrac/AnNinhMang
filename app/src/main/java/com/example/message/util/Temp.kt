package com.example.message.util

import com.example.message.model.User
import com.google.firebase.auth.FirebaseUser
import java.math.BigInteger
import javax.crypto.SecretKey

/**
 * class [Temp] Store data cache (Singleton)
 * @param currentUser Current User is using by app
 * @param keyPair key Pair of current user.
 * @param messageTemp new message sent.
 * @param retriever Retriever current(Used to get public key)
 */

object Temp {
    var currentUser: FirebaseUser? = null
    var retriever: User? = null
    var keyPair: Pair<Pair<BigInteger, BigInteger>, Pair<BigInteger, BigInteger>>? = null
    var messageTemp: String? = null
    var retrieverPublicKey: Pair<BigInteger, BigInteger>? = null
    var aesKey: SecretKey? = null
}