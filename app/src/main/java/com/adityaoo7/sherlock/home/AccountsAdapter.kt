package com.adityaoo7.sherlock.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.databinding.AccountItemBinding

class AccountsAdapter(private val viewModel: HomeViewModel) :
    ListAdapter<LoginAccount, AccountsAdapter.ViewHolder>(AccountsDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: AccountItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: HomeViewModel, item: LoginAccount) {
            binding.viewModel = viewModel
            binding.account = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AccountItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class AccountsDiffCallback : DiffUtil.ItemCallback<LoginAccount>() {
    override fun areItemsTheSame(oldItem: LoginAccount, newItem: LoginAccount): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LoginAccount, newItem: LoginAccount): Boolean {
        return oldItem == newItem
    }
}