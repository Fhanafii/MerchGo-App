package com.fhanafi.pitjarus.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fhanafi.pitjarus.databinding.FragmentWelcomeTransitionBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeTransitionFragment : Fragment() {
    private var _binding: FragmentWelcomeTransitionBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeTransitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1_250)
            findNavController().navigate(
                WelcomeTransitionFragmentDirections.actionWelcomeTransitionFragmentToAttendanceFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
