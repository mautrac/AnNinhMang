package com.example.message.ui.adapter

import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.message.databinding.ItemRetrievedMessageBinding
import com.example.message.databinding.ItemSentMessageBinding
import com.example.message.model.Message
import com.example.message.util.AESEncryption
import com.example.message.util.Temp
import java.math.BigInteger

class MessageAdapter(
    private val uid: String
) : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {

    private val privateKey: Pair<BigInteger, BigInteger> = Pair(
        Temp.keyPair!!.second.first,
        Temp.keyPair!!.second.second
    )

    private val SENT_VIEW_TYPE = 1
    private val RETRIEVED_VIEW_TYPE = 2

    class ItemSentViewHolder(
        var binding: ItemSentMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.message = message
            binding.executePendingBindings()
        }
    }

    class ItemRetrievedViewHolder(
        val binding: ItemRetrievedMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.message = message
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            SENT_VIEW_TYPE -> {
                ItemSentViewHolder(
                    ItemSentMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            RETRIEVED_VIEW_TYPE -> {
                ItemRetrievedViewHolder(
                    ItemRetrievedMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        Log.d(this.toString(), "${message.text}")
        when (holder) {
            is ItemSentViewHolder -> {
//                Temp.messageTemp?.let {
//                    message.text = Temp.messageTemp
//                }
//                holder.bind(message)
                val text = message.text
                val b64 = Base64.decode(text, Base64.DEFAULT)

                val temp = AESEncryption.decrypt(b64, Temp.aesKey!!)
                //val temp2 = AESEncryption.decrypt(temp, Temp.aesKey!!)
                Log.d(this.toString(), temp.toString())
                //Log.d(this.toString(), temp2.toString())
                //message.text = String(temp, Charsets.UTF_8)
                var m = message.copy()
                m.text = String(temp, Charsets.UTF_8)
                holder.bind(m)
            }

            is ItemRetrievedViewHolder -> {
                val text = message.text
                val b64 = Base64.decode(text, Base64.DEFAULT)
                Log.d("Message decryption", b64.size.toString())
                val temp = AESEncryption.decrypt(b64, Temp.aesKey!!)
                //val temp2 = AESEncryption.decrypt(temp, Temp.aesKey!!)
                Log.d(this.toString(), temp.toString())
                //Log.d(this.toString(), temp2.toString())
                //message.text = String(temp, Charsets.UTF_8)
                var m = message.copy()
                m.text = String(temp, Charsets.UTF_8)
                holder.bind(m)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).retrieverID == uid)
            SENT_VIEW_TYPE
        else
            RETRIEVED_VIEW_TYPE
    }
    companion object DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.senderID == newItem.senderID
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
