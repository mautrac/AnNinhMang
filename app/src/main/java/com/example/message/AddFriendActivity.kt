package com.example.message

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.activityViewModels
import com.example.message.util.HandShake
import com.example.message.util.Temp
import com.example.message.viewmodel.AddFriendViewModel
import com.example.message.viewmodel.LoginViewModel
import com.example.message.viewmodel.LoginViewModelFactory
import java.math.BigInteger

class AddFriendActivity : AppCompatActivity() {

    private val viewModel: AddFriendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val addFriendButton = findViewById<Button>(R.id.addFriendButton)
        val backButton=findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }
        addFriendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.checkUserExists(email)
            } else {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.result.observe(this) { result ->
            if (result.equals("success")) {
                // Thực hiện thêm bạn bè
                Log.d("addf", viewModel.receiver.value.toString())
                val receiver = viewModel.receiver.value!!

                val receiverPK = Pair(BigInteger(receiver.publicKey!!.first),
                        BigInteger(receiver.publicKey!!.second)
                    )
                Log.d("addf key", receiverPK.toString())

                var handShake = HandShake(receiver.uid!!, Temp.currentUser!!.uid, receiverPK, this  )

                handShake.sendHandShakeRequest()

                Toast.makeText(this, "Đã thêm vào danh sách bạn bè", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
