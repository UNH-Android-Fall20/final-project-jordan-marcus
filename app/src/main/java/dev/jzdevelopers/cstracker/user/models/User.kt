package dev.jzdevelopers.cstracker.user.models

import android.content.Context
import android.widget.ProgressBar
import com.google.firebase.firestore.Exclude
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.common.FireBaseModel
import dev.jzdevelopers.cstracker.libs.JZActivity
import dev.jzdevelopers.cstracker.user.common.UserTheme
import java.util.*

/** Kotlin Abstract Class User,
 *   Class That Handles Common User Functions/Properties
 *   @author Jordan Zimmitti, Marcus Novoa
 *   @param [context]   Gets the instance from the caller activity
 *   @param [firstName] The first-name of the user
 *   @param [lastName]  The last-name of the user
 *   @param [theme]     The theme for the user
 */
abstract class User(
    @get:Exclude var context : Context? = null,
    var firstName            : String,
    var lastName             : String,
    var theme                : UserTheme,
): FireBaseModel() {

    /**.
     * Base Function For Adding A User To The Database
     * @param [loadingBar] Circular progress bar to alert the user when the addition is in progress
     * @return Whether the user was added successfully
     */
    override suspend fun add(loadingBar: ProgressBar): Boolean {

        // Checks If The User Input Is Valid//
        if (!isValidFirstName()) return false
        if (!isValidLastName())  return false
        return true
    }

    /**.
     * Base Function For Editing A User In The Database
     * @param [id]         The id of the user
     * @param [loadingBar] Circular progress bar to alert the user when the edit is in progress
     * @return Whether the user was edited successfully
     */
    override suspend fun edit(id: String, loadingBar: ProgressBar): Boolean {

        // Checks If The User Input Is Valid//
        if (!isValidFirstName()) return false
        if (!isValidLastName())  return false
        return true
    }

    /**.
     * Abstract Function For Deleting A User In The Database
     * @param [id]         The id of the user
     * @return Whether the user was deleted successfully
     */
    override suspend fun delete(id: String): Boolean {
        return false
    }

    /**.
     * Function That Shows A General Database Error Dialog
     */
    protected fun showGeneralError() {

        // Gets The Context If It Exists//
        val context = context ?: throw NullPointerException("Context must not be null")

        // Shows The Error Dialog//
        JZActivity.showGeneralDialog(
            context,
            R.string.title_error,
            R.string.error_general
        )
    }

    /**.
     * Function That Checks And Formats The First-Name For Validity
     * @return Whether the first-name is valid
     */
    private fun isValidFirstName(): Boolean {

        // Gets The Context If It Exists//
        val context = context ?: throw NullPointerException("Context must not be null")

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
                    .toLowerCase(Locale.getDefault())
                    .capitalize(Locale.getDefault())
                true
            }
        }
    }

    /**.
     * Function That Checks And Formats The Last-Name For Validity
     * @return Whether the last-name is valid
     */
    private fun isValidLastName(): Boolean {

        // Gets The Context If It Exists//
        val context = context ?: throw NullPointerException("Context must not be null")

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
                    .toLowerCase(Locale.getDefault())
                    .capitalize(Locale.getDefault())
                true
            }
        }
    }
}