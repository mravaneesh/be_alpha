package com.example.home_domain.usecase

import android.util.Log
import com.example.home_domain.model.Post
import com.example.home_domain.repository.PostRepository
import com.example.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetFeedPostUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(currentUserId: String) : Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())

        try {
            val posts = repository.getFeedPosts(currentUserId)
            Log.i("GetFeedPostUseCase", "invoke: $posts")
            emit(Resource.Success(data = posts))
        } catch (e:Exception) {
            emit(Resource.Error(message = e.message.toString()))
        }
    }
}