package io.simplelogin.android.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.databinding.RecyclerItemLoadingBinding

class LoadingViewHolder(binding: RecyclerItemLoadingBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): LoadingViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerItemLoadingBinding.inflate(layoutInflater, parent, false)
            return LoadingViewHolder(binding)
        }
    }
}
