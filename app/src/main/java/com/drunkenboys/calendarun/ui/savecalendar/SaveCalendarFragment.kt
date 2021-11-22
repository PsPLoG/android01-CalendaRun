package com.drunkenboys.calendarun.ui.savecalendar

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import com.drunkenboys.calendarun.R
import com.drunkenboys.calendarun.databinding.FragmentSaveCalendarBinding
import com.drunkenboys.calendarun.ui.base.BaseFragment
import com.drunkenboys.calendarun.ui.savecalendar.model.CheckPointItem
import com.drunkenboys.calendarun.ui.saveschedule.model.DateType
import com.drunkenboys.calendarun.util.extensions.pickDateInMillis
import com.drunkenboys.calendarun.util.extensions.sharedCollect
import com.drunkenboys.calendarun.util.extensions.stateCollect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class SaveCalendarFragment : BaseFragment<FragmentSaveCalendarBinding>(R.layout.fragment_save_calendar) {

    private val saveCalendarViewModel by viewModels<SaveCalendarViewModel>()
    private val saveCalendarAdapter by lazy { SaveCalendarAdapter(viewLifecycleOwner, ::onCheckPointClick) }

    private val args: SaveCalendarFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupDataBinding()
        setupRecyclerView()
        setupToolbarMenuOnItemClickListener()
        collectCheckPointItemList()
        collectSaveCalendarEvent()
        collectUseDefaultCalendar()
    }

    private fun onCheckPointClick(checkPointItem: CheckPointItem, dateType: DateType) {
        lifecycleScope.launch {
            val dateInMillis = pickDateInMillis() ?: return@launch

            val dateTime = LocalDate.ofEpochDay(dateInMillis / 1000 / 60 / 60 / 24)

            when (dateType) {
                DateType.START -> checkPointItem.startDate.emit(dateTime)
                DateType.END -> checkPointItem.endDate.emit(dateTime)
            }
        }
    }

    private fun setupToolbar() = with(binding) {
        toolbarSaveCalendar.setupWithNavController(navController)

        if (args.calendarId == 0L) {
            toolbarSaveCalendar.title = getString(R.string.calendar_add)
        } else {
            toolbarSaveCalendar.title = getString(R.string.calendar_edit)
        }
    }

    private fun setupDataBinding() {
        binding.saveCalendarViewModel = saveCalendarViewModel
    }

    private fun setupRecyclerView() {
        binding.rvSaveCalendarCheckPointList.adapter = saveCalendarAdapter
    }

    private fun setupToolbarMenuOnItemClickListener() {
        binding.toolbarSaveCalendar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.menu_delete_checkPoint) {
                val currentCheckPointItemList = saveCalendarAdapter.currentList
                saveCalendarViewModel.deleteCheckPointItem(currentCheckPointItemList)
                true
            } else {
                false
            }
        }
    }

    private fun collectCheckPointItemList() {
        stateCollect(saveCalendarViewModel.checkPointItemList) { list ->
            saveCalendarAdapter.submitList(list.sortedWith(compareBy(nullsFirst(reverseOrder())) { it.startDate.value }))
            delay(300)
            binding.svSaveCalendar.smoothScrollTo(0, binding.tvSaveCalendarSaveCalendar.bottom)
        }
    }

    private fun collectUseDefaultCalendar() {
        stateCollect(saveCalendarViewModel.useDefaultCalendar) { checked ->
            when (checked) {
                true -> {
                    binding.rvSaveCalendarCheckPointList.visibility = View.GONE
                    binding.tvSaveCalendarAddCheckPointView.visibility = View.GONE
                }
                false -> {
                    binding.rvSaveCalendarCheckPointList.visibility = View.VISIBLE
                    binding.tvSaveCalendarAddCheckPointView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun collectSaveCalendarEvent() {
        sharedCollect(saveCalendarViewModel.saveCalendarEvent) { isSaved ->
            if (isSaved) {
                navController.navigateUp()
            } else {
                Toast.makeText(context, "입력 값이 이상해요", Toast.LENGTH_LONG).show()
            }
        }
    }
}
