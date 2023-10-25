package dev.nicholasfragiskatos.resultsfromactivities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import dev.nicholasfragiskatos.resultsfromactivities.databinding.ActivityMainBinding
import java.io.File
import java.util.Date

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var photoFileName: String? = null

    private val startSecondActivityForResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult: ActivityResult ->
            if (activityResult.resultCode == SecondActivity.SECOND_ACTIVITY_RESULT_CODE) {
                activityResult.data?.getStringExtra(SecondActivity.SECOND_ACTIVITY_BUNDLE_ID)?.let {
                    Toast.makeText(this, "Got Result: $it", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val startSecondActivityCustomContractLauncher =
        registerForActivityResult(MyCustomContract()) { result: String? ->
            result?.let {
                Toast.makeText(this, "Got Result: $it", Toast.LENGTH_SHORT).show()
            }
        }

    private val startImplicitIntentToSelectPhotoFromGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let {
                binding.imageView.setImageURI(it)
            }
        }

    private val requestTakePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            val photoFile = photoFileName?.let {
                File(applicationContext.filesDir, it)
            }

            if (photoFile?.exists() == true) {
                val toURI = photoFile.toURI()
                binding.imageView.setImageURI(Uri.parse(toURI.toString()))
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                photoFileName = "IMG_${Date()}.jpg"

                val file = File(applicationContext.filesDir, photoFileName)
                val uri = FileProvider.getUriForFile(
                    this,
                    "dev.nicholasfragiskatos.resultsfromactivities.fileprovider",
                    file
                )
                requestTakePicture.launch(uri)
//                startCameraUsingOldWay(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupButtons()
    }

    private fun setupButtons() {
        binding.btnStartSecondActivityOld.setOnClickListener {
            startSecondActivityForResultUsingOldWay()
        }

        binding.btnStartSecondActivityNew.setOnClickListener {
            startSecondActivityUsingNewApi()
        }

        binding.btnStartSecondActivityCustomContract.setOnClickListener {
            startSecondActivityCustomContractLauncher.launch("Custom Contract Input")
        }

        binding.btnStartImplicitIntentToPickPhoto.setOnClickListener {
            startImplicitIntentToSelectPhotoFromGalleryLauncher.launch("image/*")
        }

        binding.btnTakePicture.setOnClickListener {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startSecondActivityForResultUsingOldWay() {
        val intent = Intent(this, SecondActivity::class.java).putExtra(
            MAIN_ACTIVITY_BUNDLE_ID,
            "Input From Main Activity Old Way"
        )

        startActivityForResult(
            intent,
            MAIN_ACTIVITY_REQUEST_CODE
        )
    }

    private fun startSecondActivityUsingNewApi() {
        val intent = Intent(
            this,
            SecondActivity::class.java
        ).putExtra(
            MAIN_ACTIVITY_BUNDLE_ID,
            "Input From Main Activity New API"
        )
        startSecondActivityForResultLauncher.launch(intent)
    }

    private fun startCameraUsingOldWay(uri: Uri) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(cameraIntent, MAIN_ACTIVITY_CAMERA_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE && resultCode == SecondActivity.SECOND_ACTIVITY_RESULT_CODE) {
            data?.getStringExtra(SecondActivity.SECOND_ACTIVITY_BUNDLE_ID)?.let {
                Toast.makeText(this, "Got Result: $it", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == MAIN_ACTIVITY_CAMERA_REQUEST_CODE) {
            val success = resultCode == Activity.RESULT_OK
            if (success) {
                val photoFile = photoFileName?.let {
                    File(applicationContext.filesDir, it)
                }

                if (photoFile?.exists() == true) {
                    val toURI = photoFile.toURI()
                    binding.imageView.setImageURI(Uri.parse(toURI.toString()))
                }
            }
        }
    }

    companion object {
        const val MAIN_ACTIVITY_REQUEST_CODE = 500
        const val MAIN_ACTIVITY_CAMERA_REQUEST_CODE = 800
        const val MAIN_ACTIVITY_BUNDLE_ID = "MainActivityBundleId"
        const val PHOTO_NAME_BUNDLE_ID = "PHOTO_NAME_BUNDLE_ID"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(PHOTO_NAME_BUNDLE_ID, photoFileName)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        photoFileName = savedInstanceState.getString(PHOTO_NAME_BUNDLE_ID)
        super.onRestoreInstanceState(savedInstanceState)
    }

    inner class MyCustomContract : ActivityResultContract<String, String?>() {
        override fun createIntent(context: Context, input: String): Intent {
            val intent = Intent(
                context,
                SecondActivity::class.java
            ).putExtra(
                MAIN_ACTIVITY_BUNDLE_ID,
                input
            )
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String? {
            if (resultCode == SecondActivity.SECOND_ACTIVITY_RESULT_CODE) {
                intent?.getStringExtra(SecondActivity.SECOND_ACTIVITY_BUNDLE_ID)?.let {
                    return it
                }
            }
            return null
        }

    }
}
