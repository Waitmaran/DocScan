package com.colin.docscan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

class PageAdapter(private val pages: MutableList<AppPage>, private val context: Context, private val lifecycleOwner: LifecycleOwner): RecyclerView.Adapter<PageAdapter.MyViewHolder>(){

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageView: ImageView = itemView.findViewById(R.id.imageViewItem)
        val textView: TextView = itemView.findViewById(R.id.textViewPageNumber)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.imageButtonDeletePage)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarPage)
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val isExist = try {
            context.contentResolver.openInputStream( pages[position].bitmap?.toUri()!!)?.use {

            }
            true
        }
        catch (e: IOException) {
            false
        }

        if(isExist) {
            holder.imageView.setImageURI(pages[position].bitmap?.toUri())
        } else {
            DataBaseSync.isPageLoading.observe(lifecycleOwner) { isLoading ->
                if(isLoading) {
                    holder.progressBar.visibility = View.VISIBLE
                } else {
                    holder.progressBar.visibility = View.GONE
                    holder.imageView.setImageURI(pages[position].bitmap?.toUri())
                }
            }
            DataBaseSync.getPageFile(pages[position].imageDbUrl!!, context, pages[position])
            //notifyItemChanged(position)
//Я В примоченко двгупс
            //pages[position].bitmap = newUri
            //holder.imageView.setImageURI(Uri.parse(newUri))
        }


        holder.textView.text = "Страница №${position}"
        holder.buttonDelete.setOnClickListener {
            pages.removeAt(position)
            notifyItemChanged(position)
        }
        holder.imageView.setOnClickListener {
            val showImageIntent = Intent(context, ImagePresentationActivity::class.java)
            showImageIntent.putExtra("image", pages[position].bitmap?.toUri())
            context.startActivity(showImageIntent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.document_item, parent, false)
        return MyViewHolder(itemView)
    }
}