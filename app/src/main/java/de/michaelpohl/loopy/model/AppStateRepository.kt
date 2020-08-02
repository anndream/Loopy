package de.michaelpohl.loopy.model

import de.michaelpohl.loopy.common.Settings

class AppStateRepository(val sharedPrefs: SharedPreferencesManager) {

    var settings: Settings
        get() {
            return sharedPrefs.getSettings() ?: createDefaultSettings()
        }
        set(value) {
            sharedPrefs.saveSettings(value)
        }

    var isSetupComplete: Boolean
        get() {
            return sharedPrefs.getBoolean(APP_SETUP_COMPLTE)
        }
        set(value) {
            sharedPrefs.putBoolean(APP_SETUP_COMPLTE, value)
        }

    private fun createDefaultSettings(): Settings {
        settings = Settings(
                acceptedFileTypes = mutableListOf(AudioFileType.WAVE, AudioFileType.MP3),
                isWaitMode = false,
                showLoopCount = true,
                keepScreenOn = true,
                playInBackground = true
            )
        return settings
    }

    companion object {

        private const val PREFS_LOOPY_KEY = "loops_list"
        const val APP_SETUP_COMPLTE = "setup"

        enum class AudioFileType(val suffix: String) {
            WAVE("wav"),
            MP3("mp3"),
            OGG("ogg")
        }
    }
}
