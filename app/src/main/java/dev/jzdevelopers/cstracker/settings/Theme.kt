package dev.jzdevelopers.cstracker.settings

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.libs.JZPrefs
import dev.jzdevelopers.cstracker.user.common.UserTheme
import dev.jzdevelopers.cstracker.user.common.UserTheme.*
import dev.jzdevelopers.cstracker.user.controller.crud.SecondaryUserView
import kotlinx.android.synthetic.main.ui_theme.*
import kotlin.random.Random

/** Android Activity Theme,
 *  Activity That Picks A Theme For The App
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class Theme: JZActivity() {

    /**.
     * Configures Static Functions And Variables
     */
    companion object {

        // Define And Initializes SharedPreference Const Values//
        private const val PF_LAST   = "dev.jzdevelopers.cstracker.last"
        private const val PF_OLED   = "dev.jzdevelopers.cstracker.oled"
        private const val PF_RANDOM = "dev.jzdevelopers.cstracker.random"
        private const val PF_THEME  = "dev.jzdevelopers.cstracker.theme"

        // Define And Initializes Int Const Values//
        private const val NO_THEME = -1

        // Define And Initializes Int Values//
        private var theme       = NO_THEME
        private var themeNumber = NO_THEME

        /**.
         * Function That Gets The Theme For The App Based On The Users Preference
         * @param [context] - Gets the instance from the caller activity
         * @return The theme
         */
        fun getAppTheme(context: Context): Int {

            // Gets The Theme Preferences From The User//
            val isRandomChecked = JZPrefs.getPref(context, PF_RANDOM, false)
            val savedTheme = JZPrefs.getPref(context, PF_THEME, GREEN.ordinal)

            // Returns The Set Theme//
            return when {

                // When The User Wants A Random Theme//
                isRandomChecked && theme != NO_THEME -> theme

                // When The Theme Or Black Type Has Changed//
                themeNumber != savedTheme || themeNumber == BLACK.ordinal -> getTheme(context, savedTheme)

                // Returns The Picked Theme//
                else -> theme
            }
        }

        /**.
         * Function That Returns The Card Color Based On The Users Saved Theme
         * @param [savedTheme] The theme saved by the user
         * @return The saved theme card color
         */
        fun getCardColor(savedTheme: UserTheme): Int {

            // Gets The Saved Theme Card Color//
            return when(savedTheme) {
                DEFAULT -> R.attr.colorPrimary
                RED     -> R.color.red
                ORANGE  -> R.color.orange
                YELLOW  -> R.color.yellow
                GREEN   -> R.color.green
                BLUE    -> R.color.blue
                INDIGO  -> R.color.indigo
                VIOLET  -> R.color.violet
                PINK    -> R.color.pink
                TEAL    -> R.color.teal
                BROWN   -> R.color.brown
                BLACK   -> R.color.grey
            }
        }

        /**.
         * Function That Gets The Status Bar Color Based On The Picked Theme
         * @param [context] Gets the instance from the caller activity
         * @return The status bar color
         */
        fun getStatusBarColor(context: Context): Int {

            return when {
                themeNumber != BLACK.ordinal -> R.color.white
                else -> getBlackColor(context)

            }
        }

        /**.
         * Function That Gets The Theme
         * @param [context]    Gets the instance from the caller activity
         * @param [savedTheme] The preference where the theme is saved
         * @return The theme
         */
        private fun getTheme(context: Context, savedTheme: Int): Int {

            // Gets Whether The Theme Should Be Random Or Not//
            val isRandomChecked = JZPrefs.getPref(context, PF_RANDOM, false)
            if (isRandomChecked) return getRandomTheme(context)

            // Sets The Theme Number//
            themeNumber = savedTheme

            // Sets The Saved Theme//
            theme = when(savedTheme) {
                RED.ordinal    -> R.style.RedTheme
                ORANGE.ordinal -> R.style.OrangeTheme
                YELLOW.ordinal -> R.style.YellowTheme
                GREEN.ordinal  -> R.style.GreenTheme
                BLUE.ordinal   -> R.style.BlueTheme
                INDIGO.ordinal -> R.style.IndigoTheme
                VIOLET.ordinal -> R.style.VioletTheme
                PINK.ordinal   -> R.style.PinkTheme
                TEAL.ordinal   -> R.style.TealTheme
                BROWN.ordinal  -> R.style.BrownTheme
                BLACK.ordinal  -> getBlackTheme(context)
                else           -> R.style.GreenTheme
            }

            // Returns The Saved Theme//
            return theme
        }

        /**.
         * Function That Gets The Correct Black Color Based On The User's Preference
         * @param [context] Gets the instance from the caller activity
         * @return The black color
         */
        private fun getBlackColor(context: Context): Int {

            // Gets The Black Preference Of The User//
            val isOledChecked = JZPrefs.getPref(context, PF_OLED, false)

            // Returns The Black Color//
            return when(isOledChecked) {
                true  -> R.color.blackOled
                false -> R.color.blackMatte
            }
        }

        /**.
         * Function That Gets The Correct Black Theme Based On The User's Preference
         * @param [context] Gets the instance from the caller activity
         * @return The black theme
         */
        private fun getBlackTheme(context: Context): Int {

            // Gets The Black Preference Of The User//
            val isOledChecked = JZPrefs.getPref(context, PF_OLED, false)

            // Returns The Black Theme//
            return when(isOledChecked) {
                true  -> R.style.OledBlackTheme
                false -> R.style.MatteBlackTheme
            }
        }

        /**.
         * Function That Gets A Random Theme To Show The User
         * @param [context] Gets the instance from the caller activity
         * @return The random theme
         */
        private fun getRandomTheme(context: Context): Int {

            // Gets The Last Theme Shown To The User//
            val lastTheme = JZPrefs.getPref(context, PF_LAST, GREEN.ordinal)

            // Gets A Random Theme//
            var randomTheme = Random.nextInt(1, 11)

            // Makes Sure The Random Theme Is Different From The Last Theme That Was Shown//
            while (true) {
                if (randomTheme != lastTheme) break
                randomTheme = Random.nextInt(1, 11)
            }

            // Gets The Random Theme//
            theme = when(randomTheme) {
                RED.ordinal    -> {
                    JZPrefs.savePref(context, PF_LAST, RED.ordinal)
                    R.style.RedTheme
                }
                ORANGE.ordinal -> {
                    JZPrefs.savePref(context, PF_LAST, ORANGE.ordinal)
                    R.style.OrangeTheme
                }
                YELLOW.ordinal -> {
                    JZPrefs.savePref(context, PF_LAST, YELLOW.ordinal)
                    R.style.YellowTheme
                }
                GREEN.ordinal  -> {
                    JZPrefs.savePref(context, PF_LAST, GREEN.ordinal)
                    R.style.GreenTheme
                }
                BLUE.ordinal   -> {
                    JZPrefs.savePref(context, PF_LAST, BLUE.ordinal)
                    R.style.BlueTheme
                }
                INDIGO.ordinal -> {
                    JZPrefs.savePref(context, PF_LAST, INDIGO.ordinal)
                    R.style.IndigoTheme
                }
                VIOLET.ordinal -> {
                    JZPrefs.savePref(context, PF_LAST, VIOLET.ordinal)
                    R.style.VioletTheme
                }
                PINK.ordinal   -> {
                    JZPrefs.savePref(context, PF_LAST, PINK.ordinal)
                    R.style.PinkTheme
                }
                TEAL.ordinal   -> {
                    JZPrefs.savePref(context, PF_LAST, TEAL.ordinal)
                    R.style.TealTheme
                }
                BROWN.ordinal  -> {
                    JZPrefs.savePref(context, PF_LAST, BROWN.ordinal)
                    R.style.BrownTheme
                }
                BLACK.ordinal  -> {
                    JZPrefs.savePref(context, PF_LAST, BLACK.ordinal)
                    getBlackTheme(context)
                }
                else           -> {
                    JZPrefs.savePref(context, PF_LAST, GREEN.ordinal)
                    R.style.GreenTheme
                }
            }

            // Returns The Random Theme//
            return theme
        }
    }

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_theme) {

            // Sets The Theme//
            val theme = getAppTheme(this@Theme)
            theme(theme)

            // Sets The Status Bar Color And Icon Color//
            val statusBarColor = getStatusBarColor(this@Theme)
            when(statusBarColor) {
                R.color.white -> statusBarColor(statusBarColor, true)
                else          -> statusBarColor(statusBarColor, false)
            }

        }

        // Invoke Functions//
        switchStates()
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

        // When cardOled Is Clicked//
        click(cardOled) {

            // Changes The Switch's Toggle State//
            switchOled.isChecked = !switchOled.isChecked

            // Saves The State Of The Switch//
            when(switchOled.isChecked) {
                true  -> JZPrefs.savePref(this, PF_OLED, true)
                false -> JZPrefs.savePref(this, PF_OLED, false)
            }

            // Starts The Theme Activity//
            startActivity(Theme::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When cardRandomTheme Is Clicked//
        click(cardRandomTheme) {

            // Changes The Switch's Toggle State//
            switchRandomTheme.isChecked = !switchRandomTheme.isChecked

            // Saves The State Of The Switch//
            when(switchRandomTheme.isChecked) {
                true  -> JZPrefs.savePref(this, PF_RANDOM, true)
                false -> JZPrefs.savePref(this, PF_RANDOM, false)
            }

            // Starts The Theme Activity//
            startActivity(Theme::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When colorPreview Is Clicked//
        click(cardTheme) {

            // Defines All The Possible Theme Colors//
            val red        = getColorCompat(R.color.red)
            val orange     = getColorCompat(R.color.orange)
            val yellow     = getColorCompat(R.color.yellow)
            val green      = getColorCompat(R.color.green)
            val blue       = getColorCompat(R.color.blue)
            val indigo     = getColorCompat(R.color.indigo)
            val violet     = getColorCompat(R.color.violet)
            val pink       = getColorCompat(R.color.pink)
            val teal       = getColorCompat(R.color.teal)
            val brown      = getColorCompat(R.color.brown)
            val matteBlack = getColorCompat(R.color.blackMatte)

            // Puts The Theme Colors Into An Array//
            val colors = intArrayOf(red, orange, yellow, green, blue, indigo, violet, pink, teal, brown, matteBlack)

            // Shows The Theme Picker Dialog//
            MaterialDialog(this).show {

                // Sets The Title For The Dialog//
                title(R.string.title_theme)

                // Lets The User Chose An App Theme Color//
                colorChooser(colors) { _, color ->

                    // Saves The Picked Theme Color//
                    when(color) {
                        red        -> JZPrefs.savePref(this@Theme, PF_THEME, RED.ordinal)
                        orange     -> JZPrefs.savePref(this@Theme, PF_THEME, ORANGE.ordinal)
                        yellow     -> JZPrefs.savePref(this@Theme, PF_THEME, YELLOW.ordinal)
                        green      -> JZPrefs.savePref(this@Theme, PF_THEME, GREEN.ordinal)
                        blue       -> JZPrefs.savePref(this@Theme, PF_THEME, BLUE.ordinal)
                        indigo     -> JZPrefs.savePref(this@Theme, PF_THEME, INDIGO.ordinal)
                        violet     -> JZPrefs.savePref(this@Theme, PF_THEME, VIOLET.ordinal)
                        pink       -> JZPrefs.savePref(this@Theme, PF_THEME, PINK.ordinal)
                        teal       -> JZPrefs.savePref(this@Theme, PF_THEME, TEAL.ordinal)
                        brown      -> JZPrefs.savePref(this@Theme, PF_THEME, BROWN.ordinal)
                        matteBlack -> JZPrefs.savePref(this@Theme, PF_THEME, BLACK.ordinal)
                    }
                }

                // Closes The Dialog//
                positiveButton(R.string.button_only) {

                    // Starts Activity Theme//
                    startActivity(Theme::class, R.anim.faze_in, R.anim.faze_out)
                }
            }
        }

        // When fabExit Is Clicked//
        click(fabExit) {

            // Starts The SecondaryUserView Activity//
            startActivity(SecondaryUserView::class, R.anim.faze_in, R.anim.faze_out)
        }
    }

    /**.
     * Function That Sets The Switch Checked State Based On The User's Preference
     */
    private fun switchStates() {

        // Gets The Checked States Of The Switches//
        val isOledChecked   = JZPrefs.getPref(this, PF_OLED, false)
        val isRandomChecked = JZPrefs.getPref(this, PF_RANDOM, false)

        // Checks The Switches Based On Their States//
        if (isOledChecked)   switchOled.isChecked = true
        if (isRandomChecked) switchRandomTheme.isChecked = true
    }
}