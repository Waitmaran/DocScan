package com.colin.docscan

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.MutableLiveData
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File

object OcrHelper {
    private var tess: TessBaseAPI? = null
    val progress = MutableLiveData<String>()
    var complete: Boolean = false
    fun Init(cacheDir: File, pos: Int): Boolean {
        Log.d("RECOG SETTINGS OCR", pos.toString())
        val dataDir = cacheDir.absolutePath + File.separator.toString() //+ "tessdata" + File.separator
        val dataFile = File(dataDir).absolutePath
        tess = TessBaseAPI(){ progressValues ->
            progress.postValue("Progress: ${progressValues.percent}%")
            //линия для теста
        }
        return if (!tess!!.init(dataFile, getLanguages(pos))) {
            // Error initializing Tesseract (wrong data path or language)
            tess!!.recycle()
            false
        } else {
            Log.d("RECOG", "TRAINED DATA READ! RUNNING:${getLanguages(pos)}")
            true
        }
    }

    private fun getLanguages(pos: Int): String {
        // fast - 0
        // accurate - 1
        // balance - 2
        if (pos == 0) {
            return "fast_rus+fast_eng"
        }else if (pos == 1) {
            return "rus+eng"
        }else if (pos == 2) {
            return "balance_rus+balance_eng"
        }else {
            return "rus+eng"
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