package com.fhanafi.pitjarus.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fhanafi.pitjarus.databinding.FragmentAttendanceBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceFragment : Fragment() {
    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = requireNotNull(_binding)
    private lateinit var viewModel: AttendanceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[AttendanceViewModel::class.java]
        bindCurrentDateTime()
        binding.buttonCheckIn.setOnClickListener {
            findNavController().navigate(AttendanceFragmentDirections.actionAttendanceFragmentToStoreListFragment())
        }
    }

    private fun bindCurrentDateTime() = with(binding) {
        val now = Date()
        textDate.text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(now)
        textTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
