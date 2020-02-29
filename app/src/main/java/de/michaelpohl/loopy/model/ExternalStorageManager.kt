package de.michaelpohl.loopy.model

import android.content.Context
import android.os.Environment
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFprobe
import de.michaelpohl.loopy.common.AudioModel
import de.michaelpohl.loopy.common.FileHelper
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

    fun listSets(): List<File> {
        return FileHelper.getPathContent(path = appStorageFolder.path, onlyFolders = true)
    }

    fun listSetContents(setFolderName: String): List<AudioModel> {
        val audioModels = mutableListOf<AudioModel>()
        with(FileHelper) {
            getFileModelsFromFiles(getPathContent("${appStorageFolder.path}/$setFolderName")).forEach {
                audioModels.add(fileModelToAudioModel(it))
            }
        }
        return audioModels
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

    fun createSetFolder(folderName: String? = STANDARD_SET_FOLDER_NAME): Boolean {
        val folder = File(
            "$appStorageFolder",
            folderName
        )
        return if (!folder.exists()) {
            folder.mkdirs()
        } else true
    }

    fun copyStandardFilesToSdCard(): Boolean {
        Timber.d("Is external storage available: $isExternalStorageAvailable, read only: $isExternalStorageReadOnly")
        val outputPath = "${appStorageFolder.path}/$STANDARD_SET_FOLDER_NAME/"

        return try {
            listAssetFiles().forEach {
                copySingleFileFromAssetsToStandardSet(outputPath, context.assets.open(it), it)
            }
            convertFilesInPath(outputPath)
            true
        } catch (e: IOException) {
            Timber.e("Copying of files to SD card (Location: ${appStorageFolder.path}/$STANDARD_SET_FOLDER_NAME) failed")
            e.printStackTrace()
            false
        }
    }

    private fun convertFilesInPath(path: String) {
        Timber.d("Converting files in path...")
        File(path).walk().forEach {
            Timber.d("Found: ${it.name}")
            if (it.name.endsWith("mp3")) { //TODO remove hardcoded String with a better structure

                FFprobe.execute("-i $path${it.name}")
                val fileName = "$path${it.name}"
                val command = "-i $path${it.name} ${changeSuffixToWav(fileName)}"
                Timber.d("Executing FFMpeg with: $command")
                val returnCode = FFmpeg.execute(command)
                    Timber.d("Return code: $returnCode")
                when (returnCode) {
                    RETURN_CODE_SUCCESS -> {
                        Timber.d("Command execution completed successfully.")
                    }
                    RETURN_CODE_CANCEL -> {
                        Timber.d("Command execution cancelled by user.")
                    }
                    else -> {
                        Timber.d(
                            String.format(
                                "Command execution failed with rc=%d and the output below.", returnCode
                            )
                        )
                        Config.printLastCommandOutput(Log.INFO)
                    }
                }
            }
        }
    }

    private fun changeSuffixToWav(name: String) : String {
        Timber.d("Change Suffix of: $name")
        return "${name.substringBeforeLast(".")}.wav"
    }

    private fun copySingleFileFromAssetsToStandardSet(outputPath: String, input: InputStream, fileName: String) {
        FileOutputStream(File(outputPath, fileName)).use { out ->
            input.use {
                it.copyTo(out)
            }
            out.close()

        }
    }

    private fun convertToWav(inputPath: String, outputPath: String) {
        Timber.d("Converting to wav...\ninputPath: $inputPath, outPutPath: $outputPath")

        //        val returnCode = FFmpeg.execute("-i $inputPath -c:v $outputPath out")

        //
        //            }
        //        }
    }

    fun listAssetFiles(): Set<String> {
        val list = mutableSetOf<String>()
        try {
            context.assets.list("")?.let { filesList ->
                filesList.filter { FileHelper.isValidAudioFile(it) }.forEach { fileName ->
                    if (FileHelper.isValidAudioFile(fileName)) {
                        Timber.d("Found this file: $fileName")
                        list.add(fileName)
                    }
                }

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return list
    }
}

// tODO make a const file
const val STANDARD_SET_FOLDER_NAME = "standard"
