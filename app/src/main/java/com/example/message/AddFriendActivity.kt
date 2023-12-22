package com.example.message

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.message.util.Temp
import com.example.message.viewmodel.AddFriendViewModel

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
                Temp.currentUser?.let { it1 -> viewModel.checkUserExists(email) }
                viewModel.result.observe(this) { result ->
                    if (result == "success") {
                        Toast.makeText(this, "Đã thêm vào danh sách bạn bè", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
            }
        }


    }
}
