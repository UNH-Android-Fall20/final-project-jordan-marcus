package dev.jzdevelopers.cstracker.user.controller.crud

import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.settings.Theme.Companion.getAppStatusBarColor
import dev.jzdevelopers.cstracker.settings.Theme.Companion.getAppTheme
import dev.jzdevelopers.cstracker.user.common.UserTheme
import dev.jzdevelopers.cstracker.user.common.UserTheme.*
import dev.jzdevelopers.cstracker.user.models.PrimaryUser
import dev.jzdevelopers.cstracker.user.models.SecondaryUser
import kotlinx.android.synthetic.main.ui_secondary_user_add_edit.*
import java.util.Locale.getDefault

/** Android Activity SecondaryUserAdd,
 *  Activity That Adds A Secondary-User Profile To The Signed-In Primary User
 *  @author Jordan Zimmitti, Marcus Novoa
 */
class SecondaryUserAdd: JZActivity() {

    //<editor-fold desc="Class Variables">

    // Define And Initialize Int Value//
    private val userIconCode = 0

    // Define And Initialize Drawable Variable//
    private var profileImageDrawable: Drawable? = null

    //</editor-fold>

    /**.
     * What Happens When The Activity Is Created
     */
    override fun createActivity() {

        // Creates The UI//
        createUI(R.layout.ui_secondary_user_add_edit) {

            // Sets The Theme//
            val theme = getAppTheme(this@SecondaryUserAdd)
            theme(theme)

            // Sets The Status Bar Color And Icon Color//
            val statusBarColor = getAppStatusBarColor(this@SecondaryUserAdd)
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

        // When Back Is Clicked//
        clickBack {

            // Shows The Error Dialog//
            MaterialDialog(this).show {
                title(R.string.title_double_check)
                message(R.string.positive_no_save)
                cancelOnTouchOutside(false)
                cornerRadius(16.0f)
                negativeButton(R.string.button_negative)
                positiveButton(R.string.button_positive) {

                    // Exits The SecondaryUserAdd Activity//
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
            if (goalEditText.isNotBlank())  goal  = goalEditText.toInt()
            if (gradeEditText.isNotBlank()) grade = gradeEditText.toInt()

            // Gets The Inputted String Data//
            val firstName     = firstName.text.toString()
            val lastName      = lastName.text.toString()
            val nameLetter    = nameLetter.text.toString()
            val organization  = organization.text.toString()
            val primaryUserId = PrimaryUser.getId(this)
            val theme         = getPickedTheme()

            // Creates The New Secondary User//
            val secondaryUser = SecondaryUser(
                this,
                firstName,
                lastName,
                theme,
                goal,
                0,
                grade,
                nameLetter,
                organization,
                primaryUserId,
                "0:00"
            )

            // Adds The Secondary User To The Database//
            val isSuccessful  = secondaryUser.add(progressBar, profileImageDrawable)
            if (!isSuccessful) return@click

            // Exits The SecondaryUserAdd Activity//
            exitActivity(R.anim.faze_in, R.anim.faze_out)
        }

        // When profileImage Is Clicked//
        click(profileImage) {
            val pickIcon = Intent(Intent.ACTION_PICK, INTERNAL_CONTENT_URI)
            startActivityResult(pickIcon, userIconCode) {requestCode, _, image ->

                // When A Picture Was Not Picked Successfully//
                if (requestCode != userIconCode || image == null)
                    return@startActivityResult

                // Hides The Name Letter//
                nameLetter.visibility = View.INVISIBLE

                // Shows The Profile-Image To The User//
                profileImage.setImageURI(image.data)
                profileImage.borderColor = getColorCompat(R.color.transparent)

                // Sets The Picked Image Drawable//
                profileImageDrawable = profileImage.drawable
            }
        }

        // When profileImage Is Long Clicked//
        longClick(profileImage) {

            // Clears The Image//
            profileImage.setImageResource(R.color.transparent)
            profileImage.borderColor = getColorAttr(R.attr.colorPrimary)
            profileImageDrawable = null

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
                DEFAULT.ordinal -> textUserTheme.setText(R.string.user_theme_default_text)
                RED.ordinal     -> textUserTheme.setText(R.string.user_theme_red_text)
                ORANGE.ordinal  -> textUserTheme.setText(R.string.user_theme_orange_text)
                YELLOW.ordinal  -> textUserTheme.setText(R.string.user_theme_yellow_text)
                GREEN.ordinal   -> textUserTheme.setText(R.string.user_theme_green_text)
                BLUE.ordinal    -> textUserTheme.setText(R.string.user_theme_blue_text)
                INDIGO.ordinal  -> textUserTheme.setText(R.string.user_theme_indigo_text)
                VIOLET.ordinal  -> textUserTheme.setText(R.string.user_theme_violet_text)
                PINK.ordinal    -> textUserTheme.setText(R.string.user_theme_pink_text)
                TEAL.ordinal    -> textUserTheme.setText(R.string.user_theme_teal_text)
                BROWN.ordinal   -> textUserTheme.setText(R.string.user_theme_brown_text)
                BLACK.ordinal   -> textUserTheme.setText(R.string.user_theme_black_text)
            }
        }

        // When The firstName Text Changes//
        textCurrentChange(firstName) {text, _, _, _ ->

            // Gets The First Letter Inputted//
            if (text.isBlank()) return@textCurrentChange
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