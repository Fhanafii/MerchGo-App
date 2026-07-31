package com.fhanafi.pitjarus.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fhanafi.pitjarus.databinding.ActivityStoreDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoreDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // TODO: Prepare store detail screen in the next phase.
    }
}
