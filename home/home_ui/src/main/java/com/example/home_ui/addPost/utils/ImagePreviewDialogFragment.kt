package com.example.home_ui.addPost.utils

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.home_ui.R

class ImagePreviewDialogFragment(private val bitmap: Bitmap) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_image_preview, container, false)

        val imageView = view.findViewById<ImageView>(R.id.ivPreview)
        imageView.setImageBitmap(bitmap)

        imageView.setOnClickListener {
            dismiss()
        }

        return view
    }
}