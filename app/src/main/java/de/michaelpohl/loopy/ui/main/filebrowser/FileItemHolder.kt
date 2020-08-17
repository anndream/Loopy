package de.michaelpohl.loopy.ui.main.filebrowser

import android.view.View
import android.widget.TextView
import com.deutschebahn.streckenagent2.ui.common.recycler.DelegationAdapterItemHolder
import de.michaelpohl.loopy.R
import de.michaelpohl.loopy.common.FileModel
import de.michaelpohl.loopy.common.find

class FileItemHolder(itemView: View) : DelegationAdapterItemHolder<FileModel.File>(itemView) {
    override fun bind(item: FileModel.File) {
        (itemView.find(R.id.tv_name) as TextView).setText(item.name + "File")
    }

}