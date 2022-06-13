package io.simplelogin.android.module.settings.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import io.simplelogin.android.databinding.LayoutSenderAddressFormatCardViewBinding
import io.simplelogin.android.databinding.SpinnerRowTextOnlyBinding
import io.simplelogin.android.utils.enums.SenderFormat
import io.simplelogin.android.utils.enums.SenderFormat.*

class SenderAddressFormatCardView : RelativeLayout {
    // Initializer
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding = LayoutSenderAddressFormatCardViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    private val senderFormats = listOf(A, AT)

    // Functions
    fun bind(senderFormat: SenderFormat) {
        binding.senderAddressFormatSpinner.adapter = SenderAddressFormatSpinnerAdapter(context, senderFormats)
        binding.senderAddressFormatSpinner.setSelection(senderFormats.indexOfFirst { it == senderFormat })
    }

    fun setSenderAddressFormatSpinnerSelectionListener(listener: (SenderFormat) -> Unit) {
        val senderAddressFormatSpinnerAdapter = binding.senderAddressFormatSpinner.adapter ?: return
        binding.senderAddressFormatSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) = Unit

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedSenderFormat = senderAddressFormatSpinnerAdapter.getItem(position) as SenderFormat
                    listener(selectedSenderFormat)
                }
            }
    }
}

class SenderAddressFormatSpinnerAdapter(
    private val context: Context,
    private val senderFormats: List<SenderFormat>
) : BaseAdapter() {
    override fun getCount() = senderFormats.size
    override fun getItem(position: Int) = senderFormats[position]
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
        binding.textView.text = getItem(position).description
        return view
    }
}