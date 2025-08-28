package com.example.home_ui.addPost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.home_ui.R
import com.example.home_ui.addPost.adapter.HabitSelectAdapter
import com.example.home_ui.databinding.FragmentHabitSelectBinding
import com.example.utils.CommonFun
import com.example.utils.model.GoalModel
import com.google.firebase.firestore.FirebaseFirestore

class HabitSelectFragment : Fragment() {

    private var _binding: FragmentHabitSelectBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HabitSelectAdapter
    private val habitList = mutableListOf<GoalModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitSelectBinding.inflate(inflater, container, false)
        adapter = HabitSelectAdapter(habitList) { goal ->
            navigateToAnalytics(goal.id)
        }
        binding.rvHabits.adapter = adapter
        binding.rvHabits.layoutManager = LinearLayoutManager(requireContext())

        fetchHabits()
        return binding.root
    }

    private fun fetchHabits() {
        val userId = CommonFun.getCurrentUserId() ?: return
        FirebaseFirestore.getInstance()
            .collection("goals")
            .document(userId)
            .collection("Habit")
            .get()
            .addOnSuccessListener { result ->
                habitList.clear()
                for (doc in result.documents) {
                    doc.toObject(GoalModel::class.java)?.let { habitList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun navigateToAnalytics(goalId: String) {
        val destination = "habitAnalytics?goalId=${goalId}&postMode=true"
        CommonFun.navigateToDeepLinkFragment(
            findNavController(),
            destination
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
