package de.michaelpohl.loopy.ui.main.player

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.michaelpohl.loopy.R
import de.michaelpohl.loopy.common.FileHelper
import de.michaelpohl.loopy.common.FileModel
import hugo.weaving.DebugLog
import kotlinx.android.synthetic.main.item_loop.view.*
import rm.com.audiowave.AudioWaveView
import timber.log.Timber


class LoopsAdapter(var context: Context) : RecyclerView.Adapter<LoopsAdapter.ViewHolder>() {


    private var loopsList = listOf<FileModel>()
    var onItemClickListener: ((FileModel, Int) -> Unit)? = null
    var onProgressUpdatedListener: ((Float) -> Unit)? = null

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loop, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = loopsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(position)

        if (holder.positionInList == selectedPosition) {
            Timber.d("Selected item! position in List: %s", holder.positionInList)
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.action))
            holder.selected = true
            holder.initializeOnProgressUpdatedListener()
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.content_background))
            Timber.d("Not selected item! position in List: %s", holder.positionInList)
            holder.selected = false
        }
        Timber.d("selectedPosition: %s", selectedPosition)
    }

    fun updateData(newList: List<FileModel>) {
        setLoopsList(newList)
        notifyDataSetChanged()
    }

    fun updateProgress(position: Float) {
        onProgressUpdatedListener?.invoke(position)
    }

    fun resetProgress() {
        onProgressUpdatedListener?.invoke(0F)
    }

    private fun setLoopsList(newList: List<FileModel>) {
        loopsList = newList.sortedWith(compareBy { it.name.toLowerCase() }).filter { it.isValidFileType() }
Timber.d("Adapter updating with these loops: ")
        loopsList.forEach{ Timber.d("%s", it)}
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener,
        View.OnLongClickListener {

        var selected = false
        var positionInList = -1

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        private fun update(progress: Float) {

            if (!selected) {
                Timber.d("not selected, position: %s", positionInList)
                itemView.wave.progress = 0F
                return
            }
            itemView.wave.progress = progress
        }

        fun initializeOnProgressUpdatedListener() {
            onProgressUpdatedListener = { it -> update(it) }
        }

        override fun onClick(v: View?) {
            onItemClickListener?.invoke(loopsList[adapterPosition], positionInList)
        }

        override fun onLongClick(v: View?): Boolean {
//            no longClicklistener needed at this point
//            onItemSelectedListener?.invoke(filesList[adapterPosition])
            //TODO show file details on long click
            return true
        }

        fun bindView(position: Int) {
            val fileModel = loopsList[position]
            val bytes = FileHelper.getSingleFile(fileModel.path).readBytes()
            positionInList = position
            itemView.tv_name.text = fileModel.name
            inflateWave(itemView.wave, bytes)
        }

        private fun inflateWave(view: AudioWaveView, bytes: ByteArray) {
            view.setRawData(bytes)
        }
    }
}
