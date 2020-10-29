package dev.jzdevelopers.cstracker.user.controller.crud

import android.view.View.VISIBLE
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZRecyclerAdapterFB
import dev.jzdevelopers.cstracker.user.common.UserOrder
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
    private lateinit var adapter: JZRecyclerAdapterFB<SecondaryUser>

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
        }

        // Shows The Secondary Users//
        showSecondaryUsers()
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When fabAddProfile Is Clicked//
        click(fabAddProfile) {

            // Starts The SecondaryUserAdd Activity//
            startActivity(SecondaryUserAdd::class, R.anim.faze_in, R.anim.faze_out)
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

        // Gets The Query For Showing All Of The Secondary-Users In A Particular Order//
        val primaryUserId = PrimaryUser.getId(this)
        val query         = SecondaryUser.getAll(primaryUserId, UserOrder.FIRST_NAME)

        // Create And Shows The Secondary-Users//
        adapter = JZRecyclerAdapterFB(this, scope, layout, query, SecondaryUser::class) {

            // Generates The Different Properties//
            val fullName      = "${it.firstName} ${it.lastName}"
            val goal          = if (it.goal in 1..9)  "0${it.goal}"  else it.goal.toString()
            val grade         = if (it.grade in 1..9) "0${it.grade}" else it.grade.toString()

            // Matches The Basic Properties With Their Nodes//
            goalText.text         = goal
            gradeText.text        = grade
            nameText.text         = fullName
            organizationText.text = it.organization
            totalTimeText.text    = it.totalTime

            // Gets The Icon Reference If One Is Present//
            val iconReference = it.iconReference()

            // When There Is No Reference//
            if (iconReference == null) {
                nameLetter.text = it.nameLetter
                nameLetter.visibility = VISIBLE
                return@JZRecyclerAdapterFB
            }

            // Shows The User Icon//
            userIcon.borderColor = getColorCompat(R.color.transparent)
            Glide.with(this)
                .load(iconReference)
                .into(userIcon)
        }
        adapter.attachRecyclerView(profileList)
    }
}