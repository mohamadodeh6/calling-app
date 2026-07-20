package com.mohamadodeh6.callingapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mohamadodeh6.callingapp.databinding.ItemUserBinding
import com.mohamadodeh6.callingapp.models.UserDto

class UserAdapter(
    private val onCallClick: (UserDto) -> Unit,
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private var users: List<UserDto> = emptyList()

    fun submit(list: List<UserDto>) {
        users = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserDto) {
            binding.username.text = user.username
            binding.onlineIndicator.alpha = if (user.online) 1f else 0.25f
            binding.callButton.isEnabled = user.online
            binding.callButton.setOnClickListener { onCallClick(user) }
        }
    }
}
