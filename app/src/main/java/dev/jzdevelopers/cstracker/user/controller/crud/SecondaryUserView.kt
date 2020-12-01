package dev.jzdevelopers.cstracker.user.controller.crud

import android.view.View
import android.view.View.GONE
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.common.GlideApp
import dev.jzdevelopers.cstracker.event.controller.EventView
import dev.jzdevelopers.cstracker.libs.*
import dev.jzdevelopers.cstracker.libs.JZDateFormat.*
import dev.jzdevelopers.cstracker.settings.Settings
import dev.jzdevelopers.cstracker.settings.Theme
import dev.jzdevelopers.cstracker.settings.Theme.Companion.getCardColor
import dev.jzdevelopers.cstracker.user.common.UserSort
import dev.jzdevelopers.cstracker.user.common.UserSort.*
import dev.jzdevelopers.cstracker.user.common.UserTheme.DEFAULT
import dev.jzdevelopers.cstracker.user.models.PrimaryUser
import dev.jzdevelopers.cstracker.user.models.SecondaryUser
import kotlinx.android.synthetic.main.ui_secondary_user_design.view.*
import kotlinx.android.synthetic.main.ui_secondary_user_view.*
import kotlinx.coroutines.launch

/** Android Activity SecondaryUserView,
 *  Activity That Shows All Of The Secondary Users Under The Signed In Primary User
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class SecondaryUserView: JZActivity() {

    //<editor-fold desc="Class Variables">

    // Defines JZRecyclerAdapterFB Variable//
    private lateinit var adapter    : JZRecyclerAdapterFB<SecondaryUser>
    private lateinit var searchView : SearchView

    // Define And Initializes Int Variable//
    private var sortNum = 0

    // Define And Instantiates ArrayList Value//
    private val selectedItemList = ArrayList<Int>()

    // Define And Initializes SavedPreference Value//
    private val prefSort = "dev.jzdevelopers.cstracker.secondaryUserSort"

    //</editor-fold>

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
        createUI(R.layout.ui_secondary_user_view) {

            // Sets The Theme//
            val theme = Theme.getAppTheme(this@SecondaryUserView)
            theme(theme)

            // Sets The Status Bar Color And Icon Color//
            val statusBarColor = Theme.getStatusBarColor(this@SecondaryUserView)
            when(statusBarColor) {
                R.color.white -> statusBarColor(statusBarColor, true)
                else          -> statusBarColor(statusBarColor, false)
            }

            // Sets The Menu For The Activity//
            menu(bottomBar, R.menu.menu_secondary_user_view)
        }

        // Shows The Secondary Users//
        showSecondaryUsers()
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // Define And Initialize MenuItem Values//
        val settings = menu.findItem(R.id.settings)
        val sort     = menu.findItem(R.id.sort)

        // When Back Is Clicked//
        clickBack {

            // Shows The Dialog//
            MaterialDialog(this).show {
                title(R.string.title_double_check)
                message(R.string.general_app_close)
                negativeButton(R.string.button_negative)
                positiveButton(R.string.button_positive) {
                    exitApp()
                }
            }
        }

        // When An Adapter Item Is Clicked//
        click(adapter) { position ->

            // Gets The Secondary User Data//
            val secondaryUserId = adapter.getItemId(position)
            val secondaryUser   = adapter.getItem(position)

            // Starts The EventView Activity//
            startActivity(EventView::class, R.anim.faze_in, R.anim.faze_out) {
                it.putExtra("SECONDARY_USER_ID", secondaryUserId)
                it.putExtra("SECONDARY_USER_FIRST_NAME", secondaryUser.firstName)
                it.putExtra("SECONDARY_USER_THEME", secondaryUser.theme)
            }
        }

        // When fabAddProfile Is Clicked//
        click(fabAddProfile) {

            // Starts The SecondaryUserAdd Activity//
            startActivity(SecondaryUserAdd::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When settings Is Clicked//
        click(settings) {

            // Starts The Settings Activity//
            startActivity(Settings::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When Sort Is Clicked//
        click(sort) {

            // Define And Initializes List Value//
            val sortTypes = listOf("First Name", "Grade", "Last Name", "Organization")

            // Shows Sort Dialog//
            MaterialDialog(this).show {
                title(R.string.title_sort_secondary_user)
                listItemsSingleChoice(items = sortTypes, initialSelection = sortNum)
                listItemsSingleChoice(items = sortTypes) { _, _, text ->
                    when(text) {
                        "First Name"   -> JZPrefs.savePref(this@SecondaryUserView, prefSort, FIRST_NAME.ordinal)
                        "Last Name"    -> JZPrefs.savePref(this@SecondaryUserView, prefSort, LAST_NAME.ordinal)
                        "Grade"        -> JZPrefs.savePref(this@SecondaryUserView, prefSort, GRADE.ordinal)
                        "Organization" -> JZPrefs.savePref(this@SecondaryUserView, prefSort, ORGANIZATION.ordinal)
                    }
                }
                positiveButton(0, getString(R.string.button_only)) {

                    // Restarts The Activity//
                    startActivity(SecondaryUserView::class, false)
                }
            }
        }

        // When The Adapter Items Are Scrolling//
        adapter.itemsScrolling {
            when {

                // When Items Are Scrolling And The SearchView Isn't Open//
                it > 0 && fabAddProfile.isShown -> fabAddProfile.hide()

                // When The SearchView Is Open//
                !searchView.isIconified -> fabAddProfile.hide()

                // When No Above Condition Is Met//
                else   -> fabAddProfile.show()
            }
        }

        // When An Adapter Item Is Swiped//
        adapter.itemSwipe {

            // Gets The SecondaryUser//
            val id = adapter.getItemId(it)
            val secondaryUser = adapter.getItem(it)

            // Shows The Alert Dialog//
            MaterialDialog(this).show {
                title(R.string.title_double_check)
                message(R.string.general_delete_check_user)
                cancelOnTouchOutside(false)
                cornerRadius(16.0f)
                negativeButton(R.string.button_negative) {
                    adapter.restart()
                }
                positiveButton(R.string.button_positive) {

                    // Deletes The Secondary User//
                    lifecycleScope.launch { secondaryUser.delete(id) }

                    // Shows The User A Message That The Item Was Deleted//
                    toastShort("${secondaryUser.firstName} ${secondaryUser.lastName} was deleted")
                }
            }
        }

        // When The Adapter Items Are Multi-Selected//
        adapter.itemMultiSelect { itemPosition, itemSelectedCount, isSelected ->

            // Replaces The Normal Menu With The Secondary-User Selected Menu//
            replaceMenu(bottomBar, R.menu.menu_secondary_user_view_selected)

            // Define And Initialize MenuItem Values//
            val clear  = menu.findItem(R.id.clear)
            val delete = menu.findItem(R.id.delete)
            val edit   = menu.findItem(R.id.edit)

            // When clear Is Clicked//
            click(clear) { clear() }

            // When delete Is Clicked//
            click(delete) {

                // Shows The Alert Dialog//
                MaterialDialog(this).show {
                    title(R.string.title_double_check)
                    message(R.string.general_delete_check_user)
                    cancelOnTouchOutside(false)
                    cornerRadius(16.0f)
                    negativeButton(R.string.button_negative) {
                        adapter.restart()
                    }
                    positiveButton(R.string.button_positive) {

                        // Deletes All Of The Selected Items//
                        for (position in selectedItemList) {

                            // Gets The SecondaryUser//
                            val id = adapter.getItemId(position)
                            val secondaryUser = adapter.getItem(position)

                            // Deletes The Secondary User//
                            lifecycleScope.launch { secondaryUser.delete(id) }
                        }

                        // Clears All Of The Selected Items//
                        clear()

                        // Shows The User A Message That The Item Was Deleted//
                        toastShort("The selected items were deleted")
                    }
                }
            }

            // When edit Is Clicked//
            click(edit) {

                // Gets The SecondaryUser//
                val position      = selectedItemList[0]
                val id            = adapter.getItemId(position)
                val secondaryUser = adapter.getItem(position)

                // Starts The SecondaryUserEdit Activity//
                startActivity(SecondaryUserEdit::class, R.anim.faze_in, R.anim.faze_out) {
                    it.putExtra("SECONDARY_USER_ID", id)
                    it.putExtra("SECONDARY_USER", secondaryUser)
                }
            }

            when {

                // When No Items Are Selected//
                itemSelectedCount == 0 -> { clear() }

                // When An Item Is Selected//
                isSelected -> {

                    // Hides The 'Edit' Menu Item//
                    if (itemSelectedCount > 1) edit.isVisible = false

                    // Adds The Position Of The Item Selected//
                    selectedItemList.add(itemPosition)
                }

                // When An Item Is Not Selected//
                !isSelected -> {

                    // Hides The 'Edit' Menu Item//
                    if (itemSelectedCount > 1) edit.isVisible = false

                    // Removes The Item If It Exists//
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
        searchUsers()
    }

    /**.
     * Function That Clears All Of The Selected Items
     */
    private fun clear() {

        // Replaces The Secondary-User Selected Menu With The Normal Menu //
        replaceMenu(bottomBar, R.menu.menu_secondary_user_view)
        createListeners()

        // Restarts The Adapter//
        selectedItemList.clear()
        adapter.restart()
    }

    /**.
     * Function That Handles The Searching Of Secondary Users
     */
    private fun searchUsers() {

        // Define And Initializes The SearchView Value//
        val searchView  = menu.findItem(R.id.search).actionView as SearchView
        this.searchView = searchView

        // Gets The User's Preference For Sorting The Secondary User's//
        sortNum = JZPrefs.getPref(this, prefSort, FIRST_NAME.ordinal)
        val userSort = UserSort.values()[sortNum]

        // Sets The Query Hint//
        when(userSort) {
            FIRST_NAME   -> searchView.queryHint = "Search profile by first name"
            LAST_NAME    -> searchView.queryHint = "Search profile by last name"
            GRADE        -> searchView.queryHint = "Search profile by first name"
            ORGANIZATION -> searchView.queryHint = "Search profile by organization"
        }

        // When searchView Is Closed//
        searchClose(searchView) {
            adapter.restart()
            fabAddProfile.show()
        }

        // When searchView Is Open//
        searchOpen(searchView) {
            fabAddProfile.hide()
        }

        // When The User Is Searching//
        searchQueryChange(searchView) { newText ->

            // When The New Text Equals Null//
            if (newText == null) return@searchQueryChange

            // Searches The Items//
            adapter.search {

                // Searches Based On Sort Type//
                when(userSort) {
                    FIRST_NAME   -> it.firstName.contains(newText, true)
                    LAST_NAME    -> it.lastName.contains(newText, true)
                    GRADE        -> it.firstName.contains(newText, true)
                    ORGANIZATION -> it.organization.contains(newText, true)
                }
            }
        }
    }

    /**.
     * Function That Shows All Of The Secondary Users Under The Signed In Primary User
     */
    private fun showSecondaryUsers() {

        // Gets The Layout For Showing The Secondary-Users//
        val layout = R.layout.ui_secondary_user_design

        // Gets The Scope For Async/Await Calls//
        val scope  = lifecycleScope

        // Gets The User's Preference For Sorting The Secondary User's//
        sortNum = JZPrefs.getPref(this, prefSort, FIRST_NAME.ordinal)
        val userSort = UserSort.values()[sortNum]

        // Gets The Query For Showing All Of The Secondary-Users In A Particular Order//
        val primaryUserId = PrimaryUser.getId(this)
        val query = SecondaryUser.getAll(primaryUserId, userSort)

        // Create And Shows The Secondary-Users//
        adapter = JZRecyclerAdapterFB(this, scope, layout, query, SecondaryUser::class) { it, _ ->

            // Generates The Different Properties//
            val fullName = "${it.firstName} ${it.lastName}"
            val goal     = if (it.goal in 1..9)  "0${it.goal}"  else it.goal.toString()
            val grade    = if (it.grade in 1..9) "0${it.grade}" else it.grade.toString()
            val theme    = getCardColor(it.theme)

            // Matches The Basic Properties With Their Nodes//
            textGoal.text         = goal
            textGrade.text        = grade
            textName.text         = fullName
            textOrganization.text = it.organization
            textTotalTime.text    = it.totalTime

            // Sets The Background Color Of The Card//
            when(it.theme) {
                DEFAULT -> cardSecondaryUser.setCardBackgroundColor(getColorAttr(theme))
                else    -> cardSecondaryUser.setCardBackgroundColor(getColorCompat(theme))
            }

            // Clears The Image And Name-Letter Data//
            val whiteImage = getDrawableCompat(R.drawable.white)
            profileImage.setImageDrawable(whiteImage)
            nameLetter.visibility = GONE

            // Gets The Icon Reference If One Is Present//
            val iconReference = it.profileImageReference()

            // When There Is No Reference//
            if (iconReference == null) {
                nameLetter.text          = it.nameLetter
                nameLetter.visibility    = View.VISIBLE
                profileImage.borderColor = getColorCompat(R.color.transparent)
                return@JZRecyclerAdapterFB
            }

            // Shows The User Icon//
            GlideApp
                .with(profileImage)
                .load(iconReference)
                .placeholder(R.drawable.white)
                .into(profileImage)
        }
        adapter.attachRecyclerView(profileList)
    }
}