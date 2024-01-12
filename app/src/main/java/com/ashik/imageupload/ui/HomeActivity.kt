package com.ashik.imageupload.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.ashik.imageupload.R
import com.ashik.imageupload.databinding.ActivityMainBinding

class HomeActivity : AppCompatActivity(), HomeCallback {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, navDestination, _ ->
            binding.toolbar.title = navDestination.label
            if (navDestination.id == R.id.PickImageFragment) {
                binding.layoutLoading.isVisible = false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override val isLoading: Boolean
        get() = loading

    override fun showLoading() {
        loading = true
        binding.layoutLoading.isVisible = true
    }

    override fun hideLoading() {
        loading = false
        binding.layoutLoading.isVisible = false
    }
}