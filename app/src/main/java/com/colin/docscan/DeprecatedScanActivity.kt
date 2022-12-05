package com.colin.docscan

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.colin.docscan.databinding.ActivityScanBinding
import kotlinx.coroutines.launch
import java.io.InputStream


class DeprecatedScanActivity : AppCompatActivity() {
    lateinit var binding: ActivityScanBinding
    lateinit var imageUri: Uri
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityScanBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonCropDone.setOnClickListener {
            lifecycleScope.launch {
                //binding.progressBar.isVisible = true
//                val image = binding.documentScanner.getCroppedImage()
//                val path: String =
//                    MediaStore.Images.Media.insertImage(contentResolver, image, "title", "descr")

                val returnIntent = Intent()
//                returnIntent.putExtra("image", Uri.parse(path))
                setResult(RESULT_OK, returnIntent)
                finish()
                //binding.progressBar.isVisible = false
            }
        }

        val cameraContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                val thumbnail = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(contentResolver, imageUri), 720, 1280, true);
                val input: InputStream = contentResolver.openInputStream(imageUri)!!
                val ei = ExifInterface(input)
                val orientation: Int = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
                )
                var rotatedBitmap: Bitmap? = null
                rotatedBitmap = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(thumbnail, 90)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(thumbnail, 180)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(thumbnail, 270)
                    ExifInterface.ORIENTATION_NORMAL -> thumbnail
                    else -> thumbnail
                }
                //val image = activityResult?.data?.data
                //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, image)
//                binding.imageView.setImageBitmap(thumbnail)
//                binding.imageView.visibility = View.VISIBLE
//
//                binding.documentScanner.setImage(rotatedBitmap!!)
            }
        }

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )!!
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraContract.launch(intent)
//        binding.documentScanner.setOnLoadListener { loading ->
//            binding.progressBar.isVisible = loading
//        }
    }
    fun rotateImage(source: Bitmap, angle: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }
}