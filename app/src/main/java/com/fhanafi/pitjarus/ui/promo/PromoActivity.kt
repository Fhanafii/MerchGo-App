package com.fhanafi.pitjarus.ui.promo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fhanafi.pitjarus.databinding.ActivityPromoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PromoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // TODO: Prepare promo screen in the next phase.
    }
}
