package com.bohdan2505.htmlziptoweb

import android.content.Context
import android.os.AsyncTask
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

class UnzipAsyncTask(
    private val context: Context,
    private val zipFile: File,
    private val outputFolder: File,
    private val onProgressUpdate: (Int) -> Unit,
    private val onTaskComplete: () -> Unit
) : AsyncTask<Void, Int, Boolean>() {

    override fun doInBackground(vararg params: Void?): Boolean {
        return try {
            val zipFile = ZipFile(zipFile)
            val zipEntries = zipFile.entries().asSequence().toList()
            val buffer = ByteArray(1024)

            val totalSize = zipEntries.sumBy { it.size.toInt() }
            var extractedBytes = 0

            for ((index, zipEntry) in zipEntries.withIndex()) {
                val entryFile = File(outputFolder, zipEntry.name)

                if (zipEntry.isDirectory) {
                    entryFile.mkdirs()
                } else {
                    val parent = entryFile.parentFile
                    if (!parent.exists()) {
                        parent.mkdirs()
                    }

                    val inputStream = zipFile.getInputStream(zipEntry)
                    val fileOutputStream = FileOutputStream(entryFile)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead)
                        extractedBytes += bytesRead
                    }
                    fileOutputStream.close()
                    inputStream.close()
                }

                // Оновлення загального прогресу
                val currentProgress = ((index + 1).toDouble() / zipEntries.size.toDouble() * 100).toInt()
                publishProgress(currentProgress)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        values[0]?.let { onProgressUpdate.invoke(it) }
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        onTaskComplete.invoke()
    }
}

