package com.example.onboarding_ui.preferences

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onboarding_ui.R
import com.example.onboarding_ui.preferences.model.PreferenceSection

class PreferencesAdapter(
    private val sections: List<PreferenceSection>,
    private val onItemSelected: (String, String) -> Unit
) : RecyclerView.Adapter<PreferencesAdapter.SectionViewHolder>() {

    private val selectedItems: MutableMap<String, MutableList<String>> = mutableMapOf()

    fun getSelectedPreferences(): Map<String, List<String>> {
        return selectedItems
    }

    fun isAllSectionsSelected(): Boolean {
        return sections.all { section ->
            val list = selectedItems[section.sectionTitle]
            !list.isNullOrEmpty()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(sections[position])
    }

    override fun getItemCount(): Int = sections.size

    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSectionTitle: TextView = itemView.findViewById(R.id.tvSectionTitle)
        private val rvItems: RecyclerView = itemView.findViewById(R.id.rvItems)

        fun bind(section: PreferenceSection) {
            tvSectionTitle.text = section.sectionTitle

            rvItems.layoutManager = GridLayoutManager(itemView.context, 2)
            rvItems.adapter = PreferenceItemAdapter(
                section.items.toMutableList(),
                section.isMultiSelect
            ) { item ->
                val list = selectedItems.getOrPut(section.sectionTitle){ mutableListOf() }
                if (section.isMultiSelect) {
                    if (list.contains(item.title)) {
                        list.remove(item.title)
                    } else {
                        list.add(item.title)
                    }
                } else {
                    list.clear()
                    list.add(item.title)
                }

                onItemSelected(section.sectionTitle, item.title)
            }
        }
    }
}