package com.drunkenboys.calendarun.ui.addcalendar.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.drunkenboys.calendarun.BR
import com.drunkenboys.calendarun.R
import com.drunkenboys.calendarun.databinding.ItemCheckPointBinding
import com.drunkenboys.calendarun.showDatePickerDialog
import com.drunkenboys.calendarun.ui.addcalendar.CheckPointModel
import com.drunkenboys.calendarun.ui.base.BaseViewHolder
import com.drunkenboys.calendarun.util.context

class SaveCalendarRecyclerViewAdapter :
    ListAdapter<CheckPointModel, BaseViewHolder<ItemCheckPointBinding>>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ItemCheckPointBinding> {
        return BaseViewHolder<ItemCheckPointBinding>(parent, R.layout.item_check_point).apply {
            binding.tvCheckPointDatePicker.setOnClickListener {
                setCheckPointDate(context(), adapterPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ItemCheckPointBinding>, position: Int) {
        holder.binding.setVariable(BR.item, currentList[position])
        holder.binding.item = currentList[position]
    }

    private fun setCheckPointDate(context: Context, position: Int) {
        showDatePickerDialog(context) { _, year, month, dayOfMonth ->
            val currentItem = currentList[position]
            val newList = currentList.toMutableList()
            val date = context.getString(R.string.ui_date_format, year, month, dayOfMonth)
            val newCheckPointModel = CheckPointModel(currentItem.name, date, currentItem.check)
            newList[position] = newCheckPointModel
            submitList(newList)
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<CheckPointModel>() {
            override fun areItemsTheSame(oldItem: CheckPointModel, newItem: CheckPointModel) =
                oldItem.date == newItem.date

            override fun areContentsTheSame(oldItem: CheckPointModel, newItem: CheckPointModel) =
                oldItem == newItem
        }
    }
}
