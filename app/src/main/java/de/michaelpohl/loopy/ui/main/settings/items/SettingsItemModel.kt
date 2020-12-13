package de.michaelpohl.loopy.ui.main.settings.items

import android.content.res.Resources
import de.michaelpohl.loopy.R
import de.michaelpohl.loopy.model.AppStateRepository
import de.michaelpohl.loopy.ui.main.settings.AppSetting
import org.koin.core.KoinComponent
import org.koin.core.inject

sealed class SettingsItemModel : KoinComponent {

    abstract val setting: AppSetting
    abstract fun flip(): SettingsItemModel
    private val resources: Resources by inject()

    open val label = setting.getSettingName()

    data class CheckableSetting(
        override val setting: AppSetting,
        val isChecked: Boolean
    ) : SettingsItemModel(), KoinComponent {

        override fun flip(): CheckableSetting {
            return this.copy(isChecked = !this.isChecked)
        }
    }

    data class FileTypeSetting(
        override val setting: AppSetting,
        val isChecked: Boolean,
        val type: AppStateRepository.Companion.AudioFileType
    ) : SettingsItemModel() {

        override fun flip(): FileTypeSetting {
            return this.copy(isChecked = !this.isChecked)
        }
    }

    data class MultipleChoiceSetting(
        override val setting: AppSetting,
        val choices : Set<SettingsChoice>
    ) : SettingsItemModel() {

        override fun flip(): MultipleChoiceSetting {
            return this
        }
    }

    data class Header(override val label: String) : SettingsItemModel() {

        override val setting: AppSetting = AppSetting.NONE

        override fun flip(): Header {
            /* do nothing */
            return this
        }
    }

    private fun AppSetting.getSettingName(): String {
        return "lalala"
//        return when (this) {
//            AppSetting.WAIT_MODE -> resources.getString(R.string.settings_item_allow_wav)
//            AppSetting.COUNT_LOOPS -> resources.getString(R.string.settings_item_show_loop_count)
//            AppSetting.KEEP_SCREEN_ON -> resources.getString(R.string.settings_item_keep_screen_on)
//            AppSetting.PLAY_IN_BACKGROUND -> resources.getString(R.string.settings_item_play_in_background)
//            AppSetting.SAMPLE_RATE -> resources.getString(R.string.settings_item_sample_rate)
//            AppSetting.FILE_TYPE_MP3 -> resources.getString(R.string.settings_item_allow_mp3)
//            AppSetting.FILE_TYPE_OGG -> resources.getString(R.string.settings_item_allow_ogg)
//            AppSetting.FILE_TYPE_WAV -> resources.getString(R.string.settings_item_allow_wav)
//            else -> ""
//        }
    }
}

typealias SettingsChoice = Pair<String, Boolean>
fun SettingsChoice.name() = this.first
fun SettingsChoice.isChecked() = this.second




