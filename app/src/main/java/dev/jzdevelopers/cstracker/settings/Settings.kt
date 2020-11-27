package dev.jzdevelopers.cstracker.settings

import com.afollestad.materialdialogs.MaterialDialog
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.controller.authentication.UserSignIn
import dev.jzdevelopers.cstracker.user.controller.crud.SecondaryUserView
import dev.jzdevelopers.cstracker.user.models.PrimaryUser
import kotlinx.android.synthetic.main.ui_settings.*

/** Android Activity Settings,
 *  Activity That Shows The Settings For The App
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class Settings: JZActivity() {

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_settings) {

            // Sets The Theme//
            val theme = Theme.getAppTheme(this@Settings)
            theme(theme)

            // Sets The Status Bar Color And Icon Color//
            val statusBarColor = Theme.getStatusBarColor(this@Settings)
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

            // Starts The SecondaryUserView Activity//
            startActivity(SecondaryUserView::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When fabExit Is Clicked//
        click(fabExit) {

            // Starts The SecondaryUserView Activity//
            startActivity(SecondaryUserView::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When cardSignOut Is Clicked//
        click(cardSignOut) {

            // Shows The Dialog//
            MaterialDialog(this).show {
                title(R.string.title_double_check)
                message(R.string.general_sign_out)
                negativeButton(R.string.button_negative)
                positiveButton(R.string.button_positive) {
                    PrimaryUser.signOut()
                    startActivity(UserSignIn::class, R.anim.faze_in, R.anim.faze_out)
                }
            }
        }

        // When cardTheme Is Clicked//
        click(cardTheme) {

            // Starts The SecondaryUserView Activity//
            startActivity(Theme::class, R.anim.faze_in, R.anim.faze_out)
        }
    }
}