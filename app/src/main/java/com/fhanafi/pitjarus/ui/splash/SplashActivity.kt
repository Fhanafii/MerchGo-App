package com.fhanafi.pitjarus.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fhanafi.pitjarus.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // TODO: Prepare splash navigation in the next phase.
    }
}
