package com.colin.docscan

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DocumentsAdapter(private val documents: MutableList<AppDocument>, private val listener: DocumentsItemClickListener): RecyclerView.Adapter<DocumentsAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View, private val lis: DocumentsItemClickListener) : RecyclerView.ViewHolder(itemView),
        OnClickListener{
        val textViewPageCount: TextView = itemView.findViewById(R.id.textViewDocPageCount)
        val textViewName: TextView = itemView.findViewById(R.id.textViewDocName)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.imageButtonDeleteDoc)

        override fun onClick(p0: View?) {
            lis.onPositionClicked(absoluteAdapterPosition, p0!!)
        }

        init {
            buttonDelete.setOnClickListener(this)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return documents.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentsAdapter.MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.document_done_item, parent, false)

        return DocumentsAdapter.MyViewHolder(itemView, listener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textViewName.text = documents[position].name
        holder.textViewPageCount.text = "Страниц: ${documents[position].pagesCount()}"
    }

}