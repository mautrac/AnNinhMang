package com.example.message.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.message.R
import com.example.message.databinding.FragmentLoginBinding
import com.example.message.model.User
import com.example.message.util.RSA
import com.example.message.util.Temp
import com.example.message.viewmodel.LoginViewModel
import com.example.message.viewmodel.LoginViewModelFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.File
import javax.crypto.spec.SecretKeySpec

class LoginFragment : Fragment() {

    private var database: DatabaseReference = Firebase.database.reference
    private val userRef: DatabaseReference = database.child("users")

    private val viewModel: LoginViewModel by activityViewModels {
        LoginViewModelFactory()
    }

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.homeFragment = this@LoginFragment

        viewModel.loginResult.observe(this.viewLifecycleOwner) {
            if (it) {
                if (viewModel.checKeyExist.value == false) {
                    Temp.keyPair = RSA.generateRSAKeys()
                    requireContext().
                }
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.login_failure_notification),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    fun onLoginButton() {
        val email = binding.username.text.toString().trim()
        val password = binding.password.text.toString().trim()
        viewModel.login(email, password)
    }

    private fun saveUser(user: User) {

        userRef.orderByChild("email").equalTo(user.email)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userKey = dataSnapshot.children.first().key
                        userKey?.let {
                            userRef.child(userKey).child("publicKey")
                                .setValue(user.publicKey)
                        }
                        return
                    } else {
                        userRef.push().setValue(user)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // ...
                }
            })
    }

    private fun readKey() {
        //read aeskey
        //val path = context.getFilesDir()
        val path = requireContext().filesDir

        val letDirectory = File(path, "Keys")

        val file = File(letDirectory, "Records.txt")
        if (file.isFile) {
            val contents = file.readText()
            var i = 0
            var spaceIdx = 0
            for (j in 0..contents.length) {
                if (contents[j] == '\n') {
                    val retrieverId = contents.substring(i, spaceIdx)
                    val aesKeyStr = contents.substring(spaceIdx + 1, j)
                    if (retrieverId.equals(uid)) {
                        val aesKeyByteArray = aesKeyStr.toByteArray()
                        Temp.aesKey = SecretKeySpec(aesKeyByteArray, "AES")
                    }
                }
                if (contents[j] == ' ')
                    spaceIdx = j
            }
            Log.d(this.toString(), Temp.aesKey.toString())
        }
    }
}