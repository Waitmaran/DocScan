package com.colin.docscan

import com.google.firebase.database.IgnoreExtraProperties

//@IgnoreExtraProperties
class AppDocument(var name: String? = null, var docPages: List<AppPage>? = null) {
     var _pages: MutableList<AppPage> = mutableListOf()



    init {
        if(docPages != null) {
            _pages.addAll(docPages!!)
        }
    }

    val pages: MutableList<AppPage>
        get() = _pages

    fun addPage(page: AppPage) {
        _pages.add(page)
    }

    fun pagesCount(): Int {
        return _pages.size
    }

    fun removePage(position: Int) {
        _pages.removeAt(position)
    }

    fun removePage(page: AppPage) {
        _pages.remove(page)
    }

    fun editPage(position: Int, newPage: AppPage) {
        _pages[position] = newPage
    }
}