package com.drunkenboys.ckscalendar.monthcalendar

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.drunkenboys.ckscalendar.data.CalendarDate
import com.drunkenboys.ckscalendar.data.CalendarDesignObject
import com.drunkenboys.ckscalendar.data.CalendarScheduleObject
import com.drunkenboys.ckscalendar.data.DayType
import com.drunkenboys.ckscalendar.databinding.ItemMonthCellBinding
import com.drunkenboys.ckscalendar.listener.OnDayClickListener
import com.drunkenboys.ckscalendar.listener.OnDaySecondClickListener
import com.drunkenboys.ckscalendar.utils.context
import com.drunkenboys.ckscalendar.utils.dp2px
import com.drunkenboys.ckscalendar.utils.tintStroke

class MonthAdapter(val onDaySelectStateListener: OnDaySelectStateListener) : RecyclerView.Adapter<MonthAdapter.Holder>() {

    private var schedules = listOf<CalendarScheduleObject>()

    private val currentList = mutableListOf<CalendarDate>()

    private val startDayWithMonthFlags = HashMap<Int, String>()

    private lateinit var calendarDesign: CalendarDesignObject

    var selectedPosition = -1
    var currentPagePosition = -1

    var onDayClickListener: OnDayClickListener? = null
    var onDaySecondClickListener: OnDaySecondClickListener? = null

    private val lineIndex = HashMap<String, MonthCellPositionStore>()

