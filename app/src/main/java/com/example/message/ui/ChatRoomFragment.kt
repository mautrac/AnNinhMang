package com.example.message.ui

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.message.R
import com.example.message.databinding.FragmentChatroomBinding
import com.example.message.ui.adapter.MessageAdapter
import com.example.message.util.HandShake
import com.example.message.util.Temp
import com.example.message.viewmodel.ChatRoomViewModel
import com.example.message.viewmodel.ChatRoomViewModelFactory
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.File
import java.math.BigInteger
import javax.crypto.spec.SecretKeySpec

class ChatRoomFragment : Fragment() {

    private val viewModel: ChatRoomViewModel by activityViewModels {
        ChatRoomViewModelFactory()
    }

    private var database: DatabaseReference = Firebase.database.reference
    private val handShakeRef: DatabaseReference = database.child("hand-shakes")

    private val navigationArgs: ChatRoomFragmentArgs by navArgs()

    private lateinit var binding: FragmentChatroomBinding

    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatroomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding.chatRoomFragment = this@ChatRoomFragment

        uid = navigationArgs.uid
        Log.d(this.toString(), uid)
        //set
        Temp.retrieverPublicKey = Pair(
            BigInteger(Temp.retriever?.publicKey?.first.toString()),
            BigInteger(Temp.retriever?.publicKey?.second.toString()),
        )
        Log.d("retriever pk", Temp.retrieverPublicKey.toString())
        //set aes key


        if (!readAESKeyToTemp(uid) ) {
            Log.d("read key", "fail")
            //readAESKeyToTemp(uid)
            var hs = HandShake(uid, Temp.currentUser!!.uid, Temp.retrieverPublicKey!!, requireContext())
            hs.acceptHandShakeRequest()

        } else {
            Log.d("read key", "scuccess")
            Log.d("key after reading", Temp.aesKey!!.encoded.size.toString())
        }


        Log.d(this.toString(), Temp.retrieverPublicKey.toString())
        val currentUserUid = Temp.currentUser?.uid ?: ""

        viewModel.getMessages(currentUserUid, uid)

        val adapter = MessageAdapter(uid = uid)

        binding.recyclerViewChats.adapter = adapter

        viewModel.messages.observe(this@ChatRoomFragment.viewLifecycleOwner) {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun onSendButton() {

        val text = binding.textInput.text.toString()
        if (viewModel.checkInput(text)) {
            Toast.makeText(
                context,
                getString(R.string.text_notification),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            viewModel.sendMessage(
                senderID = Temp.currentUser!!.uid,
                retrievedID = uid,
                text = text
            )
            Temp.messageTemp = text
            binding.textInput.setText("")
        }
        binding.recyclerViewChats.adapter?.notifyDataSetChanged()

    }

    private fun readAESKeyToTemp(uid: String) : Boolean {
        //read aeskey
        //val path = context.getFilesDir()
        val path = requireContext().filesDir

        val letDirectory = File(path, "AESKeys")

        val file = File(letDirectory, Temp.currentUser!!.uid + ".txt")
        var check = false

        if (file.isFile) {
            val contents = file.readText()
            Log.d("file", contents)
            var i = 0
            var spaceIdx = 0
            for (j in 0..contents.length - 1) {
                if (contents[j] == '\n' && i < j) {
                    val retrieverId = contents.substring(i, spaceIdx)
                    val aesKeyStr = contents.substring(spaceIdx + 1, j)
                    if (retrieverId.equals(uid)) {
                        //val aesKeyByteArray = BigInteger(aesKeyStr)
                        val aesKeyByteArray = Base64.decode(aesKeyStr, Base64.DEFAULT)
                        Temp.aesKey = SecretKeySpec(aesKeyByteArray, "AES")
                        check = true
                        Log.d("read key from file", Temp.aesKey!!.encoded.size.toString())
                        break
                    }
                    i = j + 2
                }
                if (contents[j] == ' ')
                    spaceIdx = j
            }
        }
        return check
    }

}