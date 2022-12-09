package com.colin.docscan

import DocStorage
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.emptyPreferences
import androidx.recyclerview.widget.LinearLayoutManager
import com.colin.docscan.databinding.ActivityNewDocumentBinding
import com.colin.docscan.ui.notifications.SettingsFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.random.Random

class NewDocumentActivity : AppCompatActivity() {


    private var recognition = false
    private var translation = false
    var doc = AppDocument("Doc${Random.nextInt()}")
    lateinit var binding: ActivityNewDocumentBinding
    lateinit var pageAdapter: PageAdapter
    var globPos: Int? = null
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchRecognition.setOnClickListener {
            Log.d("RECOG", "SWITCHED!!!")
            recognition = !recognition
            Log.d("RECOG", recognition.toString())
        }
        binding.switchTranslate.setOnClickListener {
            Log.d("RECOG", "SWITCHED!!!")
            translation = !translation
        }

        val position: Int
        pageAdapter = PageAdapter(doc.pages, this, this)

        if(intent.extras?.get("Edit") as Boolean) {
            position = intent.extras?.get("EditPos") as Int

            doc = if(intent.extras?.get("Done") as Boolean) {
                DocStorage.doneDocs.value?.get(position)!!
            } else {
                recognition = true
                translation = true
                binding.switchRecognition.isChecked = recognition
                binding.switchTranslate.isChecked = translation
                DocStorage.undoneDocs.value?.get(position)!!
            }

            binding.textViewNewDocTitle.setText(doc.name)
            binding.textViewNewDocTitle.isEnabled = false
            Log.d("DB FAKE", doc.pages.size.toString())
            binding.RecyclerViewDocuments.adapter = PageAdapter(doc.pages, this, this)
        } else {
            recognition = true
            translation = true
            binding.switchRecognition.isChecked = recognition
            binding.switchTranslate.isChecked = translation
            binding.textViewNewDocTitle.hint = doc.name
            position = DocStorage.addUndoneDoc(doc)!!
        }

//        binding.switchRecognition.setOnCheckedChangeListener { _, _ ->
//
//        }
//
//        binding.switchTranslate.setOnCheckedChangeListener { _, _ ->
//
//        }


        binding.floatingActionButtonShotPage.setOnClickListener {
            val intForScan = Intent(applicationContext, NewScanActivity::class.java)
            globPos = position
            startActivityForResult(intForScan, 999)
        }

        binding.buttonDocDone.setOnClickListener {
            binding.buttonDocDone.isClickable = false
            var count = 1

            if(recognition) {
                Log.d("RECOG", "DOING RECOG!")
                OcrHelper.progress.observe(this) { progress ->
                    binding.buttonDocDone.text =
                        "Обработка документа: ${count}/${doc.pages.size} (${progress})"
                }

                GlobalScope.launch(Dispatchers.IO) {
                    val pos = getRecognizerMode().first()
                    Log.d("RECOG SETTINGS NEW DOC", pos.toString())
                    for (page in doc.pages) {
                        OcrHelper.Init(cacheDir, pos)
                        val text =
                            OcrHelper.recognize(Uri.parse(page.bitmap), this@NewDocumentActivity)
                        page.text = text
                        count++
                        Log.d("RECOG", "COMPLETE")
                    }
                }.invokeOnCompletion {
                    GlobalScope.launch(Dispatchers.Main) {
                        manageDocs(position)
                        finish()
                    }
                }
            }
            else {
                Log.d("RECOG", "NOT DOING RECOG!")
                manageDocs(position)
                finish()
            }
        }

        binding.RecyclerViewDocuments.layoutManager = LinearLayoutManager(this)
        binding.RecyclerViewDocuments.itemAnimator = null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun manageDocs(position: Int) {
        if (!binding.textViewNewDocTitle.text.toString().isEmpty()) {
            doc.name = binding.textViewNewDocTitle.text.toString()
        }
        if (intent.extras?.get("Edit") as Boolean) {
            if (intent.extras?.get("Done") as Boolean) {
                DocStorage.doneDocs.value?.set(position, doc)
            } else {
                DocStorage.addDoneDoc(doc)
                DocStorage.removeUndoneDoc(doc)
            }
        } else {
            DocStorage.addDoneDoc(doc)
            DocStorage.removeUndoneDoc(doc)
        }
        DataBaseSync.updloadDocFiles(doc)
        DataBaseSync.addDocument(doc)
    }

    private fun getRecognizerMode(): Flow<Int> {
        return (this.applicationContext as ScannerApplication).dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { pref ->
                val recogMode = pref[SettingsFragment.recognizerModeKey] ?: 1
                recogMode
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 999) {
            val bitmap = if(data?.extras?.get("image") != null) {
                data.extras?.get("image") as Uri
            } else {
                null
            }

            if(bitmap != null) {
                val page = AppPage(bitmap.toString(), doc.pages.size + 1, "")
                doc.addPage(page)

                Log.d("POSITIONAAA", data?.extras?.get("position").toString())
                val position = globPos!!
                if (intent.extras?.get("Edit") as Boolean) {
                    if (intent.extras?.get("Done") as Boolean) {
                        DocStorage.doneDocs.value?.set(position, doc)
                    } else {
                        DocStorage.undoneDocs.value?.set(position, doc)
                    }
                } else {
                    DocStorage.updateUndoneDoc(position, doc)
                }

                binding.RecyclerViewDocuments.adapter = PageAdapter(doc.pages, this, this)
            }
        }
    }
}