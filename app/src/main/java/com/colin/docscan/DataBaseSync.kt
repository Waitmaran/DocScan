@file:Suppress("DEPRECATION")

package com.colin.docscan

import DocStorage
import android.content.Context
import android.icu.text.Transliterator
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.Q)
object DataBaseSync {
    private val db = Firebase.database("https://documentscanner-67991-default-rtdb.asia-southeast1.firebasedatabase.app")
    var userId : String? = null

    var isDocumentListLoading: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var isPageLoading: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    var CYRILLIC_TO_LATIN = "Russian-Latin/BGN"

    var toLatinTrans = Transliterator.getInstance(CYRILLIC_TO_LATIN)

    private const val storageUrl = "gs://documentscanner-67991.appspot.com"

    fun addDocument(doc: AppDocument) {
        if(userId == "offline") {
            return
        }
        val userRootRef = db.getReference("Users/$userId")
        val userDocumentsRef = userRootRef.child("Documents")
        userDocumentsRef.child(toLatinTrans.transliterate(doc.name!!)).setValue(doc)
    }

    fun removeDocument(doc: AppDocument) {
        if(userId == "offline") {
            return
        }
        val userRootRef = db.getReference("Users/$userId")
        val userDocumentsRef = userRootRef.child("Documents")
        userDocumentsRef.child(toLatinTrans.transliterate(doc.name!!)).setValue(null)

        for(page in doc.pages) {
            Log.d("IMAGEDB", page.imageDbUrl!!)
            FirebaseStorage.getInstance().getReferenceFromUrl(page.imageDbUrl!!).delete()
        }
    }

    fun fetchDocuments() {
        if(userId == "offline") {
            return
        }
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
                        Log.d("DB", doc.pages.size.toString())
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
        if(userId == "offline") {
            return
        }
        val ref = FirebaseStorage.getInstance(storageUrl).reference
        for(page in doc.pages) {
            val imgRef = ref.child(userId!!).child(toLatinTrans.transliterate(doc.name!!)).child(page.id.toString()+".jpeg")
            page.imageDbUrl = imgRef.toString()
            imgRef.putFile(page.bitmap?.toUri()!!)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun getPageFile(ref: String, context: Context, page: AppPage) {
        if(userId == "offline") {
            return
        }
        isPageLoading.value = true
        FirebaseStorage.getInstance().getReferenceFromUrl(ref).downloadUrl.addOnSuccessListener {
            Log.d("STORFIRE URI", it.toString())
            GlobalScope.launch(Dispatchers.IO) {
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