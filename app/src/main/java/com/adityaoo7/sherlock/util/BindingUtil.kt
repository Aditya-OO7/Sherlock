package com.adityaoo7.sherlock.util

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.home.AccountsAdapter

@BindingAdapter("app:items")
fun setItems(listView: RecyclerView, items: List<LoginAccount>?) {
    items?.let {
        (listView.adapter as AccountsAdapter).submitList(items)
    }
}

// FIXME: Currently these BindingAdapters are not working.
//  These will be checked later.
/*
@BindingAdapter("headingText")
fun TextView.setHeadingTextView(isRegistered: Boolean) {
    text = if (isRegistered) {
        context.getString(R.string.login_heading_text_view)
    } else {
        context.getString(R.string.register_heading_text_view)
    }
}

@BindingAdapter("buttonText")
fun Button.setButtonText(isRegistered: Boolean) {
    text = if (isRegistered) {
        context.getString(R.string.login_button_text)
    } else {
        context.getString(R.string.register_button_text)
    }
}
*/
