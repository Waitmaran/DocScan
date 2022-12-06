package com.colin.docscan

import DocStorage
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.colin.docscan.databinding.ActivityNewDocumentBinding
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import kotlin.random.Random


class NewDocumentActivity : AppCompatActivity() {
    var doc = AppDocument("Doc${Random.nextInt()}")
    lateinit var binding: ActivityNewDocumentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val position: Int

        if(intent.extras?.get("Edit") as Boolean) {
            position = intent.extras?.get("EditPos") as Int

            doc = if(intent.extras?.get("Done") as Boolean) {
                DocStorage.doneDocs.value?.get(position)!!
            } else {
                DocStorage.undoneDocs.value?.get(position)!!
            }

            binding.textViewNewDocTitle.setText(doc.name)
            binding.textViewNewDocTitle.isEnabled = false
            binding.RecyclerViewDocuments.adapter = PageAdapter(doc.pages, this, this)
        } else {
            binding.textViewNewDocTitle.hint = doc.name
            position = DocStorage.addUndoneDoc(doc)!!
        }

        binding.buttonNewPage.setOnClickListener {
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
            if(!binding.textViewNewDocTitle.text.toString().isEmpty()) {
                doc.name = binding.textViewNewDocTitle.text.toString()
            }

            if(intent.extras?.get("Edit") as Boolean) {
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

        binding.RecyclerViewDocuments.layoutManager = LinearLayoutManager(this)
        binding.RecyclerViewDocuments.itemAnimator = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 999) {
            val bitmap = data?.extras?.get("image") as Uri

//            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//            val image = InputImage.fromFilePath(this, bitmap)

            val setting = MLLocalTextSetting.Factory()
                .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE) // Specify languages that can be recognized.
                .setLanguage("ru")
                .create()

            val analyzer: MLTextAnalyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting)
            val frame = MLFrame.fromFilePath(this, bitmap)

            val task: Task<MLText> = analyzer.asyncAnalyseFrame(frame)
            task.addOnSuccessListener {
                Toast.makeText(
                    this@NewDocumentActivity,
                    "Text: ${it.stringValue}",
                    Toast.LENGTH_SHORT
                ).show()
            }
                .addOnFailureListener { e ->
                Toast.makeText(
                    this@NewDocumentActivity,
                    "Error: ${e?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }


//            val result = recognizer.process(image)
//                .addOnSuccessListener { visionText ->
//                    Log.d("RECOGNIZER", visionText.text)
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                }


            val page = AppPage(bitmap.toString(), doc.pages.size + 1, "")

            doc.addPage(page)
            binding.RecyclerViewDocuments.adapter = PageAdapter(doc.pages, this, this)
        }
    }
}