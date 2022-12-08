package com.colin.docscan

class AppDocument(var name: String? = null, var docPages: List<AppPage>? = null) {
    private var _pages: MutableList<AppPage> = mutableListOf()

    init {
        if(docPages != null) {
            _pages.addAll(docPages!!)
        }
    }

    var pages: MutableList<AppPage>
        get() = _pages
        set(value) {
            _pages = value
        }

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