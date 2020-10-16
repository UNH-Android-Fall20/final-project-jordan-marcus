package dev.jzdevelopers.cstracker.user.oject

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.libs.JZActivity
import java.util.*
import kotlin.collections.HashMap

/** Abstract Class User
 *  Abstract Class That Handles Common User Features
 *  @author Jordan Zimmitti, Marcus Novoa
 */
abstract class User(
    var firstName : String = "",
    var lastName  : String = "",
) {

    //<editor-fold desc="Class Variables">

    // Defines The User Id Variable//
    var id : String = ""
        protected set

    // Gets The Different FireBase Instances//
    protected val firebaseAuth = FirebaseAuth.getInstance()
    protected val fireStore    = FirebaseFirestore.getInstance()

    // Define And Instantiates HashMap Values//
    protected val userToSave   = HashMap<String, Any>()
    protected val userToUpdate = HashMap<String, Any>()

    //</editor-fold>

    /**.
     * Configures Static Variables And Functions
     */
    companion object {

        // Gets The FireBase Authorization Instances//
        private val firebaseAuth = FirebaseAuth.getInstance()

        /**.
         * Function That Checks If The User Is Logged In
         */
        fun isSignedIn(): Boolean {

            // Gets The State Of The Signed In User//
            val user = firebaseAuth.currentUser
            return user != null
        }
    }

    /**.
     * Base Function That Saves A New User To The Database
     * @param [context] Gets the instance from the caller activity
     * @return Whether all the base user properties are valid and ready to be saved
     */
    protected open fun signUp(context: Context): Boolean {

        // Checks If The User Input Is Valid//
        if (!isValidFirstName(context)) return false
        if (!isValidLastName(context))  return false

        // Takes The User Data And Prepares It For The Database//
        userToSave["firstName"] = firstName
        userToSave["lastName"]  = lastName
        return true
    }

    /**.
     * Function That Shows A General Error Dialog
     * @param [context] Gets the instance from the caller activity
     */
    protected fun showGeneralError(context: Context) {

        // Shows The Error Dialog//
        JZActivity.showGeneralDialog(
            context,
            R.string.title_user_sign_up_error,
            R.string.error_general
        )
    }

    /**.
     * Function That Checks And Formats The First-Name For Validity
     * @param [context] Gets the instance from the caller activity
     * @return whether the first-name is valid
     */
    private fun isValidFirstName(context: Context): Boolean {

        // Checks The First-Name For Validity//
        return when {

            // When The First-Name Is Blank//
            firstName.isBlank() -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_user_sign_up_error,
                    R.string.error_first_name_blank
                )
                false
            }

            // When First-Name Contains A Symbol//
            !firstName.matches(Regex("[a-zA-Z]+")) -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_user_sign_up_error,
                    R.string.error_first_name_symbol
                )
                false
            }

            // When First-Name Has A Length Greater Than Twenty//
            firstName.length > 20 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_user_sign_up_error,
                    R.string.error_first_name_long
                )
                false
            }

            // When The First-Name Is Valid//
            else -> {
                firstName = firstName
                    .trim()
                    .toLowerCase(Locale.getDefault())
                    .capitalize(Locale.getDefault())
                true
            }
        }
    }

    /**.
     * Function That Checks And Formats The Last-Name For Validity
     * @param [context] Gets the instance from the caller activity
     * @return whether the last-name is valid
     */
    private fun isValidLastName(context: Context): Boolean {

        // Checks The Last-Name For Validity//
        return when {

            // When The Last-Name Is Blank//
            lastName.isBlank() -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_user_sign_up_error,
                    R.string.error_last_name_blank
                )
                false
            }

            // When The Last-Name Contains A Symbol//
            !lastName.matches(Regex("[a-zA-Z]+")) -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_user_sign_up_error,
                    R.string.error_last_name_symbol
                )
                false
            }

            // When The Last-Name Has A Length Greater Than Twenty//
            lastName.length > 40 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_user_sign_up_error,
                    R.string.error_last_name_long
                )
                false
            }

            // When The Last-Name Is Valid//
            else -> {
                lastName = lastName
                    .trim()
                    .toLowerCase(Locale.getDefault())
                    .capitalize(Locale.getDefault())
                true
            }
        }
    }
}