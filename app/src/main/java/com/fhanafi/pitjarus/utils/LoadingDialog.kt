package com.fhanafi.pitjarus.utils

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.fhanafi.pitjarus.databinding.DialogLoadingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoadingDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        val binding = DialogLoadingBinding.inflate(LayoutInflater.from(requireContext()))
        binding.textMessage.text = requireArguments().getString(ARG_MESSAGE).orEmpty()
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    companion object {
        private const val ARG_MESSAGE = "message"
        const val TAG = "global_loading_dialog"

        fun newInstance(message: String): LoadingDialog {
            return LoadingDialog().apply {
                arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
            }
        }
    }
}
