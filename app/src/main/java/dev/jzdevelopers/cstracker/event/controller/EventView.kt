package dev.jzdevelopers.cstracker.event.controller

import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.common.EventSort
import dev.jzdevelopers.cstracker.event.common.EventSort.*
import dev.jzdevelopers.cstracker.event.models.Event
//import dev.jzdevelopers.cstracker.event.models.SeniorTechHelp
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZRecyclerAdapterFB
import dev.jzdevelopers.cstracker.libs.JZPrefs
import dev.jzdevelopers.cstracker.user.models.PrimaryUser
import kotlinx.android.synthetic.main.ui_event_add.view.*
import kotlinx.android.synthetic.main.ui_event_design.view.*
import kotlinx.android.synthetic.main.ui_event_view.*

/** Android Activity EventView,
 *  Activity That Shows All Of The Events Under The Signed In Primary User
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class EventView : JZActivity() {

    // Defines JZRecyclerAdapterFB Variable//
    private lateinit var adapter    : JZRecyclerAdapterFB<Event>
    private lateinit var searchView : SearchView

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

            // Sets The Icon Color of The System Bars//
            statusBarColor(isDarkIcons = true)

            // Sets The Menu For The Activity//
            menu(bottomBar, R.menu.menu_secondary_user_view)
        }

        // Shows The Events//
        showEvents()
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // Define And Initialize MenuItem Values//
        val settings = menu.findItem(R.id.settings)
        val sort     = menu.findItem(R.id.sort)

        // When An Adapter Item Is Clicked//
        click(adapter) {
            toastShort("clicked $it")
        }

        // When fabAddEvent Is Clicked//
        click(fabAddEvent) {

            // Starts The EventAdd Activity//
            startActivity(EventAdd::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When Sort Is Clicked//
        click(sort) {

            // Define And Initializes List Value//
            val sortTypes = listOf("Date", "Location", "Name", "People In Charge", "Total Time")

            // Shows Sort Dialog//
            MaterialDialog(this).show {
                title(R.string.title_sort_event)
                listItemsSingleChoice(items = sortTypes, initialSelection = sortNum)
                listItemsSingleChoice(items = sortTypes) { _, _, text ->
                    when(text) {
                        "Date"             -> JZPrefs.savePref(this@EventView, prefSort, DATE.ordinal)
                        "Location"         -> JZPrefs.savePref(this@EventView, prefSort, LOCATION.ordinal)
                        "Name"             -> JZPrefs.savePref(this@EventView, prefSort, NAME.ordinal)
                        "People In Charge" -> JZPrefs.savePref(this@EventView, prefSort, PEOPLE_IN_CHARGE.ordinal)
//                        "Total Time"       -> JZPrefs.savePref(this@EventView, prefSort, TOTAL_TIME.ordinal)
                    }
                }
                positiveButton(0, getString(R.string.button_only)) {

                    // Restarts The Activity//
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
        val primaryUserId = PrimaryUser.getId(this)
        val query         = Event.getAll(primaryUserId, eventSort)

        // Creates And Shows The Events//
        adapter = JZRecyclerAdapterFB(this, scope, layout, query, Event::class) { it, _ ->

            // Generates The Different Properties//
            val date     = it.date
            val location = it.location
            val name     = it.name

            // Matches The Basic Properties With Their Nodes//
            eventNameText.text     = name
            eventDateText.text     = date
            eventLocationText.text = location
            startTimeValue.text    = it.startTime
            endTimeValue.text      = it.endTime
//            eventTotalTimeValue.text     = it.totalTime
        }
        adapter.attachRecyclerView(eventList)
    }
}