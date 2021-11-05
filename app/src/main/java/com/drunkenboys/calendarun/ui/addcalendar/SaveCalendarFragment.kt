package com.drunkenboys.calendarun.ui.addcalendar

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.drunkenboys.calendarun.R
import com.drunkenboys.calendarun.databinding.FragmentSaveCalendarBinding
import com.drunkenboys.calendarun.showDatePickerDialog
import com.drunkenboys.calendarun.toStringDateFormat
import com.drunkenboys.calendarun.ui.addcalendar.adapter.SaveCalendarRecyclerViewAdapter
import com.drunkenboys.calendarun.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SaveCalendarFragment : BaseFragment<FragmentSaveCalendarBinding>(R.layout.fragment_save_calendar) {

    private val saveCalendarViewModel by viewModels<SaveCalendarViewModel>()
    private val saveCalendarAdapter by lazy { SaveCalendarRecyclerViewAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDataBinding()
        setRecyclerViewAdapter()
        setAddCheckPointViewClickListener()
        setCheckPointListObserver()
        setPickDateTimeEventObserver()
    }

    private fun setDataBinding() {
        binding.saveCalendarViewModel = saveCalendarViewModel
    }

    private fun setRecyclerViewAdapter() {
        binding.rSaveCalendarCheckPointList.adapter = saveCalendarAdapter
    }

    private fun setAddCheckPointViewClickListener() {
        binding.btnSaveCalendarAddCheckPointView.setOnClickListener {
            val newList = emptyList<CheckPointModel>().toMutableList()
            newList.add(CheckPointModel("", ""))
            newList.addAll(saveCalendarAdapter.currentList)
            saveCalendarAdapter.submitList(newList)
        }
    }

    private fun setPickDateTimeEventObserver() {
        saveCalendarViewModel.pickStartDateEvent.observe(viewLifecycleOwner) {
            showDatePickerDialog(requireContext()) { _, year, month, dayOfMonth ->
                saveCalendarViewModel.setCalendarStartDate(getString(R.string.ui_date_format, year, month, dayOfMonth))
            }
        }
        saveCalendarViewModel.pickEndDateEvent.observe(viewLifecycleOwner) {
            showDatePickerDialog(requireContext()) { _, year, month, dayOfMonth ->
                saveCalendarViewModel.setCalendarEndDate(getString(R.string.ui_date_format, year, month, dayOfMonth))
            }
        }

    }

    private fun setCheckPointListObserver() {
        saveCalendarViewModel.checkPointList.observe(viewLifecycleOwner, { checkPointList ->
            val checkPointModelList = mutableListOf<CheckPointModel>()
            checkPointList.forEach { checkPoint ->
                val checkPointModel = CheckPointModel(
                    checkPoint.name,
                    toStringDateFormat(checkPoint.endDate)
                )
                checkPointModelList.add(checkPointModel)
            }
            saveCalendarAdapter.submitList(checkPointModelList)
        })
    }
}
