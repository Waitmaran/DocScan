package com.colin.docscan

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import com.google.firebase.FirebaseApp
import com.huawei.hms.mlsdk.common.MLApplication
import com.zynksoftware.documentscanner.ui.DocumentScanner

class ScannerApplication: Application()  {
    override fun onCreate() {
        super.onCreate()
        //FirebaseApp.initializeApp(this)
        MLApplication.getInstance().apiKey = "your ApiKey";

        val configuration = DocumentScanner.Configuration()
        configuration.imageQuality = 80
        configuration.imageSize = 1000000 // 1 MB
        configuration.imageType = Bitmap.CompressFormat.JPEG
        DocumentScanner.init(this, configuration) // or simply DocumentScanner.init(this)
    }
}