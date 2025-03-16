package com.example.goal_ui.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.goal_ui.R

class ColorAdapter(
    private val colors: List<Int>,
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var selectedPosition = -1

    inner class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorView: View = view.findViewById(R.id.colorItem)

        fun bind(color:Int, isSelected:Boolean){
            val drawable = ContextCompat.getDrawable(itemView.context, R.drawable.bg_color_picker) as GradientDrawable
            drawable.setColor(color)  // Set dynamic color

            drawable.setStroke(if (isSelected) 6 else 0,Color.WHITE )

            colorView.background = drawable
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val colorRes = colors[position]
        holder.bind(colorRes, position == selectedPosition)
        holder.colorView.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            val previousPosition = selectedPosition
            selectedPosition = currentPosition
            notifyItemChanged(previousPosition) // Refresh previous selection
            notifyItemChanged(selectedPosition) // Refresh new selection
            onColorSelected(colorRes)
        }

    }

    override fun getItemCount(): Int = colors.size
}
