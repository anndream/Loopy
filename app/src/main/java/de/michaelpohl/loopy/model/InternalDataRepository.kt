package de.michaelpohl.loopy.model

import android.content.res.AssetManager
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class InternalDataRepository(
    private val sharedPrefsManager: SharedPreferencesManager,
    private val storage: ExternalStorageManager
) : KoinComponent {

    private val assets: AssetManager by inject()

    fun getSavedSets(): Sets {
        return sharedPrefsManager.loadLoopSets()
    }

    fun saveSet(set: LoopSet) {
        sharedPrefsManager.saveSingleSet(set)
    }

    fun getSingleSet(): LoopSet {
        //TODO fill with real functionality
        return LoopSet("", listOf())
    }

    fun autoCreateFirstLoopSet() {
        val wasFolderCreated = storage.createAppFolder() && storage.createSetFolder()
        Timber.d("Was the folder created? $wasFolderCreated")
        val fileName = "testing.mp3"
        storage.copyToSdCard(assets.open(fileName), fileName)
    }
}