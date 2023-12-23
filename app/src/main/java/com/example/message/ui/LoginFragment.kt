package com.example.message.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.message.R
import com.example.message.databinding.FragmentLoginBinding
import com.example.message.model.BigIntegerPair
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
                if (viewModel.checKeyExist.value == false && viewModel.publicKey != null) {
                    Temp.keyPair = RSA.generateRSAKeys()

                    var newUser = User(Temp.currentUser!!.uid,
                                Temp.currentUser!!.email,
                                BigIntegerPair(Temp.keyPair!!.first.first.toString(),
                                                Temp.keyPair!!.first.second.toString()
                                    )
                        )
                    saveUser(newUser)
                    savePrivateRSAKey()
                }
                else {
                    getRSAKey()
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
                            // Cập nhật publicKey cho người dùng hiện có
                            userRef.child(userKey).child("publicKey")
                                .setValue(user.publicKey)
                            // Cập nhật danh sách bạn bè ở đây nếu bạn muốn
                        }
                        return
                    } else {
                        // Tạo người dùng mới
                        userRef.push().setValue(user)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Xử lý lỗi nếu cần
                }
            })
    }

    // Phương thức để thêm bạn


    private fun getRSAKey() {
        //read aeskey
        //val path = context.getFilesDir()
        val path = requireContext().filesDir

        val letDirectory = File(path, "RSAKeys")

        val file = File(letDirectory, Temp.currentUser!!.uid + ".txt")
        if (file.isFile) {
            val contents = file.readText()
            var i = 0

            for (j in 0..contents.length) {
                if (contents[j] == '\n') {
                    val first = contents.substring(i, j)
                    val second = contents.substring(j + 1, contents.length - 1)
                    val privateKey = Pair(first.toBigInteger(), second.toBigInteger())
                    val publicKey = Pair(viewModel.publicKey.value!!.first!!.toBigInteger(),
                                viewModel.publicKey.value!!.second!!.toBigInteger() )
                    Temp.keyPair = Pair(publicKey, privateKey)

                    break
                }

            }
        }
        Log.d("READ RSA KEYS", Temp.keyPair.toString())
    }

    fun savePrivateRSAKey() {
        //read aeskey
        //val path = context.getFilesDir()
        val path = requireContext().filesDir

        val letDirectory = File(path, "RSAKeys")
        letDirectory.mkdirs()

        val file = File(letDirectory, Temp.currentUser!!.uid + ".txt")
        file.createNewFile()
        file.appendText(Temp.keyPair!!.second.first.toString() + "/n")
        file.appendText(Temp.keyPair!!.second.second.toString() + "/n")

    }
}