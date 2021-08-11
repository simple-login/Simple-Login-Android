package io.simplelogin.android.utils

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.viewholder.LoadingViewHolder

class LoadingFooterAdapter : RecyclerView.Adapter<LoadingViewHolder>() {
    var isLoading = false

    override fun getItemCount() = if (isLoading) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LoadingViewHolder.from(parent)

    override fun onBindViewHolder(holder: LoadingViewHolder, position: Int) = Unit
}
