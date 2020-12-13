package de.michaelpohl.loopy.ui.main.settings.items

import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.example.adapter.adapter.DelegationAdapterItemHolder
import de.michaelpohl.loopy.R
import de.michaelpohl.loopy.common.find
import de.michaelpohl.loopy.common.gone

class SettingsMultipleChoiceViewHolder(itemView: View) :
    DelegationAdapterItemHolder<SettingsItemModel.MultipleChoiceSetting>(itemView) {

    private lateinit var firstLabel: TextView
    private lateinit var secondLabel: TextView
    private lateinit var thirdLabel: TextView
    private lateinit var firstRadioButton: RadioButton
    private lateinit var secondRadioButton: RadioButton
    private lateinit var thirdRadioButton: RadioButton
    override fun onCreated() {
        super.onCreated()
        firstLabel = itemView.find(R.id.tv_label_first)
        secondLabel = itemView.find(R.id.tv_label_second)
        thirdLabel = itemView.find(R.id.tv_label_third)
        firstRadioButton = itemView.find(R.id.rb_radio_first)
        secondRadioButton = itemView.find(R.id.rb_radio_second)
        thirdRadioButton = itemView.find(R.id.rb_radio_third)
    }

    //    TODO!!! This should resolve the number of buttons and their labels dynamically!
    override fun bind(item: SettingsItemModel.MultipleChoiceSetting) {
        val choices = item.choices.toList()
        firstLabel.text = choices[0].name()
        secondLabel.text = choices[1].name()

        firstRadioButton.isChecked = choices[0].isChecked()
        secondRadioButton.isChecked = choices[1].isChecked()

        // TODO this is really bad code
        if (choices.size == 3) {
        thirdRadioButton.isChecked = choices[2].isChecked()
        thirdLabel.text = choices[2].name()
        } else {
            thirdRadioButton.gone()
            thirdLabel.gone()
        }
    }
}
