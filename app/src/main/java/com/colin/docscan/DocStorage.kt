import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.colin.docscan.AppDocument
import com.colin.docscan.DataBaseSync
import com.itextpdf.text.Chunk
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import java.io.File


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

    fun updateUndoneDocPost(position: Int, doc: AppDocument) {
        undoneDocs.value?.set(position, doc)
        undoneDocs.postValue(undoneDocs.value)
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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun savePdf(doc: AppDocument, contentResolver: ContentResolver, context: Context) {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, doc.name)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + File.separator + "DocScan")
        val uri: Uri? = contentResolver.insert(MediaStore.Files.getContentUri("external"), values)

        if (uri != null) {
            val outputStream = contentResolver.openOutputStream(uri)
            val document = com.itextpdf.text.Document()
            PdfWriter.getInstance(document, outputStream)
            document.open()
            document.addAuthor(DataBaseSync.userId)
            addDataIntoPDF(document, doc)
            document.close()
            Toast.makeText(context, "Успешный экспорт в PDF", Toast.LENGTH_SHORT).show()
        }

    }

    private fun addDataIntoPDF(document: com.itextpdf.text.Document, doc: AppDocument) {
        val myFont = "/assets/fonts/timesnewromanpsmt.ttf";
        val bf = BaseFont.createFont(myFont, BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
        val textFont = Font(bf, 14f, Font.NORMAL)
        val textFontBold = Font(bf, 18f, Font.BOLD)

        val paragraph = Paragraph()
        val headingFont = Font(Font.FontFamily.HELVETICA, 24F, Font.BOLD)
        paragraph.add(Paragraph(doc.name, headingFont))
        for(page in doc.pages) {
            paragraph.add(Paragraph("Текст: ", textFontBold))
            paragraph.add(Paragraph(page.text, textFont))
            paragraph.add(Chunk.NEWLINE)
            paragraph.add(Paragraph("Перевод: ", textFontBold))
            paragraph.add(Paragraph(page.translatedText, textFont))
            paragraph.add(Chunk.NEXTPAGE)
        }
        document.add(paragraph)
    }
}