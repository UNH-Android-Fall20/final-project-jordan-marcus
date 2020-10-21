package dev.jzdevelopers.cstracker.user.authentication

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.EventView
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.MultiUser.*
import dev.jzdevelopers.cstracker.user.SecondaryUserAdd
import dev.jzdevelopers.cstracker.user.data_classes.PrimaryUser
import dev.jzdevelopers.cstracker.user.data_classes.PrimaryUser.Companion.activate
import dev.jzdevelopers.cstracker.user.data_classes.PrimaryUser.Companion.isActivated
import dev.jzdevelopers.cstracker.user.data_classes.PrimaryUser.Companion.isSignedIn
import kotlinx.android.synthetic.main.ui_user_sign_in.*

/** Android Activity UserSignIn
 *  Activity That Signs In A User
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class UserSignIn: JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_user_sign_in) {

            // Sets The Icon Color of The System Bars//
            navigationColor(R.color.white, true)
            statusBarColor(isDarkIcons  = true)
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When Back Is Clicked//
        clickBack { exitApp() }

        // When forgotPassword Is Clicked//
        click(forgotPassword) {

            // Starts The Activity UserPasswordReset//
            startActivity(UserPasswordReset::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When signIn Is Clicked//
        click(signIn) {

            // Gets The Inputted Data//
            val email    = email.text.toString()
            val password = password.text.toString()

            // Define And Instantiates The Primary User//
            val primaryUser = PrimaryUser(email = email)

            // Signs In The Primary User//
            primaryUser.signIn(this, progressBar, password)
            checkLoginStatus()
        }

        // When signupText Is Clicked//
        click(signupText) {

            // Starts The Activity UserSignUp//
            startActivity(UserSignUp::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When uiUserSignIn Is Clicked//
        click(uiUserSignIn) {
            email.clearFocus()
            password.clearFocus()
        }
    }

    /**.
     * Function That Checks To See If The User Is Already Signed In To Bypass The Login Screen
     */
    private suspend fun checkLoginStatus() {

        // When The Primary User Is Not Signed In//
        if (!isSignedIn(this)) return
        if (!isActivated(this)) {
            activate(this)
            startActivity(UserActivation::class, R.anim.faze_in, R.anim.faze_out)
        }

        // Gets The Primary User's Multi-User Preference//
        val multiUser = PrimaryUser.getCachedMultiUser(this)

        // Starts The Activity Based On The User Mode//
        when(multiUser) {
            YES.ordinal        -> startActivity(SecondaryUserAdd::class, R.anim.faze_in, R.anim.faze_out)
            NO.ordinal         -> startActivity(EventView::class, R.anim.faze_in, R.anim.faze_out)
            SIGNED_OUT.ordinal -> return
        }
    }
}