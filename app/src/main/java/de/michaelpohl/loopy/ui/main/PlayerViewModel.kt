package de.michaelpohl.loopy.ui.main
import android.app.Application
import android.databinding.ObservableField
import android.view.View
import de.michaelpohl.loopy.R
import de.michaelpohl.loopy.model.LoopedPlayer
import timber.log.Timber

class PlayerViewModel(application: Application) : BaseViewModel(application) {

    var looper : LoopedPlayer =LoopedPlayer.create(application, R.raw.loop)

    var testLabel= ObservableField<String>("hohoho")

    fun onClick(view: View) {
        Timber.d("Hi!")
        testLabel.set("hahaha")
        looper.start()

    }

    fun onClick2(view: View) {
        testLabel.set("Hohoho")
        looper.stop()
    }


}