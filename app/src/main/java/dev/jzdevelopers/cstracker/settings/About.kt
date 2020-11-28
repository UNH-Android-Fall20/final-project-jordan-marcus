package dev.jzdevelopers.cstracker.settings

import android.content.Intent
import android.net.Uri
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.controller.crud.SecondaryUserView
import kotlinx.android.synthetic.main.ui_about.*
import kotlinx.android.synthetic.main.ui_settings.*
import kotlinx.android.synthetic.main.ui_settings.fabExit

/** Android Activity About,
 *  Activity That Shows The Information About The App And Ways Of Contact
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class About: JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_about) {

            // Sets The Theme//
            val theme = Theme.getAppTheme(this@About)
            theme(theme)

            // Sets The Status Bar Color And Icon Color//
            val statusBarColor = Theme.getStatusBarColor(this@About)
            when(statusBarColor) {
                R.color.white -> statusBarColor(statusBarColor, true)
                else          -> statusBarColor(statusBarColor, false)
            }
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When The Back Is Clicked//
        clickBack {

            // Starts The Settings Activity//
            startActivity(Settings::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When fabExit Is Clicked//
        click(fabExit) {

            // Starts The SecondaryUserView Activity//
            startActivity(SecondaryUserView::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When cardContactUs Is Clicked//
        click(cardContactUs) {

            // Defines The Email Information//
            val to      = "jordanzimmitti@gmail.com"
            val body    = "Please fill in the following information below\n\n 1. Device Name:\n\n 2. Android Version:\n\n My problem is..."
            val subject = "CS Tracker: Contact Us"

            // Generates The Email Template//
            val uri = Uri.parse("mailto:")
                .buildUpon()
                .appendQueryParameter("to", to)
                .appendQueryParameter("subject", subject)
                .appendQueryParameter("body", body)
                .build()

            // Opens The Users Default Email App To Send The Message//
            val emailIntent = Intent(Intent.ACTION_SENDTO, uri)
            startActivity(Intent.createChooser(emailIntent, "Sending Email..."))
        }

        // When cardFeedback Is Clicked//
        click(cardFeedback) {

            // Defines The Email Information//
            val to      = "jordanzimmitti@gmail.com"
            val body    = "Please fill in the following information below\n\n 1. What I like:\n\n 2. What I don't like:\n\n Some suggestions I have are..."
            val subject = "CS Tracker: Feedback"

            // Generates The Email Template//
            val uri = Uri.parse("mailto:")
                .buildUpon()
                .appendQueryParameter("to", to)
                .appendQueryParameter("subject", subject)
                .appendQueryParameter("body", body)
                .build()

            // Opens The Users Default Email App To Send The Message//
            val emailIntent = Intent(Intent.ACTION_SENDTO, uri)
            startActivity(Intent.createChooser(emailIntent, "Sending Email..."))
        }
    }
}