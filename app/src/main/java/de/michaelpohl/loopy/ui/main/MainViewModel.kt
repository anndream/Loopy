package de.michaelpohl.loopy.ui.main
import android.arch.lifecycle.ViewModel
import android.view.View
import de.michaelpohl.loopy.model.LoopedPlayer
import timber.log.Timber

class MainViewModel : ViewModel() {

    lateinit var looper : LoopedPlayer

    var testLabel = "hohoho"

    fun testMethod(view: View) {
        Timber.d("Hi!")
    }


}
