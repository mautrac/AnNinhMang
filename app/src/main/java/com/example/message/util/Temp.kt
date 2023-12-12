package com.example.message.util

import com.example.message.model.User
import com.google.firebase.auth.FirebaseUser
import java.math.BigInteger

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
    val keyPair = RSA.generateRSAKeys()
    var messageTemp: String? = null
    var retrieverPublicKey: Pair<BigInteger, BigInteger>? = null
}