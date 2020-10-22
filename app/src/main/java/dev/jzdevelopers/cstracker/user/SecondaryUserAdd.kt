package dev.jzdevelopers.cstracker.user

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.UserTheme.*
import dev.jzdevelopers.cstracker.user.data_classes.PrimaryUser
import dev.jzdevelopers.cstracker.user.data_classes.SecondaryUser
import kotlinx.android.synthetic.main.ui_secondary_user_add.*
import java.util.Locale.getDefault

/** Android Activity SecondaryUserAdd,
 *  Activity That Adds A Secondary User Profile To The Signed-In Primary User
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class SecondaryUserAdd: JZActivity() {

    //<editor-fold desc="Class Variables">

    // Define And Initialize Int Value//
    private val userIconCode = 0

    // Define And Initialize Uri Variable//
    private var userIconUri: Uri? = null

    //</editor-fold>

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_secondary_user_add) {

            // Sets The Icon Color of The System Bars//
            statusBarColor(isDarkIcons = true)
        }
    }

    /**.
     * Function That Handles All Listeners For The Activity
     */
    override fun createListeners() {

        // When Back Is Clicked//
        clickBack {

            // Shows The Error Dialog//
            MaterialDialog(this).show {
                title(R.string.title_no_save)
                message(R.string.positive_no_save)
                cancelOnTouchOutside(false)
                cornerRadius(16.0f)
                negativeButton(R.string.button_negative)
                positiveButton(R.string.button_positive) {

                    // Goes Back To Activity SecondaryUserView//
                    exitActivity(R.anim.faze_in, R.anim.faze_out)
                }
            }
        }

        // When fabSaveProfile Is Clicked//
        click(fabSaveProfile) {

            // Gets The Inputted Int Data//
            val goalEditText  = goal.text.toString()
            val gradeEditText = grade.text.toString()
            var goal          = 0
            var grade         = 0

            // Extracts The Int Data From The EditText String//
            if (!goalEditText.isBlank())  goal  = goalEditText.toInt()
            if (!gradeEditText.isBlank()) grade = gradeEditText.toInt()

            // Gets The Inputted String Data//
            val firstName     = firstName.text.toString()
            val lastName      = lastName.text.toString()
            val nameLetter    = nameLetter.text.toString()
            val organization  = organization.text.toString()
            val primaryUserId = PrimaryUser.getId(this)
            val theme         = getPickedTheme()

            // Signs Up The New Secondary User//
            val secondaryUser = SecondaryUser(this, firstName, lastName, theme, goal, 0, grade, nameLetter, organization, primaryUserId, "0:00", userIconUri)
            val isSuccessful  = secondaryUser.signUp(progressBar)
            if (!isSuccessful) return@click

            // Starts The SecondaryUserView Activity//
            startActivity(SecondaryUserView::class, R.anim.faze_in, R.anim.faze_out)
        }

        // When userIcon Is Clicked//
        click(userIcon) {
            val pickIcon = Intent(Intent.ACTION_PICK, INTERNAL_CONTENT_URI)
            startActivityResult(pickIcon, userIconCode) {requestCode, _, data ->

                // When A Picture Was Not Picked Successfully//
                if (requestCode != userIconCode || data == null)
                    return@startActivityResult

                // Sets The Picked Image Uri//
                userIconUri = data.data

                // Hides The Name Letter//
                nameLetter.visibility = View.INVISIBLE

                // Shoes The userIcon Image To The User//
                userIcon.setImageURI(userIconUri)
                userIcon.borderColor = getColorCompat(R.color.transparent)
            }
        }

        // When userIcon Is Long Clicked//
        longClick(userIcon) {

            // Clears The Image//
            userIcon.setImageResource(R.color.transparent)
            userIcon.borderColor = getColorAttr(R.attr.colorPrimary)
            userIconUri = null

            // Makes nameLetter Visible//
            nameLetter.visibility = View.VISIBLE

            // When firstName Is Blank//
            val isBlank = firstName.text.toString().isBlank()
            if (isBlank) {
                nameLetter.text = "A"
                return@longClick
            }

            // Gets The First Letter In firstName As The Name-Letter//
            val nameLetterText = firstName.text.toString()[0].toString()
            nameLetter.text = nameLetterText
        }

        // When The userTheme Progress Changes//
        progressChange(userTheme) { _, progress, _ ->

            // Set SeekBar Title Based On SeekBar Progress//
            when(progress) {
                DEFAULT.ordinal -> userThemeText.setText(R.string.user_theme_default_text)
                RED.ordinal     -> userThemeText.setText(R.string.user_theme_red_text)
                ORANGE.ordinal  -> userThemeText.setText(R.string.user_theme_orange_text)
                YELLOW.ordinal  -> userThemeText.setText(R.string.user_theme_yellow_text)
                GREEN.ordinal   -> userThemeText.setText(R.string.user_theme_green_text)
                BLUE.ordinal    -> userThemeText.setText(R.string.user_theme_blue_text)
                INDIGO.ordinal  -> userThemeText.setText(R.string.user_theme_indigo_text)
                VIOLET.ordinal  -> userThemeText.setText(R.string.user_theme_violet_text)
                PINK.ordinal    -> userThemeText.setText(R.string.user_theme_pink_text)
                TEAL.ordinal    -> userThemeText.setText(R.string.user_theme_teal_text)
                BROWN.ordinal   -> userThemeText.setText(R.string.user_theme_brown_text)
                BLACK.ordinal   -> userThemeText.setText(R.string.user_theme_black_text)
            }
        }

        // When The firstName Text Changes//
        textChange(firstName) {text, _, _, _ ->

            // Gets The First Letter Inputted//
            if (text.isBlank()) return@textChange
            nameLetter.text = text[0].toString().trim().capitalize(getDefault())
        }
    }

    /**.
     * Function That Gets The Theme Picked By The User
     * @return The theme
     */
    private fun getPickedTheme(): UserTheme {

        // Returns The Picked Theme//
        return when(userTheme.progress) {
            DEFAULT.ordinal -> DEFAULT
            RED.ordinal     -> RED
            ORANGE.ordinal  -> ORANGE
            YELLOW.ordinal  -> YELLOW
            GREEN.ordinal   -> GREEN
            BLUE.ordinal    -> BLUE
            INDIGO.ordinal  -> INDIGO
            VIOLET.ordinal  -> VIOLET
            PINK.ordinal    -> PINK
            TEAL.ordinal    -> TEAL
            BROWN.ordinal   -> BROWN
            BLACK.ordinal   -> BLACK
            else            -> DEFAULT
        }
    }
}