package de.michaelpohl.loopy

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import de.michaelpohl.loopy.common.AppData
import de.michaelpohl.loopy.common.FileModel
import de.michaelpohl.loopy.common.FileType
import de.michaelpohl.loopy.common.Settings
import de.michaelpohl.loopy.model.LoopsRepository
import de.michaelpohl.loopy.ui.main.BaseFragment
import de.michaelpohl.loopy.ui.main.browser.FileBrowserFragment
import de.michaelpohl.loopy.ui.main.browser.FileBrowserViewModel
import de.michaelpohl.loopy.ui.main.player.PlayerFragment
import de.michaelpohl.loopy.ui.main.player.PlayerViewModel
import hugo.weaving.DebugLog
import kotlinx.android.synthetic.main.main_activity.*
import timber.log.Timber

@DebugLog
class MainActivity : AppCompatActivity(), FileBrowserViewModel.OnItemClickListener,
    PlayerViewModel.PlayerActionsListener {

    private val defaultFilesPath = Environment.getExternalStorageDirectory().toString()
    private var menuResourceID = R.menu.menu_main
    private lateinit var currentFragment: BaseFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Timber logging on when Debugging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        LoopsRepository.init(
            getSharedPreferences(
                resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )
        )

        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            addPlayerFragment(LoopsRepository.currentSelectedFileModels)
        }
        setSupportActionBar(findViewById(R.id.my_toolbar))
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_help -> {
            //TODO show help fragment
            true
        }

        R.id.action_gear -> {
            //handled in PlayerFragment
            false
        }

        R.id.action_browser -> {
            //handled in PlayerFragment
            false
        }

        R.id.action_submit -> {
            // context for this action is the FileBrowserFragment, but we handle it here because we need activity methods
            val didUpdate = LoopsRepository.updateAndSaveFileSelection()
            if (didUpdate) {
                Timber.d("We did update")
                clearBackStack()
                addPlayerFragment(LoopsRepository.currentSelectedFileModels, LoopsRepository.settings)
                true
            } else {
                val snackbar = Snackbar.make(
                    container,
                    getString(R.string.snackbar_text_no_new_files_selected),
                    Snackbar.LENGTH_LONG
                )
                snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.action))
                snackbar.show()
                false
            }
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(menuResourceID, menu)
        return true
    }

    override fun onFolderClicked(fileModel: FileModel) {
        if (fileModel.fileType == FileType.FOLDER) {
            addFileFragment(fileModel.path)
        }
    }

    override fun onOpenFileBrowserClicked() {
        addFileFragment()
    }

    override fun onBackPressed() {

        //apparently it is possible to come by here with currentFragment not being initialized
        if (::currentFragment.isInitialized && !currentFragment.onBackPressed()) {
            super.onBackPressed()
        }

        if (supportFragmentManager.backStackEntryCount == 0) {
//            finish()
        }
    }

    private fun addPlayerFragment(loops: List<FileModel> = emptyList(), settings: Settings = Settings()) {
        for (model in loops) Timber.d("what we get in MainActivity: %s", model.name)

        //TODO this method can be better - handling what's in AppData should completely move into LoopsRepository
        val appData = AppData(loops, settings)
        val playerFragment = PlayerFragment.newInstance(appData)
        currentFragment = playerFragment
        playerFragment.changeActionBarLayoutCallBack = { it -> changeActionBar(it) }
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, playerFragment, "player")
            .commit()
    }

    private fun addFileFragment(path: String = defaultFilesPath) {
        val filesListFragment = FileBrowserFragment.newInstance(path)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        currentFragment = filesListFragment
        fragmentTransaction.replace(R.id.container, filesListFragment)
        fragmentTransaction.addToBackStack(path)
        fragmentTransaction.commit()
        changeActionBar(R.menu.menu_browser)
    }

    private fun clearBackStack() {
        while (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        }
    }

    private fun changeActionBar(resourceID: Int) {
        menuResourceID = resourceID
        invalidateOptionsMenu()
    }
}

