package com.bohdan2505.htmlziptoweb

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.zip.ZipInputStream


class FileSystem {
    @SuppressLint("Range")
    fun getFilePathFromUri(uri: Uri, requireContext: Context, ZIP_ARCHIVE_FOLDER_NAME: String): String {
        var filePath = ""
        if (uri.scheme == "content") {
            val cursor = requireContext.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    val appDirectory = this.createAppDirectory(
                        File(requireContext.filesDir, ZIP_ARCHIVE_FOLDER_NAME))

                    filePath = "${appDirectory.absolutePath}/$displayName"
                    val inputStream = requireContext.contentResolver.openInputStream(uri)
                    inputStream?.use { input ->
                        val outputStream = FileOutputStream(filePath)
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        } else if (uri.scheme == "file") {
            filePath = uri.path ?: ""
        }
        return filePath
    }

    @SuppressLint("SetWorldReadable", "SetWorldWritable")
    fun createAppDirectory(appDirectory: File): File {
        if (!appDirectory.exists()) {
            appDirectory.mkdir()
            appDirectory.setReadable(true, false)
            appDirectory.setWritable(true, false)
            appDirectory.setExecutable(true, false)
        }

        return appDirectory
    }

    @SuppressLint("SetWorldWritable", "SetWorldReadable")
    fun copyFileToAppDirectory(sourceFilePath: String, destinationDirectory: File) {
        val sourceFile = File(sourceFilePath)
        val destinationFile = File(destinationDirectory, sourceFile.name)
        destinationFile.setReadable(true, false)
        destinationFile.setWritable(true, false)
        destinationFile.setExecutable(true, false)

        try {
            val sourceChannel = FileInputStream(sourceFile).channel
            val destinationChannel = FileOutputStream(destinationFile).channel
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel)
            sourceChannel.close()
            destinationChannel.close()
        } catch (e: IOException) {
            e.printStackTrace()
            // Обробка помилок копіювання файлу
        }
    }

    fun isValidFolderName(folderName: String): Boolean {
        val regex = """^[a-zA-Z0-9_.\- ]+$""".toRegex()
        val maxLength = 255
        if (!folderName.matches(regex)) {
            return false
        } else if (" " in folderName) {
            return false
        }

        return folderName.length <= maxLength
    }

    fun isFolderExists(folder: File): Boolean {
        return folder.exists() && folder.isDirectory
    }

    @SuppressLint("SetWorldWritable", "SetWorldReadable")
    fun unzip(zipFile: File, outputFolder: File) {
        val buffer = ByteArray(1024)

        try {
            val zipInputStream = ZipInputStream(FileInputStream(zipFile))
            var zipEntry = zipInputStream.nextEntry

            while (zipEntry != null) {
                val entryName = zipEntry.name
                val outputFile = File(outputFolder, entryName)

                if (zipEntry.isDirectory) {
                    outputFile.mkdirs()
                    outputFile.setReadable(true, false)
                    outputFile.setWritable(true, false)
                    outputFile.setExecutable(true, false)
                } else {
                    val outputStream = FileOutputStream(outputFile)
                    var len: Int
                    while (zipInputStream.read(buffer).also { len = it } > 0) {
                        outputStream.write(buffer, 0, len)
                    }
                    outputStream.close()
                }

                zipEntry = zipInputStream.nextEntry
            }

            zipInputStream.closeEntry()
            zipInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearFolder(folder: File) {
        if (folder.exists()) {
            val files = folder.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        clearFolder(file)
                    }
                    file.delete()
                }
            }
        }
    }

    @SuppressLint("SetWorldReadable", "SetWorldWritable")
    fun copyFiles(source: File, destination: File) {
        if (source.isDirectory) {
            if (!destination.exists()) {
                destination.mkdirs()
                destination.setReadable(true, false)
                destination.setWritable(true, false)
                destination.setExecutable(true, false)
            }

            val files = source.listFiles()
            if (files != null) {
                for (file in files) {
                    copyFiles(file, File(destination, file.name))
                }
            }
        } else {
            try {
                source.copyTo(destination)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFile(file: File): Boolean {
        return file.exists() && file.delete()
    }

    fun deleteFolder(folder: File): Boolean {
        if (folder.isDirectory) {
            this.clearFolder(folder)
        }
        folder.delete()
        return ! folder.exists()
    }

    fun renameFolder(oldFolder: File, newFolder: File): Boolean {
        return oldFolder.renameTo(newFolder)
    }

    fun fileExists(file: File): Boolean {
        return file.exists()
    }

    fun readFileContent(file: File): String {
        val contentStringBuilder = StringBuilder()

        try {
            val bufferedReader = BufferedReader(FileReader(file))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                contentStringBuilder.append(line).append("\n")
            }

            bufferedReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return contentStringBuilder.toString()
    }

    @SuppressLint("SetWorldReadable", "SetWorldWritable")
    fun writeToFile(file: File, content: String): Boolean {
        try {
            if (!file.exists()) {
                file.createNewFile()
                file.setReadable(true, false)
                file.setWritable(true, false)
                file.setExecutable(true, false)
            }
            val fileWriter = FileWriter(file)
            fileWriter.write(content)
            fileWriter.close()
        } catch (e: IOException) {
            return false
        }

        return true
    }
}