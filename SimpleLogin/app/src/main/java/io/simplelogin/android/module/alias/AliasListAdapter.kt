package io.simplelogin.android.module.alias

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.viewholder.AliasViewHolder

class AliasListAdapter(private val clickListener: ClickListener) : RecyclerView.Adapter<AliasViewHolder>() {
    interface ClickListener {
        fun onClick(alias: Alias)
        fun onSwitch(alias: Alias)
        fun onCopy(alias: Alias)
        fun onSendEmail(alias: Alias)
        fun onDelete(alias: Alias, position: Int)
    }

    private var _aliases = listOf<Alias>()
    fun setAliases(aliases: List<Alias>) {
        _aliases = aliases
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = _aliases.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AliasViewHolder =
        AliasViewHolder.from(parent)

    override fun onBindViewHolder(holder: AliasViewHolder, position: Int) =
        holder.bind(_aliases[position], clickListener)
}