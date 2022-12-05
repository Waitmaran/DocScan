package com.colin.docscan

import android.view.View

interface DocumentsItemClickListener {
    fun onPositionClicked(position: Int, view: View)
    //fun onViewClicked(view: View)
}