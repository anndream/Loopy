package de.michaelpohl.loopy.model

import android.content.Context
import android.os.Environment
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ExternalStorageManager(val context: Context) {

    private val appStorageFolder: File by lazy {
       context.getExternalFilesDir(null)
    }

    private val isExternalStorageReadOnly: Boolean
        get() {
            val extStorageState = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)
        }
    private val isExternalStorageAvailable: Boolean
        get() {
            val extStorageState = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED.equals(extStorageState)
        }

    fun saveFile(fileToSave: File, path: String): Boolean {
        var externalFile = File(path, fileToSave.name.toString())
        return try {
            val fileOutPutStream = FileOutputStream(externalFile)
            fileOutPutStream.write(fileToSave.readBytes())
            fileOutPutStream.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    //    fun readFile(): File {
    //        var myExternalFile = File(getExternalFilesDir(filepath), fileName.text.toString())
    //
    //        val filename = myExternalFile.name.toString()
    //        myExternalFile = File(getExternalFilesDir(filepath),filename)
    //        if(filename.toString()!=null && filename.toString().trim()!=""){
    //            var fileInputStream = FileInputStream(myExternalFile)
    //            var inputStreamReader = InputStreamReader(fileInputStream)
    //            val bufferedReader = BufferedReader(inputStreamReader)
    //            val stringBuilder: StringBuilder = StringBuilder()
    //            var text: String? = null
    //            while ({ text = bufferedReader.readLine(); text }() != null) {
    //                stringBuilder.append(text)
    //            }
    //            fileInputStream.close()
    //            //Displaying data on EditText
    //            Toast.makeText(applicationContext,stringBuilder.toString(),Toast.LENGTH_SHORT).show()
    //    }
    //    })  }

    fun createAppFolder(): Boolean {
//        val folder = File(Environment.getExternalStorageDirectory())
//        return if (!folder.exists()) {
//            folder.mkdirs()
//        } else true
        return true
    }

    fun createSetFolder(folderName: String? = STANDARD_SET_FOLDER_NAME): Boolean {
        val folder = File(
            "$appStorageFolder",
            folderName
        )
        return if (!folder.exists()) {
            folder.mkdirs()
        } else true
    }

    //TODO change this to handle a number of files
    fun copyStandardFilesToSdCard(input: InputStream, fileName: String) {
        Timber.d("Is external storage available: $isExternalStorageAvailable, read only: $isExternalStorageReadOnly")
        val outputPath = "${appStorageFolder.path}/$STANDARD_SET_FOLDER_NAME/"

        try {
            FileOutputStream(File(outputPath, fileName)).use { out ->
                input.use {
                    it.copyTo(out)
                }
                out.close()
            }
        } catch (e: IOException) {
            Timber.e("Copying of $fileName to SD card (Location: ${appStorageFolder.path}/$STANDARD_SET_FOLDER_NAME$fileName) failed")
            e.printStackTrace()
        }
    }

    companion object {
        const val STANDARD_SET_FOLDER_NAME = "standard"
    }
}
