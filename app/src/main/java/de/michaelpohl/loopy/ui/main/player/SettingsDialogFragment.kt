package de.michaelpohl.loopy.ui.main.player

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.michaelpohl.loopy.R
import de.michaelpohl.loopy.common.Settings
import de.michaelpohl.loopy.common.SwitchingLoopsBehaviour.SWITCH
import de.michaelpohl.loopy.common.SwitchingLoopsBehaviour.WAIT
import de.michaelpohl.loopy.common.ValidAudioFileType
import kotlinx.android.synthetic.main.dialog_settings.*
import kotlinx.android.synthetic.main.dialog_settings.view.*
import timber.log.Timber

class SettingsDialogFragment : DialogFragment() {

    private lateinit var settings: Settings
    private lateinit var allowedFileTypes: MutableList<ValidAudioFileType>
    private var showLoopCount: Boolean = false

    lateinit var resultListener: (Settings) -> Unit

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View = inflater.inflate(R.layout.dialog_settings, container, false)

        view.rb_switch_immediately.isChecked = settings.switchingLoopsBehaviour == SWITCH
        view.rb_wait_until_finished.isChecked = settings.switchingLoopsBehaviour == WAIT


        view.cb_check_wav.isChecked = allowedFileTypes.contains(ValidAudioFileType.WAVE)
        view.cb_check_mp3.isChecked = allowedFileTypes.contains(ValidAudioFileType.MP3)
        view.cb_check_ogg.isChecked = allowedFileTypes.contains(ValidAudioFileType.OGG)

        view.cb_check_show_loop_count.isChecked = showLoopCount
        view.cb_check_keep_screen_on.isChecked = settings.keepScreenOn
        view.cb_check_play_in_background.isChecked = settings.playInBackground

        view.rb_switch_immediately.setOnClickListener { onSwitchImmediatelyClicked() }
        view.rb_wait_until_finished.setOnClickListener { onWaitUntilFinishedClicked() }

        view.cb_check_wav.setOnClickListener { onCheckBoxWavClicked() }
        view.cb_check_mp3.setOnClickListener { onCheckBoxMp3Clicked() }
        view.cb_check_ogg.setOnClickListener { onCheckBoxOggClicked() }

        view.cb_check_show_loop_count.setOnClickListener { onToggleShowLoopCountCLicked() }
        view.cb_check_keep_screen_on.setOnClickListener { onToggleKeepScreenOnClicked() }
        view.cb_check_play_in_background.setOnClickListener { onTogglePlayInBackgroundClicked() }

        view.btn_ok.setOnClickListener { onOkClicked() }
        view.btn_cancel.setOnClickListener { onCancelClicked() }

        return view
    }

    //TODO whe don't need local variables here we can just use the settings object and avoid some confusion
    fun setCurrentSettings(currentSettings: Settings) {
        this.settings = currentSettings
        this.allowedFileTypes = currentSettings.allowedFileTypes.toMutableList()
        this.showLoopCount = settings.showLoopCount
    }

    private fun onCheckBoxWavClicked() {
        val fileType = ValidAudioFileType.WAVE
        if (cb_check_wav.isChecked) {
            allow(fileType)
        } else {
            forbid(fileType)
        }
    }

    private fun onCheckBoxMp3Clicked() {
        val fileType = ValidAudioFileType.MP3
        if (cb_check_mp3.isChecked) {
            allow(fileType)
        } else {
            forbid(fileType)
        }
    }

    private fun onCheckBoxOggClicked() {
        val fileType = ValidAudioFileType.OGG

        if (cb_check_ogg.isChecked) {
            allow(fileType)
        } else {
            forbid(fileType)
        }
    }

    private fun onSwitchImmediatelyClicked() {
        settings.switchingLoopsBehaviour = SWITCH
        view?.rb_switch_immediately?.isChecked = true
        view?.rb_wait_until_finished?.isChecked = false
    }

    private fun onWaitUntilFinishedClicked() {
        settings.switchingLoopsBehaviour = WAIT
        view?.rb_switch_immediately?.isChecked = false
        view?.rb_wait_until_finished?.isChecked = true
    }

    private fun onToggleShowLoopCountCLicked() {
        showLoopCount = !showLoopCount

        view?.cb_check_show_loop_count?.isChecked = showLoopCount //does this need error handling?
    }

    private fun onToggleKeepScreenOnClicked() {
        settings.keepScreenOn = !settings.keepScreenOn
    }

    private fun onTogglePlayInBackgroundClicked() {
        settings.playInBackground = ! settings.playInBackground
    }

    private fun onOkClicked() {
        settings.allowedFileTypes = allowedFileTypes.toTypedArray()
        settings.showLoopCount = showLoopCount
        resultListener.invoke(settings)
        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }


    private fun allow(fileType: ValidAudioFileType) {
        if (!allowedFileTypes.contains(fileType)) {
            allowedFileTypes.add(fileType)
        }
    }

    private fun forbid(fileType: ValidAudioFileType) {
        if (allowedFileTypes.contains(fileType)) {
            allowedFileTypes.remove(fileType)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("invoking result listener")
        resultListener.invoke(settings)
    }
}
