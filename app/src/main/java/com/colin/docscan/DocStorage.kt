import androidx.lifecycle.MutableLiveData
import com.colin.docscan.AppDocument

object DocStorage {
    val doneDocs = MutableLiveData<MutableList<AppDocument>>()
    val undoneDocs = MutableLiveData<MutableList<AppDocument>>()

    init {
        doneDocs.value = mutableListOf()
        undoneDocs.value = mutableListOf()
    }

    fun addDoneDoc(doc: AppDocument) {
        doneDocs.value?.add(doc)
        doneDocs.value = doneDocs.value
    }

    fun setDoneDocs(docs: MutableList<AppDocument>) {
        doneDocs.value = docs
    }

    fun addUndoneDoc(doc: AppDocument): Int? {
        undoneDocs.value?.add(doc)
        undoneDocs.value = undoneDocs.value
        return undoneDocs.value?.size?.minus(1)
    }

    fun updateUndoneDoc(position: Int, doc: AppDocument) {
        undoneDocs.value?.set(position, doc)
        undoneDocs.value = undoneDocs.value
    }

    fun removeUndoneDoc(doc: AppDocument) {
        undoneDocs.value?.remove(doc)
        undoneDocs.value = undoneDocs.value
    }

    fun removeDoneDoc(doc: AppDocument) {
        doneDocs.value?.remove(doc)
        doneDocs.value = doneDocs.value
    }

    fun updateDoneDoc(position: Int, doc: AppDocument) {
        doneDocs.value?.set(position, doc)
        doneDocs.value = undoneDocs.value
    }
}