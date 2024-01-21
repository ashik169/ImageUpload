package com.ashik.imageupload.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.ashik.imageupload.R
import com.ashik.imageupload.databinding.ActivityHomeBinding
import com.ashik.imageupload.extensions.getUriForFile
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.ui.upload.PreviewUploadFragment
import com.ashik.imageupload.utils.Constants
import java.io.File

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
        binding.toolbar.setNavigationOnClickListener { _ ->
            navController.navigateUp(
                appBarConfiguration
            )
        }
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
        if (savedInstanceState == null) {
            Log.d("HomeActivity", "Action -> ${intent?.action}")
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

    override fun shareFile(fileInfoModel: FileInfoModel) {
        val fileUri = getUriForFile(fileInfoModel.file)
        val fileType = contentResolver?.getType(fileUri)
        val intent = ShareCompat.IntentBuilder(this).setType(fileType ?: "image/*")
            .setSubject("Shared files").addStream(fileUri).setChooserTitle("Share an image").intent
        startShareIntent(intent)
    }

    override fun shareFiles(selectedItems: List<FileInfoModel>) {
        val intentBuilder = ShareCompat.IntentBuilder(this).setSubject("Shared images")
            .setChooserTitle("Share an image")
        val fileUris = selectedItems.map { getUriForFile(it.file) }.onEach {
            intentBuilder.addStream(it)
        }
        val fileType = contentResolver?.getType(fileUris.first())
        intentBuilder.setType(fileType ?: "image/*")
        startShareIntent(intentBuilder.intent)
    }

    private fun startShareIntent(shareIntent: Intent) {
        val intentChooser = Intent.createChooser(shareIntent, "Share images")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            val components = arrayOf(ComponentName(this, HomeActivity::class.java))
            val components = arrayOf(this.intent.component)
            intentChooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, components)
        }
        startActivity(intentChooser)
    }

}