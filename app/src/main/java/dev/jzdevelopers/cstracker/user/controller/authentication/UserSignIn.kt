package dev.jzdevelopers.cstracker.user.controller.authentication

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.event.controller.EventView
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.settings.Theme
import dev.jzdevelopers.cstracker.user.controller.crud.SecondaryUserView
import dev.jzdevelopers.cstracker.user.models.PrimaryUser
import dev.jzdevelopers.cstracker.user.models.PrimaryUser.Companion.getCachedMultiUser
import dev.jzdevelopers.cstracker.user.models.PrimaryUser.Companion.isSignedIn
import kotlinx.android.synthetic.main.ui_user_sign_in.*

/** Android Activity UserSignIn,
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

            // Sets The Theme//
            val theme = Theme.getAppTheme(this@UserSignIn)
            theme(theme)

            // Sets The Bar Colors And Icon Colors//
            val barColor = Theme.getStatusBarColor(this@UserSignIn)
            when(barColor) {
                R.color.white -> {
                    navigationColor(barColor, true)
                    statusBarColor(barColor, true)
                }
                else -> {
                    navigationColor(barColor, false)
                    statusBarColor(barColor, false)
                }
            }
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

            // Signs In The Primary-User//
            val isSuccessful = PrimaryUser.signIn(this, progressBar, email, password)
            if (!isSuccessful) return@click
            signInUser()
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
     * Function That Signs The User In
     */
    private suspend fun signInUser() {

        // When The Primary-User Is Not Signed In//
        if (!isSignedIn(this)) return

        // Gets The Primary-User's Multi-User Preference//
        val isMultiUser = getCachedMultiUser(this)

        // Starts The Activity Based On The User Mode//
        when(isMultiUser) {
            true  -> startActivity(SecondaryUserView::class, R.anim.faze_in, R.anim.faze_out)
            false -> startActivity(EventView::class, R.anim.faze_in, R.anim.faze_out)
        }
    }
}