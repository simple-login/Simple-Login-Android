package io.simplelogin.android.module.alias.create

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import io.simplelogin.android.databinding.SpinnerRowTextOnlyBinding

class AliasCreateSpinnerAdapter(private val context: Context, private val suffixes: List<String>) : BaseAdapter() {
    override fun getCount() = suffixes.size
    override fun getItem(position: Int) = suffixes[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val binding: SpinnerRowTextOnlyBinding
        if (convertView == null) {
            binding = SpinnerRowTextOnlyBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = convertView.tag as SpinnerRowTextOnlyBinding
        }
        binding.textView.text = getItem(position)
        return view
    }
}