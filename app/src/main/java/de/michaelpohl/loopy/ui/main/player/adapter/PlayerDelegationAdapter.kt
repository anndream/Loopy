package de.michaelpohl.loopy.ui.main.player.adapter

import com.example.adapter.adapter.AnyDiffCallback
import com.example.adapter.adapter.DelegationAdapter
import de.michaelpohl.loopy.common.AudioModel

// TODO refactor with custom Sorting
class PlayerDelegationAdapter(private val delegate: PlayerItemDelegate) :
    DelegationAdapter<AudioModel>(
        AnyDiffCallback(), PlayerItemSorting(),
        listOf(delegate)
    ) {
    fun updateFileCurrentlyPlayed(name: String) {
        delegate.updateFileCurrentlyPlayed(name)
    }

    fun updateFilePreselected(name: String) {
        delegate.updateFilePreselected(name)
    }

    fun updatePlaybackProgress(payload: Pair<String, Int>) {
        delegate.updatePlaybackProgress(payload)
    }

    companion object {
        enum class SelectionState { NOT_SELECTED, PRESELECTED, PLAYING }
    }
}