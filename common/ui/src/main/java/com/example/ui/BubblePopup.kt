package com.example.ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.ImageView
import com.example.ui.databinding.QuickOptionBinding

enum class BubbleItemType {
    DELETE,
    EDIT;

    fun getName(context: Context): String {
        return when (this) {
            DELETE -> context.getString(R.string.delete)
            EDIT -> context.getString(R.string.edit)
        }
    }

    fun getIcon(context: Context): Drawable? {
        return when (this) {
            DELETE -> context.getDrawable(R.drawable.ic_delete)
            EDIT -> context.getDrawable(R.drawable.ic_edit)
        }
    }
}

class BubblePopup {
    private var popupWindow: PopupWindow? = null
    private var clickListener: ((View, BubbleItemType) -> Unit)? = null

    fun show(anchor: View, options: List<BubbleItemType>) {
        val context = anchor.context
        val popupView = LayoutInflater.from(context).inflate(R.layout.quick_option, null)

        val popupLayout = popupView.findViewById<LinearLayout>(R.id.popup_layout)
        popupLayout.removeAllViews()

        for (option in options) {
            val optionView = LayoutInflater.from(context)
                .inflate(R.layout.quick_option_cell, popupLayout, false)

            optionView.findViewById<TextView>(R.id.popup_option_text).text = option.getName(context)
            optionView.findViewById<ImageView>(R.id.popup_option_image)
                .setImageDrawable(option.getIcon(context))

            optionView.setOnClickListener {
                clickListener?.invoke(anchor, option)
                popupWindow?.dismiss()
            }

            popupLayout.addView(optionView)
        }

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isFocusable = true
            setBackgroundDrawable(ColorDrawable(context.getColor(android.R.color.transparent)))
            elevation = 10f
            animationStyle = R.style.PopupAnimation
            showAsDropDown(anchor, 50, -anchor.height * 2 - 80)
        }
    }

    fun setOnClickListener(listener: (View, BubbleItemType) -> Unit) {
        clickListener = listener
    }
}
