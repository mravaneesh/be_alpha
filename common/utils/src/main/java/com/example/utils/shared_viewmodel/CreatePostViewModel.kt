package com.example.utils.shared_viewmodel

import android.graphics.Bitmap
import android.view.View
import androidx.core.view.drawToBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class SectionState (
    val isLoading:Boolean = false,
    val bitmap: Bitmap? = null
)

class CreatePostViewModel: ViewModel() {
    private val _sectionPreviews = MutableStateFlow<Map<String, SectionState>>(emptyMap())
    val sectionPreviews: StateFlow<Map<String, SectionState>> = _sectionPreviews

    fun captureSection(sectionKey:String, view: View) {
        _sectionPreviews.update { current ->
            current + (sectionKey to SectionState(isLoading = true))
        }

        view.post {
            val bitmap = view.drawToBitmap()
            _sectionPreviews.update { current ->
                current + (sectionKey to SectionState(isLoading = false, bitmap = bitmap))
            }
        }
    }

    fun removeSection(sectionKey: String) {
        _sectionPreviews.update { current ->
            current - sectionKey
        }
    }

    fun getCapturedBitmaps(): List<Bitmap> {
        return _sectionPreviews.value.values.mapNotNull { it.bitmap }
    }

    fun isSectionCaptured(sectionKey: String): Boolean {
        return _sectionPreviews.value[sectionKey]?.bitmap != null
    }

    fun clearPreviews() {
        _sectionPreviews.value = emptyMap()
    }
}