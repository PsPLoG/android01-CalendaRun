package com.drunkenboys.calendarun

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.drunkenboys.calendarun.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbar2.setOnClickListener {
            binding.calendarMonth.isVisible = binding.calendarMonth.isVisible.not()
            binding.calendarYear.isVisible = binding.calendarYear.isVisible.not()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_schedule -> {
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
