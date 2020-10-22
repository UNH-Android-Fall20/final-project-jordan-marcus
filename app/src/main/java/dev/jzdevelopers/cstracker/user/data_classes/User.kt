package dev.jzdevelopers.cstracker.user.data_classes

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.UserTheme
import java.util.Locale.getDefault

/** Kotlin Abstract Class User,
 *   Class That Handles Common User Properties
 *   @author Jordan Zimmitti, Marcus Novoa
 *   @param [context]   The instance from the caller activity
 *   @param [firstName] The first-name of the user
 *   @param [lastName]  The last-name of the user
 *   @param [theme]     The theme for the user
 */
abstract class User(
    val context   : Context,
    var firstName : String,
    var lastName  : String,
    var theme     : UserTheme
) {

    //<editor-fold desc="Class Variables">

    /**
     * The Id Of The User
     */
    var id : String = ""
        protected set

    // Gets The Different FireBase Instances//
    protected val firebaseAuth  = FirebaseAuth.getInstance()
    protected val fireStore     = FirebaseFirestore.getInstance()
    private   val fireStorage   = FirebaseStorage.getInstance()

    // Get Storage Reference From FireBase//
    protected val storage = fireStorage.getReferenceFromUrl("gs://cs-tracker-5b4d1.appspot.com")

    // Define And Instantiates HashMap Values//
    protected val userToSave   = HashMap<String, Any>()
    protected val userToUpdate = HashMap<String, Any>()

    //</editor-fold>

    /**.
     * Configures Static Functions And Variables
     */
    companion object {

        // Gets The FireBase Authorization Instances//
        private val firebaseAuth = FirebaseAuth.getInstance()

        /**.
         * Function That Checks Whether The User Is Signed-In
         * @return Whether the user is signed-in
         */
        fun isSignedIn(): Boolean {
            val user = firebaseAuth.currentUser
            return user != null
        }
    }

    /**.
     * Function That Shows A General Error Dialog
     */
    protected fun showGeneralError() {

        // Shows The Error Dialog//
        JZActivity.showGeneralDialog(
            context,
            R.string.title_error,
            R.string.error_general
        )
    }

    /**.
     * Base Function That Saves A New User To The Database
     * @return Whether all the base user properties are valid and ready to be saved
     */
    protected open fun signUp(): Boolean {

        // Checks If The User Input Is Valid//
        if (!isValidFirstName()) return false
        if (!isValidLastName())  return false

        // Takes The User Data And Prepares It For The Database//
        userToSave["firstName"] = firstName
        userToSave["lastName"]  = lastName
        userToSave["theme"]     = theme
        return true
    }

    /**.
     * Base Function That Updates A User Saved In The Database
     * @return Whether all the base user properties are valid and ready to be updated
     */
    protected open fun update(): Boolean {

        // Checks If The User Input Is Valid//
        if (!isValidFirstName()) return false
        if (!isValidLastName())  return false

        // Takes The User Data And Prepares It For The Database//
        userToUpdate["firstName"] = firstName
        userToUpdate["lastName"]  = lastName
        userToUpdate["theme"]     = theme
        return true
    }

    /**.
     * Function That Checks And Formats The First-Name For Validity
     * @return Whether the first-name is valid
     */
    private fun isValidFirstName(): Boolean {

        // Checks The First-Name For Validity//
        return when {

            // When The First-Name Is Blank//
            firstName.isBlank() -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_first_name_blank
                )
                false
            }

            // When The First-Name Contains A Symbol//
            !firstName.matches(Regex("[a-zA-Z]+")) -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_first_name_symbol
                )
                false
            }

            // When The First-Name Has A Length Greater Than Twenty//
            firstName.length > 20 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_first_name_long
                )
                false
            }

            // When The First-Name Is Valid//
            else -> {
                firstName = firstName
                    .trim()
                    .toLowerCase(getDefault())
                    .capitalize(getDefault())
                true
            }
        }
    }

    /**.
     * Function That Checks And Formats The Last-Name For Validity
     * @return Whether the last-name is valid
     */
    private fun isValidLastName(): Boolean {

        // Checks The Last-Name For Validity//
        return when {

            // When The Last-Name Is Blank//
            lastName.isBlank() -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_last_name_blank
                )
                false
            }

            // When The Last-Name Contains A Symbol//
            !lastName.matches(Regex("[a-zA-Z]+")) -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_last_name_symbol
                )
                false
            }

            // When The Last-Name Has A Length Greater Than Twenty//
            lastName.length > 20 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_last_name_long
                )
                false
            }

            // When The Last-Name Is Valid//
            else -> {
                lastName = lastName
                    .trim()
                    .toLowerCase(getDefault())
                    .capitalize(getDefault())
                true
            }
        }
    }
}