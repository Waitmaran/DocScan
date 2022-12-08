package com.colin.docscan

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.colin.docscan.databinding.ActivityNewScanBinding
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import com.zynksoftware.documentscanner.model.ScannerResults


class NewScanActivity : com.zynksoftware.documentscanner.ScanActivity() {
    private lateinit var binding: ActivityNewScanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addFragmentContentLayout()
    }

    override fun onError(error: DocumentScannerErrorModel) {
        TODO("Not yet implemented")
    }

    override fun onSuccess(scannerResults: ScannerResults) {
        val imageFile = if(scannerResults.transformedImageFile != null) {
            scannerResults.transformedImageFile
        } else {
            scannerResults.croppedImageFile
        }
        val returnIntent = Intent()

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        val imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )!!

        contentResolver.openOutputStream(imageUri)?.use {
            BitmapFactory.decodeFile(imageFile?.path).compress(Bitmap.CompressFormat.JPEG, 95, it)
        }

        Log.d("URI", "URI: $imageUri")
        returnIntent.putExtra("image", imageUri)
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    override fun onClose() {
        finish()
    }
}