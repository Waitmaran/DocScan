package com.colin.docscan.ui.scan

import DocStorage
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.colin.docscan.DocumentsAdapter
import com.colin.docscan.DocumentsItemClickListener
import com.colin.docscan.NewDocumentActivity
import com.colin.docscan.R
import com.colin.docscan.databinding.FragmentScanBinding
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

        _binding = FragmentScanBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.RecyclerViewUndoneDocs.layoutManager = LinearLayoutManager(context)
        binding.RecyclerViewUndoneDocs.itemAnimator = null

        DocStorage.undoneDocs.observe(viewLifecycleOwner) { docs ->
            if(docs.size == 0) {
                binding.textViewNoUndoneDocs.visibility = View.VISIBLE
            } else {
                binding.textViewNoUndoneDocs.visibility = View.GONE
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

        binding.imageButtonScanCamera.setOnClickListener {
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
}