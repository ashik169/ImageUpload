package com.ashik.imageupload.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.ashik.imageupload.R
import com.ashik.imageupload.databinding.ActivityHomeBinding
import com.ashik.imageupload.ui.upload.PreviewUploadFragment
import com.ashik.imageupload.utils.Constants

class HomeActivity : AppCompatActivity(), HomeCallback {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding

    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.toolbar.setNavigationOnClickListener { _ ->  navController.navigateUp(appBarConfiguration) }
        navController.addOnDestinationChangedListener { _, navDestination, _ ->
            if (navDestination.id == R.id.FileInfoDialog) {
                return@addOnDestinationChangedListener
            }
            binding.toolbar.title = navDestination.label
            if (navDestination.id == R.id.HomeFragment) {
                binding.toolbar.subtitle = null
                binding.layoutLoading.isVisible = false
            }
        }
        handleIntent(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        handleIntent()
    }

    private fun handleIntent(savedInstanceState: Bundle? = null) {
        Log.i("HomeActivity", "handleIntent $savedInstanceState")
        if (savedInstanceState == null) {
            val uris = mutableListOf<Uri>()
            val clipData = intent?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i).uri
                    uris.add(clipData.getItemAt(i).uri)
                }
            } else {
                intent?.data?.let(uris::add)
            }
            if (uris.isNotEmpty()) {
                val filteredUris =
                    if (uris.size > Constants.MAX_IMAGE_UPLOAD) uris.take(Constants.MAX_IMAGE_UPLOAD) else uris
                Log.i(
                    "HomeActivity",
                    "Received Uris -> ${uris.joinToString(transform = Uri::toString)}"
                )
                if (navController.currentDestination?.id != R.id.UploadImageFragment) {
                    navController.navigate(
                        R.id.action_navigate_to_upload_image, bundleOf(
                            PreviewUploadFragment.IMAGE_URIS to filteredUris
                        )
                    )
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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

    override fun updateSubtitle(value: String?) {
        binding.toolbar.subtitle = value
    }
}