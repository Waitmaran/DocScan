package com.colin.docscan

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.colin.docscan.databinding.ActivityTextPreviewBinding


class TextPreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextPreviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextPreviewBinding.inflate(layoutInflater)
        val text = intent.extras!!.get("text")
        val translatedText = intent.extras!!.get("translatedText")
        if(text != null) {
            binding.textViewText.text = text.toString()
        } else {
            binding.textViewText.text = "Нет данных о распознанном тексте"
        }

        if(translatedText != null) {
            binding.textViewTranslatedText.text = translatedText.toString()
        } else {
            binding.textViewTranslatedText.text = "Нет данных о переводе"
        }

        setContentView(binding.root)
    }
}