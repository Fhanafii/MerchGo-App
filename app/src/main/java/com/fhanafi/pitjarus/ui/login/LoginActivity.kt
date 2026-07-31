package com.fhanafi.pitjarus.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fhanafi.pitjarus.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // TODO: Prepare login screen in the next phase.
    }
}
