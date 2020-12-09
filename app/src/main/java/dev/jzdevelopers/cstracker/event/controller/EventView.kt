package dev.jzdevelopers.cstracker.event.controller

import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.common.EventSort
import dev.jzdevelopers.cstracker.event.common.EventSort.*
import dev.jzdevelopers.cstracker.event.models.Event
import dev.jzdevelopers.cstracker.libs.*
import dev.jzdevelopers.cstracker.libs.JZDateFormat.AMERICAN
import dev.jzdevelopers.cstracker.libs.JZDateFormat.REVERSED
import dev.jzdevelopers.cstracker.libs.JZTimeFormat.MILITARY
import dev.jzdevelopers.cstracker.libs.JZTimeFormat.STANDARD
import dev.jzdevelopers.cstracker.settings.Theme
import dev.jzdevelopers.cstracker.user.controller.crud.SecondaryUserView
import kotlinx.android.synthetic.main.ui_event_design.view.*
import kotlinx.android.synthetic.main.ui_event_view.*

/** Android Activity EventView,
 *  Activity That Shows All Of The Events Under The Signed In Primary User
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class EventView: JZActivity() {

    // Defines JZRecyclerAdapterFB Variable//
    private lateinit var adapter    : JZRecyclerAdapterFB<Event>
    private lateinit var searchView : SearchView

    // Define And Initializes JZTime Variable//
    private val jzTime = JZTime()

    // Defines Secondary User ID Variable//
    private lateinit var secondaryUserId : String

    // Define And Initializes Int Variable//
    private var sortNum = 0

    // Define And Instantiates ArrayList Value//
    private val selectedItemList = ArrayList<Int>()

    // Define And Initializes SavedPreference Value//
    private val prefSort = "dev.jzdevelopers.cstracker.eventSort"

    /**.
     * What Happens When The Activity First Starts
     */
    override fun onStart() {
        super.onStart()

        // Starts Listening For Query Changed//
        adapter.startListening()
    }

    /**.
     * What Happens When The Activity First Stops
     */
    override fun onStop() {
        super.onStop()

        // Stops Listening For Query Changed//
        adapter.stopListening()
    }

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_event_view) {

            // Sets The Theme//
            val theme = Theme.getAppTheme(this@EventView)
            theme(theme)

            // Gets The Secondary User's Data//
            val secondaryUserFirstName = intent.extras?.get("SECONDARY_USER_FIRST_NAME") as String
            secondaryUserId            = intent.extras?.get("SECONDARY_USER_ID")         as String

            // Sets The Status Bar Color And Icon Color//
            val statusBarColor = Theme.getStatusBarColor(this@EventView)
            when(statusBarColor) {
                R.color.white -> statusBarColor(statusBarColor, true)
                else          -> statusBarColor(statusBarColor, false)
            }

            // Sets The Menu For The Activity//
            menu(bottomBar, R.menu.menu_event_view)

            // Sets The Title For The Activity//
            title(eventView, "$secondaryUserFirstName's Events")
        }

        // Shows The Events//
        showEvents()
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // Define And Initialize MenuItem Values//
        val sort = menu.findItem(R.id.sort)

        // When Back Is Clicked//
        clickBack {

            // Starts The SecondaryUserView Activity//
            startActivity(SecondaryUserView::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When An Adapter Item Is Clicked//
        click(adapter) {
            toastShort("clicked $it")
        }

        // When fabAddEvent Is Clicked//
        click(fabAddEvent) {

            // Starts The EventAdd Activity//
            startActivity(EventAdd::class, R.anim.faze_in, R.anim.faze_out) {
                it.putExtra("SECONDARY_USER_ID", secondaryUserId)
            }
        }

        // When Sort Is Clicked//
        click(sort) {

            // Define And Initializes List Value//
            val sortTypes = listOf("Location", "Name", "Newest to Oldest", "Oldest to Newest")

            // Shows Sort Dialog//
            MaterialDialog(this).show {
                title(R.string.title_sort_event)
                listItemsSingleChoice(items = sortTypes, initialSelection = sortNum)
                listItemsSingleChoice(items = sortTypes) { _, _, text ->
                    when(text) {
                        "Location"         -> JZPrefs.savePref(this@EventView, prefSort, LOCATION.ordinal)
                        "Name"             -> JZPrefs.savePref(this@EventView, prefSort, NAME.ordinal)
                        "Newest to Oldest" -> JZPrefs.savePref(this@EventView, prefSort, NEWEST_TO_OLDEST.ordinal)
                        "Oldest to Newest" -> JZPrefs.savePref(this@EventView, prefSort, OLDEST_TO_NEWEST.ordinal)
                    }
                }
                positiveButton(0, getString(R.string.button_only)) {

                    // Restarts The Activity//
                    /*
                     * CRASHES WHEN RESTARTING BECAUSE IT RECEIVES NO FIRST
                     * NAME FROM THE SECONDARY_USER_VIEW ACTIVITY.
                     */
                    startActivity(EventView::class, false)
                }
            }
        }

        // When The Adapter Items Are Scrolling//
        adapter.itemsScrolling {
            when {

                // When Items Are Scrolling And The SearchView Isn't Open//
                it > 0 && fabAddEvent.isShown -> fabAddEvent.hide()

                // When The SearchView Is Open//
                !searchView.isIconified -> fabAddEvent.hide()

                // When No Above Condition Is Met//
                else -> fabAddEvent.show()
            }
        }

        // When An Adapter Item Is Swiped//
        adapter.itemSwipe {

            // Gets The Id Of The Event//
            val id = adapter.getItemId(it)

            // Deletes The Event//
            val event = adapter.getItem(it)
            event.delete(id)

            // Shows The User A Message That The Item Was Deleted//
            toastShort("${event.name} was deleted")
        }

        // When The Adapter Items Are Multi-Selected//
        adapter.itemMultiSelect { itemPosition, itemSelectedCount, isSelected ->
            when {

                // When No Items Are Selected//
                itemSelectedCount == 0 -> adapter.restart()

                // When An Item Is Selected//
                isSelected -> selectedItemList.add(itemPosition)

                // When An Item Is Not Selected//
                !isSelected -> {
                    val index = selectedItemList.indexOfFirst { savedItemPosition ->
                        savedItemPosition == itemPosition
                    }
                    if (index != -1) {
                        selectedItemList.removeAt(index)
                    }
                }
            }
        }

        // When Search Is Clicked//
        searchEvents()
    }

    /**.
     * Function That Handles The Searching Of Events
     */
    private fun searchEvents() {

        // Define And Initializes The SearchView Value//
        val searchView  = menu.findItem(R.id.search).actionView as SearchView
        this.searchView = searchView

        // Sets The Query Hint//
        searchView.queryHint = "Search Events"

        // When searchView Is Closed//
        searchClose(searchView) {
            fabAddEvent.show()
        }

        // When searchView Is Open//
        searchOpen(searchView) {
            fabAddEvent.hide()
        }
    }

    /**.
     * Function That Shows All Of The Events Under The Signed In Primary User
     */
    private fun showEvents() {

        // Gets The Layout For Showing The Events//
        val layout = R.layout.ui_event_design

        // Gets The Scope For Async/Await Calls//
        val scope = lifecycleScope

        // Gets The User's Preference For Sorting The Events//
        sortNum       = JZPrefs.getPref(this, prefSort, NAME.ordinal)
        val eventSort = EventSort.values()[sortNum]

        // Gets The Query For Showing All Of The Events In A Particular Order//
        val userId = secondaryUserId
        val query  = Event.getAll(userId, eventSort)

        // Creates And Shows The Events//
        adapter = JZRecyclerAdapterFB(this, scope, layout, query, Event::class) { it, _ ->

            // Generates The Different Properties//
            val date = JZDate.switchDateFormat(it.date, REVERSED, AMERICAN)
            jzTime.startTimeHour   = JZTime.fullTimeToHour(it.startTime, MILITARY)
            jzTime.startTimeMinute = JZTime.fullTimeToMinute(it.startTime)
            jzTime.endTimeHour     = JZTime.fullTimeToHour(it.endTime, MILITARY)
            jzTime.endTimeMinute   = JZTime.fullTimeToMinute(it.endTime)

            // Matches The Basic Properties With Their Nodes//
            eventNameText.text       = it.name
            eventDateText.text       = date
            eventLocationText.text   = it.location
            eventStartTimeValue.text = jzTime.getStartTime(STANDARD)
            eventEndTimeValue.text   = jzTime.getEndTime(STANDARD)
            eventTotalTimeValue.text = it.totalTime
        }
        adapter.attachRecyclerView(eventList)
    }
}