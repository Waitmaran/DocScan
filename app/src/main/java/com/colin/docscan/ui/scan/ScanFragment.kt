package com.colin.docscan.ui.scan

import DocStorage
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.colin.docscan.*
import com.colin.docscan.databinding.FragmentScanBinding
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[ScanViewModel::class.java]

        fun getUnsavedDocsJson(): Flow<String> {
            return (context?.applicationContext  as ScannerApplication).dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { pref ->
                    val recogMode = pref[unsavedDocsKey] ?: "null"
                    //Log.d("RECOG SETTINGS", recogMode.toString())
                    recogMode
                }
        }

        _binding = FragmentScanBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.RecyclerViewUndoneDocs.layoutManager = LinearLayoutManager(context)
        binding.RecyclerViewUndoneDocs.itemAnimator = null

        val moshi: Moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(MutableList::class.java, AppDocument::class.java)
        val jsonAdapter: JsonAdapter<List<*>> = moshi.adapter(type)

        GlobalScope.launch {
            val savedUndoneDocs = getUnsavedDocsJson().first()
            if(savedUndoneDocs != "null") {
                val unsavedDocs: List<AppDocument> = jsonAdapter.fromJson(savedUndoneDocs) as List<AppDocument>
                DocStorage.undoneDocs.postValue(unsavedDocs.toMutableList())
            }
        }

        DocStorage.undoneDocs.observe(viewLifecycleOwner) { docs ->

            var json = "null"
            if(docs.size == 0) {
                binding.textViewNoUndoneDocs.visibility = View.VISIBLE
            } else {
                json = jsonAdapter.toJson(docs)
                binding.textViewNoUndoneDocs.visibility = View.GONE
            }
            GlobalScope.launch(Dispatchers.IO) {
                setUnsavedDocsJson(json)
            }

            binding.RecyclerViewUndoneDocs.adapter = DocumentsAdapter(docs, object : DocumentsItemClickListener {
                override fun onPositionClicked(position: Int, view: View) {
                    if(view.id == R.id.imageButtonDeleteDoc) {
                        DocStorage.removeUndoneDoc(DocStorage.undoneDocs.value?.get(position)!!)
                        binding.RecyclerViewUndoneDocs.adapter!!.notifyItemChanged(position)
                    } else {
                        //val position =
                        //    binding.RecyclerViewUndoneDocs.getChildLayoutPosition(clickedView)
                        val editDocIntent = Intent(context, NewDocumentActivity::class.java)
                        editDocIntent.putExtra("Edit", true)
                        editDocIntent.putExtra("Done", false)
                        editDocIntent.putExtra("EditPos", position)
                        startActivity(editDocIntent)
                    }
                }
            })
        }

        binding.floatingActionButton.setOnClickListener {
            val editDoneDocIntent = Intent(context, NewDocumentActivity::class.java)
            editDoneDocIntent.putExtra("Edit", false)
            startActivity(editDoneDocIntent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val unsavedDocsKey = stringPreferencesKey("null")
    }

    suspend fun setUnsavedDocsJson(mode: String) {
        (context?.applicationContext as ScannerApplication).dataStore.edit { pref ->
            pref[unsavedDocsKey] = mode
        }
    }


}