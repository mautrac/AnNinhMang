package com.example.message.util

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.util.Random

/**
 * RSA algorithm
 * @reference https://www.javatpoint.com/rsa-encryption-algorithm
 */
object RSA {
    // Function to calculate the greatest common divisor of two number
    private fun gcd(a: BigInteger, b: BigInteger): BigInteger {
        if (b == BigInteger.ZERO) {
            return a
        }
        return gcd(b, a % b)
    }

    /**
     * RSA key pair generator
     * @return public and private key pairs
     */
    fun generateRSAKeys(): Pair<Pair<BigInteger, BigInteger>, Pair<BigInteger, BigInteger>> {
        val random = Random()

        // Select two large prime numbers, p and q.
        val p = BigInteger(1024, 100, random).nextProbablePrime()
        val q = BigInteger(1024, 100, random).nextProbablePrime()

        // Multiply these numbers to find n = p x q, where n is
        // called the modulus for encryption and decryption.
        val n = p * q

        // Choose a number e less than n, such that n is relatively prime to (p - 1) x (q -1).
        // It means that e and (p - 1) x (q - 1) have no common factor except 1. Choose "e" such that 1<e < φ (n),
        // e is prime to φ (n), gcd (e,d(n)) =1
        // choose e, such that 1 < e < φ(n) and gcd(e, φ(n)) = 1
        val phiN = (p - BigInteger.ONE) * (q - BigInteger.ONE)

        var e = BigInteger(1024, random)
        while (e <= BigInteger.ONE || e >= phiN || gcd(e, phiN) != BigInteger.ONE) {
            e = BigInteger(1024, random)
        }

        // Step 4: calculator d, such that (d * e) % φ(n) = 1
        val d = e.modInverse(phiN)

        // If n = p x q, then the public key is <e, n>. A plaintext message m is encrypted
        // using public key <e, n>. To find ciphertext from the plain text following
        // formula is used to get ciphertext C.
        val publicKey = Pair(e, n)

        // * The private key is <d, n>. A ciphertext message c is decrypted using private key <d, n>.
        // To calculate plain text m from the ciphertext c following formula is used to get plain text m.
        val privateKey = Pair(d, n)

        return Pair(publicKey, privateKey)
    }

    /**
     * Encrypt function
     * @return message encrypted (BigInteger)
     */
    fun encrypt(
        message: BigInteger,
        publicKey: Pair<BigInteger, BigInteger>
    ): BigInteger {
        val (e, n) = publicKey
        return message.modPow(e, n)
    }

    /**
     * Decrypt function
     * @return text decrypted (BigInteger)
     */
    fun decrypt(
        ciphertext: BigInteger,
        privateKey: Pair<BigInteger, BigInteger>
    ): BigInteger {
        val (d, n) = privateKey
        return ciphertext.modPow(d, n)
    }
}

// Convert text from UTF-8 to BigInteger
fun utf8ToBigInteger(text: String): BigInteger {
    val utf8Bytes = text.toByteArray(StandardCharsets.UTF_8)
    return BigInteger(1, utf8Bytes) // Đặt bit đầu tiên thành 0 để tránh số âm
}

// Convert text from BigInteger to utf-8
fun bigIntegerToUtf8(bigInteger: BigInteger): String {
    val byteArray = bigInteger.toByteArray()
    return String(byteArray, StandardCharsets.UTF_8)
}