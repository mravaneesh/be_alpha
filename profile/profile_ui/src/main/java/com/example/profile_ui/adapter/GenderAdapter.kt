package com.example.profile_ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.profile_ui.R

class GenderAdapter(
    private val genderList: List<String>,
    private var selectedGender: String?,
    private val onGenderSelected: (String) -> Unit
) : RecyclerView.Adapter<GenderAdapter.GenderViewHolder>() {

    private var selectedPosition = genderList.indexOf(selectedGender)

    inner class GenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tvGender)
        val tickIcon: ImageView = itemView.findViewById(R.id.imgCheck)
        val divider: View = itemView.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gender, parent, false)
        return GenderViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenderViewHolder, position: Int) {
        val gender = genderList[position]
        holder.textView.text = gender

        holder.tickIcon.visibility = if (position == selectedPosition) View.VISIBLE else View.INVISIBLE
        holder.divider.visibility = if (position == genderList.size - 1) View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                selectedPosition = adapterPosition
                onGenderSelected(gender)
                notifyDataSetChanged()
            }
        }
    }


    override fun getItemCount(): Int = genderList.size
}
