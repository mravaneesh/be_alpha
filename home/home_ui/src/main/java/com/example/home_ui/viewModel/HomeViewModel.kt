package com.example.home_ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.home_domain.model.Post
import com.example.home_domain.usecase.GetFeedPostUseCase
import com.example.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFeedPostUseCase: GetFeedPostUseCase
):ViewModel() {
    var isHabitCardClosed = false

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