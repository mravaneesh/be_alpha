package com.example.home_ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_domain.model.AiSuggestion
import com.example.ai_domain.usecase.GetPersonalizedSuggestionsUseCase
import com.example.home_domain.model.Post
import com.example.home_domain.usecase.GetFeedPostUseCase
import com.example.onboarding_domain.model.UserPreferences
import com.example.onboarding_domain.usecases.GetUserPreferencesUseCase
import com.example.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFeedPostUseCase: GetFeedPostUseCase,
    private val getPersonalizedSuggestionsUseCase: GetPersonalizedSuggestionsUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase
):ViewModel() {
    private val TAG = "HomeViewModel"
    var isHabitCardClosed = false

    private val _suggestions = MutableLiveData<AiSuggestion>()
    val suggestions: LiveData<AiSuggestion> = _suggestions

    fun loadSuggestions(userId: String) {
        Log.i(TAG, "Loading AI suggestions for user: $userId")
        viewModelScope.launch {
            try {
                val prefs: UserPreferences = getUserPreferencesUseCase(userId)
                Log.i(TAG, "User preferences found: $prefs")
                val aiSuggestion = getPersonalizedSuggestionsUseCase(prefs)
                _suggestions.value = aiSuggestion
                Log.i(TAG, "AI suggestions loaded: $aiSuggestion")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading AI suggestions", e)
            }
        }
    }


    private val _feedState = MutableLiveData<Resource<List<Post>>>()
    val feedState: LiveData<Resource<List<Post>>> = _feedState

    fun loadFeed(userId: String) {
        viewModelScope.launch {
            getFeedPostUseCase(userId).collect {
                _feedState.value = it
            }
        }
    }
}