package dev.jzdevelopers.cstracker.user.controller.crud

import android.view.View
import android.view.View.GONE
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZPrefs
import dev.jzdevelopers.cstracker.libs.JZRecyclerAdapterFB
import dev.jzdevelopers.cstracker.user.common.UserSort
import dev.jzdevelopers.cstracker.user.common.UserSort.*
import dev.jzdevelopers.cstracker.user.models.PrimaryUser
import dev.jzdevelopers.cstracker.user.models.SecondaryUser
import kotlinx.android.synthetic.main.ui_secondary_user_design.view.*
import kotlinx.android.synthetic.main.ui_secondary_user_view.*

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

            // Sets The Icon Color of The System Bars//
            statusBarColor(isDarkIcons = true)

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

        // When An Adapter Item Is Clicked//
        click(adapter) {
            toastShort("clicked $it")
        }

        // When fabAddProfile Is Clicked//
        click(fabAddProfile) {

            // Starts The SecondaryUserAdd Activity//
            startActivity(SecondaryUserAdd::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When Sort Is Clicked//
        click(sort) {

            // Define And Initializes List Value//
            val sortTypes = listOf("First Name", "Grade", "Last Name", "Organization", "Total Time")

            // Shows Sort Dialog//
            MaterialDialog(this).show {
                title(R.string.title_sort_secondary_user)
                listItemsSingleChoice(items = sortTypes, initialSelection = sortNum)
                listItemsSingleChoice(items = sortTypes) { _, _, text ->
                    when(text) {
                        "First Name"   -> JZPrefs.savePref(this@SecondaryUserView, prefSort, FIRST_NAME.ordinal)
                        "Grade"        -> JZPrefs.savePref(this@SecondaryUserView, prefSort, GRADE.ordinal)
                        "Last Name"    -> JZPrefs.savePref(this@SecondaryUserView, prefSort, LAST_NAME.ordinal)
                        "Organization" -> JZPrefs.savePref(this@SecondaryUserView, prefSort, ORGANIZATION.ordinal)
                        "Total Time"   -> JZPrefs.savePref(this@SecondaryUserView, prefSort, TOTAL_TIME.ordinal)
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

            // Gets The Id Of The SecondaryUser//
            val id = adapter.getItemId(it)

            // Deletes The Secondary User//
            val secondaryUser = adapter.getItem(it)
            secondaryUser.delete(id)

            // Shows The User A Message That The Item Was Deleted//
            toastShort("${secondaryUser.firstName} ${secondaryUser.lastName} was deleted")
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
        searchUsers()
    }

    /**.
     * Function That Handles The Searching Of Secondary Users
     */
    private fun searchUsers() {

        // Define And Initializes The SearchView Value//
        val searchView  = menu.findItem(R.id.search).actionView as SearchView
        this.searchView = searchView

        // Sets The Query Hint//
        searchView.queryHint = "Search Profiles"

        // When searchView Is Closed//
        searchClose(searchView) {
            fabAddProfile.show()
        }

        // When searchView Is Open//
        searchOpen(searchView) {
            fabAddProfile.hide()
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
        val userSort    = UserSort.values()[sortNum]

        // Gets The Query For Showing All Of The Secondary-Users In A Particular Order//
        val primaryUserId = PrimaryUser.getId(this)
        val query         = SecondaryUser.getAll(primaryUserId, userSort)

        // Create And Shows The Secondary-Users//
        adapter = JZRecyclerAdapterFB(this, scope, layout, query, SecondaryUser::class) { it, _ ->

            // Generates The Different Properties//
            val fullName = "${it.firstName} ${it.lastName}"
            val goal     = if (it.goal in 1..9)  "0${it.goal}"  else it.goal.toString()
            val grade    = if (it.grade in 1..9) "0${it.grade}" else it.grade.toString()

            // Matches The Basic Properties With Their Nodes//
            goalText.text         = goal
            gradeText.text        = grade
            nameText.text         = fullName
            organizationText.text = it.organization
            totalTimeText.text    = it.totalTime

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
            Glide.with(this)
                .load(iconReference)
                .placeholder(R.drawable.white)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(profileImage)
        }
        adapter.attachRecyclerView(profileList)
    }
}