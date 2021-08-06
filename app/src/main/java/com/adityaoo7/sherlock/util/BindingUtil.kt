package com.adityaoo7.sherlock.util

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.home.AccountsAdapter

@BindingAdapter("items")
fun setItems(listView: RecyclerView, items: List<LoginAccount>?) {
    items?.let {
        (listView.adapter as AccountsAdapter).submitList(items)
    }
}