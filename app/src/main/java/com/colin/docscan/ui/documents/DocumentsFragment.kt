package com.colin.docscan.ui.documents

import DocStorage
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.colin.docscan.*
import com.colin.docscan.databinding.FragmentDocumentsBinding

class DocumentsFragment : Fragment() {

    private var _binding: FragmentDocumentsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this)[DocumentsViewModel::class.java]

        _binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.RecyclerViewDoneDocuments.layoutManager = LinearLayoutManager(context)
        binding.RecyclerViewDoneDocuments.itemAnimator = null

        DocStorage.doneDocs.observe(viewLifecycleOwner) { docs ->
            binding.RecyclerViewDoneDocuments.adapter = DocStorage.doneDocs.value?.let { it ->
                DocumentsAdapter(
                    it, object: DocumentsItemClickListener {
                        override fun onPositionClicked(position: Int, view: View) {
                            if(view.id == R.id.imageButtonDeleteDoc) {
                                DataBaseSync.removeDocument(DocStorage.doneDocs.value?.get(position)!!)
                                DocStorage.removeDoneDoc(DocStorage.doneDocs.value?.get(position)!!)
                                binding.RecyclerViewDoneDocuments.adapter!!.notifyItemChanged(position)
                            } else if(view.id == R.id.imageButtonSavePdf) {
                                DocStorage.savePdf(
                                    DocStorage.doneDocs.value?.get(position)!!,
                                    requireActivity().contentResolver,
                                    requireContext())
                            }
                            else {
                                //val position = binding.RecyclerViewDoneDocuments.getChildLayoutPosition(view)
                                val editDocIntent = Intent(context, NewDocumentActivity::class.java)
                                editDocIntent.putExtra("Edit", true)
                                editDocIntent.putExtra("Done", true)
                                editDocIntent.putExtra("EditPos", position)
                                startActivity(editDocIntent)
                            }
                        }
                    }
                )

            }
//            if(docs.size == 0) {
//                binding.textViewNoDoneDocs.visibility = View.VISIBLE
//            } else {
//                binding.textViewNoDoneDocs.visibility = View.GONE
//            }
           // Log.d("ABOBA", "ABOBA")
//            DocStorage.setDoneDocs(documents)
        }

        DataBaseSync.isDocumentListLoading.observe(viewLifecycleOwner) { isLoading ->
            if(isLoading) {
                binding.progressBarLoading.visibility = View.VISIBLE
            } else {
                binding.progressBarLoading.visibility = View.GONE
                if(DocStorage.doneDocs.value?.size == 0) {
                    binding.textViewNoDoneDocs.visibility = View.VISIBLE
                }
            }
        }

        //binding.progressBarLoading.visibility = View.VISIBLE
        if((activity as MainActivity).token == "offline") {
            binding.textViewNoDoneDocs.text = "В оффлайн режиме невозможно загрузить данные, сохраненные в облаке!"
            binding.textViewNoDoneDocs.visibility = View.VISIBLE
        } else {
            DataBaseSync.fetchDocuments()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}