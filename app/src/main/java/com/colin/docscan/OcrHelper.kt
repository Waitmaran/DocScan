package com.colin.docscan

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File


object OcrHelper {
    private var tess: TessBaseAPI? = null
    val progress = MutableLiveData<String>()
    var complete: Boolean = false
    fun Init(cacheDir: File): Boolean {
        val dataDir = cacheDir.absolutePath + File.separator.toString() //+ "tessdata" + File.separator
        val dataFile = File(dataDir).absolutePath
        tess = TessBaseAPI(){ progressValues ->
            progress.postValue("Progress: ${progressValues.percent}%")
        }
        return if (!tess!!.init(dataFile, "rus")) {
            // Error initializing Tesseract (wrong data path or language)
            tess!!.recycle()
            false
        } else {
            Log.d("RECOG", "TRAINED DATA READ!")
            true
        }
    }

    fun recognize(imageUri: Uri, context: Context): String {
        complete = false
        val bitmapReal = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        if(tess != null) {
            tess!!.setImage(bitmapReal)
        } else {
            throw Exception("tess is not initialized")
        }

        tess!!.getHOCRText(0);
        val text = tess!!.utF8Text!!
        complete = true
        progress.postValue("Текст успешно извлечен!")
        tess!!.recycle()
        return text
    }
}