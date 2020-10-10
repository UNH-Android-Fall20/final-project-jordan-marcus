package dev.jzdevelopers.cstracker.common

import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import kotlinx.android.synthetic.main.ui_sign_up.*

/** Android Activity SignUp
 *  Activity That Signs Up A New User
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class SignUp: JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_sign_up) {

            // Sets The Theme For The Activity//
            theme(R.style.GreenTheme, false)
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When UI Sign-Up Is Clicked//
        click(uiSignUp) {
            firstName.clearFocus()
            lastName.clearFocus()
            email.clearFocus()
            password.clearFocus()
            confirmPassword.clearFocus()
        }
    }
}