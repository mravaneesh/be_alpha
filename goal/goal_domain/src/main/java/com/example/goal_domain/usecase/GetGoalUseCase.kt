package com.example.goal_domain.usecase

import com.example.goal_domain.model.Goal
import com.example.goal_domain.repository.GoalRepository
import com.example.utils.CommonFun
import com.example.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetGoalsUseCase@Inject constructor
    (private val repository: GoalRepository) {
     operator fun invoke(userId: String,category: String): Flow<Resource<List<Goal>>> = flow {
         emit(Resource.Loading())
         try{
             emit(Resource.Success(data = repository.getGoals(userId,category)))
         }catch (e:Exception){
             emit(Resource.Error(e.message.toString()))
         }
     }
}
