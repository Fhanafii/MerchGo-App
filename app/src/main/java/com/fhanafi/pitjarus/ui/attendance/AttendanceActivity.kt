package com.fhanafi.pitjarus.ui.attendance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fhanafi.pitjarus.databinding.ActivityAttendanceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AttendanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAttendanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // TODO: Prepare attendance screen in the next phase.
    }
}
