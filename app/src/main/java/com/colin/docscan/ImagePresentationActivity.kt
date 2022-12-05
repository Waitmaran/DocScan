package com.colin.docscan

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.colin.docscan.databinding.ActivityImagePresentationBinding

class ImagePresentationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImagePresentationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePresentationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageViewImagePresentation.setImageURI(intent.extras?.get("image") as Uri)
    }
}