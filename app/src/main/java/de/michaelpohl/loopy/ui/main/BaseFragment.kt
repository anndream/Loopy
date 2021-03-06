package de.michaelpohl.loopy.ui.main

import android.support.v4.app.Fragment
import de.michaelpohl.loopy.R
import kotlinx.android.synthetic.*

open class BaseFragment : Fragment() {


    override fun onResume() {
        super.onResume()
        activity?.title = getTitle()
    }

    /**
     *do something when the back button is pressed
     *Override if interested
     *Return true to prevent activity from executing its onBackPressed
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    open fun getTitle(): String {
        return getString(R.string.appbar_title_player)
    }

    /**
     * Clears the findViewById cache when the view is destroyed to make sure we only ever adress valid views
     */
    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }
}