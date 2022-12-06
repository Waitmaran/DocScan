package com.colin.docscan

import DocStorage
import android.content.ContentResolver
import android.content.Context
import android.graphics.drawable.Drawable
import android.icu.text.Transliterator.Position
import android.provider.MediaStore
import android.provider.Settings.Global
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.Target
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

object DataBaseSync {
    val db = Firebase.database("https://documentscanner-67991-default-rtdb.asia-southeast1.firebasedatabase.app")
    var userId : String? = null

    var isDocumentListLoading: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var isPageLoading: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    const val storageUrl = "gs://documentscanner-67991.appspot.com"

    fun addDocument(doc: AppDocument) {
        val userRootRef = db.getReference("Users/$userId")
        val userDocumentsRef = userRootRef.child("Documents")
        userDocumentsRef.child(doc.name!!).setValue(doc)
    }

    fun removeDocument(doc: AppDocument) {
        val userRootRef = db.getReference("Users/$userId")
        val userDocumentsRef = userRootRef.child("Documents")
        userDocumentsRef.child(doc.name!!).setValue(null)
    }

    fun fetchDocuments() {
        isDocumentListLoading.value = true
        val userRootRef = db.getReference("Users/$userId/Documents")
        val documentsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val documentsList: MutableList<AppDocument> = mutableListOf()
                for (ds in dataSnapshot.children) {
                    val doc: AppDocument? = ds.getValue(AppDocument::class.java)
                    if (doc != null) {
                        doc.name?.let { Log.d("DB", it) }
                        documentsList.add(doc)
                    }
                }
                DocStorage.doneDocs.value = documentsList
                isDocumentListLoading.value = false
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("DATABASE", "loadPost:onCancelled", databaseError.toException())
            }
        }
        userRootRef.addValueEventListener(documentsListener as ValueEventListener)
    }

    fun updloadDocFiles(doc: AppDocument) {
        val ref = FirebaseStorage.getInstance(storageUrl).reference
        for(page in doc.pages) {
            val imgRef = ref.child(userId!!).child(doc.name!!).child(page.id.toString()+".jpeg")
            page.imageDbUrl = imgRef.toString()
            imgRef.putFile(page.bitmap?.toUri()!!)
        }
    }

    private fun deleteDocFiles() {

    }

    fun getPageFile(ref: String, context: Context, page: AppPage) {
        isPageLoading.value = true
        FirebaseStorage.getInstance().getReferenceFromUrl(ref).downloadUrl.addOnSuccessListener {
            Log.d("STORFIRE URI", it.toString())
           val job = GlobalScope.launch(Dispatchers.IO) {
                val bitmap = Glide.with(context).asBitmap().load(it).timeout(60000).submit().get()
                val path: String = MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    bitmap,
                    "Title",
                    null
                )
                page.bitmap = path
                isPageLoading.postValue(false)
                Log.d("STORFIRE", it.toString())
            }
        }
    }

}