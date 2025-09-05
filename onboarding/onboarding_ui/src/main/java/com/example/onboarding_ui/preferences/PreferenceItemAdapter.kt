package com.example.onboarding_ui.preferences

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.onboarding_ui.R
import com.example.onboarding_ui.preferences.model.PreferenceItem

class PreferenceItemAdapter(
    private val items: MutableList<PreferenceItem>,
    private val isMultiSelect: Boolean,
    private val onItemSelected: (PreferenceItem) -> Unit
) : RecyclerView.Adapter<PreferenceItemAdapter.PreferenceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreferenceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preference, parent, false)
        return PreferenceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PreferenceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPreference: TextView = itemView.findViewById(R.id.tvPreference)

        fun bind(item: PreferenceItem) {
            tvPreference.text = "${item.emoji?:""}  ${item.title}"

            // Background based on selection
            val bgRes = if (item.isSelected) {
                R.drawable.bg_pref_select
            } else {
                R.drawable.bg_pref_unselect
            }
            val textStyle = if (item.isSelected) {
                R.style.PreferenceText_Selected
            } else {
                R.style.PreferenceText_Unselected
            }
            tvPreference.setTextAppearance(textStyle)
            tvPreference.background =
                ContextCompat.getDrawable(itemView.context, bgRes)
            // Toggle selection
            itemView.setOnClickListener {
                if (!isMultiSelect) {
                    items.forEach { it.isSelected = false }
                    item.isSelected = true
                    notifyDataSetChanged()
                } else {
                    item.isSelected = !item.isSelected
                    notifyItemChanged(adapterPosition)
                }
                onItemSelected(item)
            }
        }
    }
}