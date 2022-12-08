package com.colin.docscan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.colin.docscan.databinding.ActivityTextPreviewBinding

class TextPreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextPreviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextPreviewBinding.inflate(layoutInflater)
        binding.textView4.text = intent.extras!!.get("text").toString()
        setContentView(binding.root)
    }
}