    fun setItems(
        list: List<CalendarDate>,
        schedules: List<CalendarScheduleObject>,
        calendarDesign: CalendarDesignObject,
        currentPagePosition: Int
    ) {
        list.forEachIndexed { index, calendarDate ->
            if (calendarDate.isSelected) {
                selectedPosition = index
            }
            if (calendarDate.dayType != DayType.PADDING && startDayWithMonthFlags[calendarDate.date.monthValue] == null) {
                startDayWithMonthFlags[calendarDate.date.monthValue] = "${calendarDate.date}"
            }
        }

        this.lineIndex.clear()
        this.calendarDesign = calendarDesign
        this.currentPagePosition = currentPagePosition
        this.schedules = schedules
        this.currentList.clear()
        this.currentList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val calculateHeight = parent.height / CALENDAR_COLUMN_SIZE
        return Holder(ItemMonthCellBinding.inflate(LayoutInflater.from(parent.context), parent, false), calculateHeight)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int = currentList.size

    inner class Holder(private val binding: ItemMonthCellBinding, private val calculateHeight: Int) :
        RecyclerView.ViewHolder(binding.root) {

        private val weekDayColor = calendarDesign.weekDayTextColor
        private val holidayColor = calendarDesign.holidayTextColor
        private val saturdayColor = calendarDesign.saturdayTextColor

        init {
            itemView.setOnClickListener {
                if (adapterPosition != -1 && currentList[adapterPosition].dayType != DayType.PADDING) {
                    notifyClickEventType()
                    notifyChangedSelectPosition(adapterPosition)
                    onDaySelectStateListener.onDaySelectChange(currentPagePosition, selectedPosition)
                }
            }
            binding.layoutMonthCell.layoutParams.height = calculateHeight
            binding.viewMonthSelectFrame.setBackgroundResource(calendarDesign.selectedFrameDrawable)
            binding.viewMonthSelectFrame.tintStroke(calendarDesign.selectedFrameColor, 2.0f)
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: CalendarDate) {
            binding.layoutMonthCell.isSelected = item.isSelected
            binding.tvMonthDay.text = ""
            if (item.dayType != DayType.PADDING) {
                if (startDayWithMonthFlags[item.date.monthValue] == item.date.toString()) {
                    binding.tvMonthDay.text = "${item.date.monthValue}. ${item.date.dayOfMonth}"
                    binding.tvMonthDay.typeface = Typeface.DEFAULT_BOLD
                } else {
                    binding.tvMonthDay.text = "${item.date.dayOfMonth}"
                    binding.tvMonthDay.typeface = Typeface.DEFAULT
                }
            }
            setDateCellTextDesign(item)

            val scheduleContainer = makePaddingScheduleList(item, schedules)
            val hasAnySchedule = scheduleContainer.any { it != null }
            if (hasAnySchedule) {
                binding.layoutMonthSchedule.removeAllViews()
                scheduleContainer.map { it ?: makeDefaultScheduleTextView() }
                    .forEach {
                        binding.layoutMonthSchedule.addView(it)
                    }
            }
        }

        private fun setDateCellTextDesign(item: CalendarDate) {
            val textColor = when (item.dayType) {
                DayType.HOLIDAY, DayType.SUNDAY -> holidayColor
                DayType.SATURDAY -> saturdayColor
                else -> weekDayColor
            }
            binding.tvMonthDay.setTextColor(textColor)
            binding.tvMonthDay.gravity = calendarDesign.textAlign
            binding.tvMonthDay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, calendarDesign.textSize)
        }

        // make sorted schedule list with white padding
        private fun makePaddingScheduleList(item: CalendarDate, schedules: List<CalendarScheduleObject>): Array<TextView?> {
            val filteredScheduleList = schedules.filter {
                item.dayType != DayType.PADDING &&
                        it.startDate.toLocalDate() <= item.date &&
                        item.date <= it.endDate.toLocalDate()
            }

            val scheduleListContainer = arrayOfNulls<TextView>(SCHEDULE_CONTAINER_SIZE)
            filteredScheduleList.forEach {
                val paddingKey = "${adapterPosition / CALENDAR_COLUMN_SIZE}:${it.id}"
                val paddingLineIndex = lineIndex[paddingKey]?.savedLineIndex
                val lastSelectedPosition = lineIndex[paddingKey]?.lastSelectedPosition
                val isFirstShowSchedule = item.date == it.startDate.toLocalDate() ||
                        item.dayType == DayType.SUNDAY ||
                        adapterPosition == lastSelectedPosition

                if (paddingLineIndex != null) {
                    scheduleListContainer[paddingLineIndex] = mappingScheduleTextView(it, isFirstShowSchedule)
                } else {
                    for (i in scheduleListContainer.indices) {
                        if (scheduleListContainer[i] == null) {
                            scheduleListContainer[i] = mappingScheduleTextView(it, true)
                            lineIndex[paddingKey] = MonthCellPositionStore(i, adapterPosition)
                            break
                        }
                    }
                }
            }
            // 보여줄 갯수만 뽑아서 반환 SCHEDULE_CONTAINER_SIZE 보다 크면 안됨
            return scheduleListContainer.sliceArray(0 until calendarDesign.visibleScheduleCount)
        }

        private fun mappingScheduleTextView(it: CalendarScheduleObject, isFirstShowSchedule: Boolean): TextView {
            val startMargin = if (isFirstShowSchedule) 2f else 0f
            val textView = makeDefaultScheduleTextView(startMargin)
            textView.layoutParams
            textView.text = if (isFirstShowSchedule) it.text else ""
            textView.setTextColor(Color.WHITE)
            textView.setBackgroundColor(it.color)
            return textView
        }

        private fun makeDefaultScheduleTextView(startMargin: Float = 0f): TextView {
            val textView = TextView(itemView.context)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(context().dp2px(startMargin).toInt(), 0, 0, context().dp2px(2.0f).toInt())
            layoutParams.weight = 1f
            textView.includeFontPadding = false
            textView.isSingleLine = true
            textView.layoutParams = layoutParams
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.maxLines = 1
            textView.ellipsize = TextUtils.TruncateAt.END
            val textSize = binding.layoutMonthSchedule.height / calendarDesign.visibleScheduleCount

            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, calendarDesign.textSize)
            textView.setPadding(context().dp2px(2.0f).toInt(), 0, 0, context().dp2px(2.0f).toInt())
            return textView
        }

        private fun notifyChangedSelectPosition(position: Int) {
            val selectedTemp = selectedPosition
            selectedPosition = position

            if (selectedTemp != -1) {
                currentList[selectedTemp].isSelected = false
                notifyItemChanged(selectedTemp)
            }

            currentList[position].isSelected = true
            notifyItemChanged(position)
        }

        private fun notifyClickEventType() {
            if (selectedPosition == adapterPosition) {
                onDaySecondClickListener?.onDayClick(currentList[adapterPosition].date, adapterPosition)
            } else {
                onDayClickListener?.onDayClick(currentList[adapterPosition].date, adapterPosition)
            }
        }
    }

    companion object {

        private val diffUtil = object : DiffUtil.ItemCallback<CalendarDate>() {

            override fun areItemsTheSame(oldItem: CalendarDate, newItem: CalendarDate) = oldItem.date == newItem.date

            override fun areContentsTheSame(oldItem: CalendarDate, newItem: CalendarDate) = oldItem == newItem
        }

        private const val CALENDAR_COLUMN_SIZE = 7

        private const val SCHEDULE_HEIGHT_DIVIDE_RATIO = 10

        //TODO : 10개넘어가는 일정이 있으면 오류 가능성 있음
        private const val SCHEDULE_CONTAINER_SIZE = 10
    }
}
