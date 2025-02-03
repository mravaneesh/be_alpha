package com.example.goal_ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.goal_ui.R

class CategoryAdapter(private val categories: MutableList<String>, private val onClick: (String) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: TextView = itemView.findViewById(R.id.categoryButton)

        init {
            // Set onClickListener for each button
            itemView.setOnClickListener {
                onClick(categories[adapterPosition]) // Call the click listener with the category name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_button_item, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.button.text = categories[position]
    }

    override fun getItemCount() = categories.size

    // Method to add a new category
    fun addCategory(newCategory: String) {
        categories.add(newCategory)  // Add the new category to the list
        notifyItemInserted(categories.size - 1)  // Notify that an item has been inserted at the end
    }
}
