package io.simplelogin.android.module.alias.create

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import io.simplelogin.android.R

class AliasCreateSpinnerAdapter(context: Context, private val suffixes: List<String>) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)

    override fun getCount() = suffixes.size
    override fun getItem(position: Int) = suffixes[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: SpinnerRowHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.spinner_row_text_only, parent, false)
            viewHolder = SpinnerRowHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as SpinnerRowHolder
        }

        viewHolder.textView.text = getItem(position)
        return view
    }
}

private class SpinnerRowHolder(row: View?) {
    val textView: TextView = row?.findViewById(R.id.textView) as TextView
}