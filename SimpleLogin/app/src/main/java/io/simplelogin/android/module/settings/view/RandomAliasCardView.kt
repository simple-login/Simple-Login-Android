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
import io.simplelogin.android.databinding.LayoutRandomAliasCardViewBinding
import io.simplelogin.android.databinding.SpinnerRowDomainLiteBinding
import io.simplelogin.android.databinding.SpinnerRowTextOnlyBinding
import io.simplelogin.android.utils.enums.RandomMode
import io.simplelogin.android.utils.model.DomainLite

class RandomAliasCardView : RelativeLayout {
    // Initializer
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding = LayoutRandomAliasCardViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    // Functions
    fun bind(randomMode: RandomMode, defaultDomain: String, domainLites: List<DomainLite>) {
        binding.randomModeSpinner.adapter = RandomModeSpinnerAdapter(context)
        binding.randomModeSpinner.setSelection(randomMode.position)
        binding.defaultDomainSpinner.adapter = DefaultDomainSpinnerAdapter(context, domainLites)
        binding.defaultDomainSpinner.setSelection(domainLites.indexOfFirst { it.name == defaultDomain })
    }

    fun setRandomModeSpinnerSelectionListener(listener: (RandomMode) -> Unit) {
        binding.randomModeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) = Unit

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedMode = RandomMode.fromPosition(position)
                    listener(selectedMode)
                }
            }
    }

    fun setDefaultDomainSpinnerSelectionListener(listener: (DomainLite) -> Unit) {
        val defaultDomainSpinnerAdapter = binding.defaultDomainSpinner.adapter ?: return
        binding.defaultDomainSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) = Unit

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedDomainLite = defaultDomainSpinnerAdapter.getItem(position) as DomainLite
                    listener(selectedDomainLite)
                }
            }
    }
}

class RandomModeSpinnerAdapter(private val context: Context) : BaseAdapter() {
    override fun getCount() = 2
    override fun getItem(position: Int) = RandomMode.fromPosition(position)
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

class DefaultDomainSpinnerAdapter(
    private val context: Context,
    private val domainLites: List<DomainLite>
) : BaseAdapter() {
    override fun getCount() = domainLites.size
    override fun getItem(position: Int) = domainLites[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val binding: SpinnerRowDomainLiteBinding
        if (convertView == null) {
            binding = SpinnerRowDomainLiteBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = convertView.tag as SpinnerRowDomainLiteBinding
        }
        val domainLite = getItem(position)
        binding.domainNameTextView.text = domainLite.name
        binding.ownerTextView.text = if (domainLite.isCustom) "Your domain" else "SimpleLogin provided domain"
        return view
    }
}