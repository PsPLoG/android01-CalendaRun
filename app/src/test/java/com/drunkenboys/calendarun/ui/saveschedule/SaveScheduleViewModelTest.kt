package com.drunkenboys.calendarun.ui.saveschedule

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.drunkenboys.calendarun.data.calendar.local.CalendarLocalDataSource
import com.drunkenboys.calendarun.data.schedule.entity.Schedule
import com.drunkenboys.calendarun.data.schedule.local.FakeCalendarLocalDataSource
import com.drunkenboys.calendarun.data.schedule.local.FakeScheduleLocalDataSource
import com.drunkenboys.calendarun.data.schedule.local.ScheduleLocalDataSource
import com.drunkenboys.calendarun.ui.saveschedule.model.BehaviorType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class SaveScheduleViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var scheduleDataSource: ScheduleLocalDataSource
    private lateinit var calendarDataSource: CalendarLocalDataSource

    private lateinit var viewModel: SaveScheduleViewModel

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        scheduleDataSource = FakeScheduleLocalDataSource()
        calendarDataSource = FakeCalendarLocalDataSource()
        viewModel = SaveScheduleViewModel(0, 0, calendarDataSource, scheduleDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `뷰모델_초기화_테스트`() {
        val calendarName = "test calendar"

        viewModel.init(BehaviorType.INSERT)

        assertEquals(calendarName, viewModel.calendarName.value)
    }

    @Test
    fun `일정_수정_시_뷰모델_초기화_테스트`() = testScope.runBlockingTest {
        val startDate = LocalDateTime.now()
        val endDate = LocalDateTime.now()
        scheduleDataSource.insertSchedule(Schedule(0, 0, "test", startDate, endDate, Schedule.NotificationType.A_HOUR_AGO, "memo", 0))

        viewModel.init(BehaviorType.UPDATE)

        assertEquals("test", viewModel.title.value)
        assertEquals(startDate, viewModel.startDate.value)
        assertEquals(endDate, viewModel.endDate.value)
        assertEquals(Schedule.NotificationType.A_HOUR_AGO, viewModel.notificationType.value)
        assertEquals("memo", viewModel.memo.value)
        assertEquals(0, viewModel.tagColor.value)
    }

    @Test
    fun `제목_미입력_시_저장_테스트`() = testScope.runBlockingTest {
        viewModel.init(BehaviorType.INSERT)
        viewModel.saveSchedule()

        assertEquals(null, viewModel.saveScheduleEvent.value)
    }

    @Test
    fun `정상_입력_시_저장_테스트`() {
        viewModel.init(BehaviorType.INSERT)
        val title = "test title"
        val memo = "test memo"
        viewModel.title.value = title
        viewModel.memo.value = memo

        viewModel.saveSchedule()
        val result = viewModel.saveScheduleEvent.value

        assertEquals(title, result?.name)
        assertEquals(memo, result?.memo)
    }

    @Test
    fun `일정_삭제_테스트`() = testScope.runBlockingTest {
        val startDate = LocalDateTime.now()
        val endDate = LocalDateTime.now()
        val schedule = Schedule(0, 0, "test", startDate, endDate, Schedule.NotificationType.A_HOUR_AGO, "memo", 0)
        scheduleDataSource.insertSchedule(schedule)
        viewModel.init(BehaviorType.UPDATE)

        viewModel.deleteSchedule()

        assertEquals(schedule, viewModel.deleteScheduleEvent.value)
    }
}
