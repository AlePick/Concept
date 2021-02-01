package com.example.concept2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class FriendAdapter(private val friendsList: List<Friend>, private val listener: OnItemClickListener?)
    : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val imageView: ImageView = itemView.findViewById(R.id.friend_image_view)
        val usernameView: TextView = itemView.findViewById(R.id.friend_username_view)
        val statusView: TextView = itemView.findViewById(R.id.friend_status_view)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            if (position != RecyclerView.NO_POSITION && listener != null){
                listener?.onItemClick(position)
            }
        }
    }
    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.friends_list_row, parent, false)

        return FriendViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentFriend = friendsList[position]
        holder.imageView.setImageResource(currentFriend.image)
        holder.usernameView.text = currentFriend.username
        if (currentFriend.online){
            if (currentFriend.ingame){
                holder.statusView.text = "In Game"
            } else{
                holder.statusView.text = "Online"
            }
        } else {
            holder.statusView.text = "Offline"
        }
    }

    override fun getItemCount()= friendsList.size
}