package com.fhanafi.pitjarus.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fhanafi.pitjarus.databinding.FragmentLoginBinding
import com.fhanafi.pitjarus.utils.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeState()
        viewModel.checkSession()
        binding.buttonLogin.setOnClickListener {
            viewModel.login(
                binding.inputUsername.editText?.text?.toString().orEmpty(),
                binding.inputPassword.editText?.text?.toString().orEmpty()
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.loading.root.visibility = if (state is UiState.Loading) View.VISIBLE else View.GONE
                    binding.buttonLogin.isEnabled = state !is UiState.Loading
                    when (state) {
                        is UiState.Success -> findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToAttendanceFragment())
                        is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        is UiState.Unauthorized -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
