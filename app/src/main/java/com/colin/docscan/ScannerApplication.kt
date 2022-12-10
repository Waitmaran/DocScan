package com.colin.docscan

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.zynksoftware.documentscanner.ui.DocumentScanner

class ScannerApplication: Application()  {
    val dataStore: DataStore<Preferences> by preferencesDataStore("recognizer_mode")
    override fun onCreate() {
        super.onCreate()
        val configuration = DocumentScanner.Configuration()
        configuration.imageQuality = 100
        configuration.imageSize = 800000 // 1 MB
        configuration.imageType = Bitmap.CompressFormat.JPEG
        DocumentScanner.init(this, configuration) // or simply DocumentScanner.init(this)
    }
}