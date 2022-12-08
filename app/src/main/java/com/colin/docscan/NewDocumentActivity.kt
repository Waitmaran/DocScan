package com.colin.docscan

import DocStorage
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.colin.docscan.databinding.ActivityNewDocumentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random


class NewDocumentActivity : AppCompatActivity() {
    var doc = AppDocument("Doc${Random.nextInt()}")
    lateinit var binding: ActivityNewDocumentBinding
    lateinit var pageAdapter: PageAdapter
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val position: Int
        pageAdapter = PageAdapter(doc.pages, this, this)

        if(intent.extras?.get("Edit") as Boolean) {
            position = intent.extras?.get("EditPos") as Int

            doc = if(intent.extras?.get("Done") as Boolean) {
                DocStorage.doneDocs.value?.get(position)!!
            } else {
                DocStorage.undoneDocs.value?.get(position)!!
            }

            binding.textViewNewDocTitle.setText(doc.name)
            binding.textViewNewDocTitle.isEnabled = false
            Log.d("DB FAKE", doc.pages.size.toString())
            binding.RecyclerViewDocuments.adapter = PageAdapter(doc.pages, this, this)
        } else {
            binding.textViewNewDocTitle.hint = doc.name
            position = DocStorage.addUndoneDoc(doc)!!
        }

        binding.switchRecognition.setOnClickListener {

        }

        binding.switchTranslate.setOnClickListener {

        }

        binding.floatingActionButtonShotPage.setOnClickListener {
            startActivityForResult(Intent(applicationContext, NewScanActivity::class.java), 999)
            if(intent.extras?.get("Edit") as Boolean) {
                if(intent.extras?.get("Done") as Boolean) {
                    DocStorage.doneDocs.value?.set(position, doc)
                } else {
                    DocStorage.undoneDocs.value?.set(position, doc)
                }
            } else {
                DocStorage.updateUndoneDoc(position, doc)
            }
        }

        binding.buttonDocDone.setOnClickListener {
            binding.buttonDocDone.isClickable = false
            var count = 1

            OcrHelper.progress.observe(this) { progress ->
                binding.buttonDocDone.text = "Обработка документа: ${count}/${doc.pages.size} (${progress})"
            }

            GlobalScope.launch( Dispatchers.IO ) {
                for (page in doc.pages) {
                    OcrHelper.Init(cacheDir)
                    val text = OcrHelper.recognize(Uri.parse(page.bitmap), this@NewDocumentActivity)
                    page.text = text
                    count++
                    Log.d("RECOG", "COMPLETE")
                }
            }.invokeOnCompletion {
                GlobalScope.launch(Dispatchers.Main) {
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
                    finish()
                }
            }
        }

        binding.RecyclerViewDocuments.layoutManager = LinearLayoutManager(this)
        binding.RecyclerViewDocuments.itemAnimator = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 999) {
            val bitmap = data?.extras?.get("image") as Uri

            val page = AppPage(bitmap.toString(), doc.pages.size + 1, "")
            doc.addPage(page)
            binding.RecyclerViewDocuments.adapter = PageAdapter(doc.pages, this, this)
        }
    }
}