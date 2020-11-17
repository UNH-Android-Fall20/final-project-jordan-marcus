package dev.jzdevelopers.cstracker.event.models

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import dev.jzdevelopers.cstracker.R
import dev.jzdevelopers.cstracker.common.FireBaseModel
import dev.jzdevelopers.cstracker.libs.JZActivity
import kotlinx.coroutines.tasks.await
import java.text.DateFormat
import java.util.*
import java.util.regex.Pattern

/** Kotlin Class Event,
 *  Class That Handles Event Functions/Properties
 *  @author Jordan Zimmitti, Marcus Novoa
 *  @param [context]           Gets the instance from the caller activity
 *  @param [date]              The date of the event (MM-DD-YYYY)
 *  @param [endTime]           The last recorded end time of the event service hours
 *  @param [location]          The location of the event
 *  @param [name]              The name of the event
 *  @param [notes]             Any notes worth mentioning for the event
 *  @param [peopleInCharge]    The people in charge of the event
 *  @param [phoneNumber]       The contact phone number for the event
 *  @param [startTime]         The start time of the event service hours
 *  @param [userId]            The id of the user that the event belongs to
 */
class Event(
    val context        : Context? = null,
    var date           : String   = "",
    var endTime        : String   = "0:00",
    var location       : String   = "",
    var name           : String   = "",
    var notes          : String   = "",
    var peopleInCharge : String   = "",
    var phoneNumber    : String   = "000-000-0000",
    var startTime      : String   = "0:00",
    val userId         : String   = ""
): FireBaseModel() {

    /**.
     * Function That Adds An Event To The Database
     * @param [loadingBar] Circular progress bar to alert the user when the addition is in progress
     * @return Whether the event was added successfully
     */
    override suspend fun add(loadingBar: ProgressBar): Boolean {
        try {

            // When Context Is Null//
            if (context == null) {

                // Throws A Runtime Error//
                throw NullPointerException("Context must not be null")
            }

            // Checks If The Event Input Is Valid//
            if (!isValidName()) return false
//            if (!isValidDate()) return false
            if (!isValidLocation()) return false
            if (!isValidPeopleInCharge()) return false
            if (!isValidPhoneNumber()) return false
            if (!isValidNotes()) return false
            // !isValidUserId

            // Shows The Loading Bar//
            loadingBar.visibility = View.VISIBLE

            // Adds The Event Data To The Database//
            fireStore.collection("Events").add(this).await()

            // Hides The Loading Bar//
            loadingBar.visibility = View.GONE

            // Logs That The Event Was Added Successfully//
            Log.v("Event", "Event [$name] has been added to user id: $userId")
            return true
        }
        catch (e: Exception) {
            e.printStackTrace()
            loadingBar.visibility = View.GONE
            showGeneralError()
            return false
        }
    }

    /**.
     * Function That Edits An Event In The Database
     * @param [id]         The id of the event
     * @param [loadingBar] Circular progress bar to alert the user when the edit is in progress
     * @return Whether the event was edited successfully
     */
    override suspend fun edit(id: String, loadingBar: ProgressBar): Boolean {

        // Checks If The Event Input Is Valid//
        if (!isValidName()) return false
        if (!isValidDate()) return false
        if (!isValidLocation()) return false
        if (!isValidPeopleInCharge()) return false
        if (!isValidPhoneNumber()) return false
        if (!isValidNotes()) return false
        // !isValidUserId

        return true
    }

    /**.
     * Function That Deletes An Event In The Database
     * @param [id]         The id of the event
     * @return Whether the event was deleted successfully
     */
    override suspend fun delete(id: String): Boolean {
        return false
    }

    /**.
     * Function That Shows A General Database Error Dialog
     */
    protected fun showGeneralError() {

        // When Context Is Null//
        if (context == null) {

            // Throws A Runtime Error//
            throw NullPointerException("Context must not be null")
        }

        // Shows The Error Dialog//
        JZActivity.showGeneralDialog(
            context,
            R.string.title_error,
            R.string.error_general
        )
    }

    /**.
     * Function That Checks The Event Date For Validity
     * @return Whether the event date is valid
     */
    private fun isValidDate(): Boolean {

        // When Context Is Null//
        if (context == null) {

            // Throws A Runtime Error//
            throw NullPointerException("Context must not be null")
        }

        // Reformat The Event Date To A String//
        val dateString: String = date.toString()
        val df: DateFormat = DateFormat.getDateInstance()
        df.isLenient = false

        // Checks The Event Date For Validity//
        return try {

            // Successfully Parse The Date String//
            var d = df.parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**.
     * Function That Checks The Event Location For Validity
     * @return Whether the event location is valid
     */
    private fun isValidLocation(): Boolean {

        // When Context Is Null//
        if (context == null) {

            // Throws A Runtime Error//
            throw NullPointerException("Context must not be null")
        }

        // Checks The Event Location For Validity//
        return when {

            // When The Event Location Is Blank//
            location.isBlank() -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_event_location_blank
                )
                false
            }

            // When The Event Location Has A Length Greater Than One Hundred//
            location.length > 100 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_event_location_long
                )
                false
            }

            // When The Event Location Is Valid//
            else -> {
                location = location
                    .trim()
                    .replace("\\s+", " ")
                true
            }
        }
    }

    /**.
     * Function That Checks And Formats The Event Name For Validity
     * @return Whether the event name is valid
     */
    private fun isValidName(): Boolean {

        // When Context Is Null//
        if (context == null) {

            // Throws A Runtime Error//
            throw NullPointerException("Context must not be null")
        }

        // Checks The Event Name For Validity//
        return when {

            // When The Event Name Is Blank//
            name.isBlank() -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_event_name_blank
                )
                false
            }

            // When The Event Name Has A Length Greater Than Fifty//
            name.length > 50 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_event_name_long
                )
                false
            }

            // When The Event Name Is Valid//
            else -> {
                name = name
                    .trim()
                    .replace("\\s+", " ")
                    .toLowerCase(Locale.getDefault())
                    .capitalizeWords()
                true
            }
        }
    }

    /**.
     * Function That Checks The Event Notes For Validity
     * @return Whether the event notes are valid
     */
    private fun isValidNotes(): Boolean {

        // When Context Is Null//
        if (context == null) {

            // Throws A Runtime Error//
            throw NullPointerException("Context must not be null")
        }

        // Checks The Event Notes For Validity//
        return when {

            // When The Event Notes Have A Length Greater Than One Thousand//
            notes.length > 1000 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_event_notes_long
                )
                false
            }

            // When The Event Notes Are Valid//
            else -> {
                notes = notes
                    .trim()
                    .replace("\\s+", " ")
                true
            }
        }
    }

    /**.
     * Function That Checks The Event People In Charge For Validity
     * @return Whether the event people in charge are valid
     */
    private fun isValidPeopleInCharge(): Boolean {

        // When Context Is Null//
        if (context == null) {

            // Throws A Runtime Error//
            throw NullPointerException("Context must not be null")
        }

        // Checks The Event People In Charge For Validity//
        return when {

            // When The Event People In Charge Are Blank//
            peopleInCharge.isBlank() -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_event_incharge_blank
                )
                false
            }

            // When The Event People In Charge Has A Length Greater Than Fifty//
            peopleInCharge.length > 50 -> {
                JZActivity.showGeneralDialog(
                    context,
                    R.string.title_error,
                    R.string.error_event_incharge_long
                )
                false
            }

            // When The Event People In Charge Are Valid//
            else -> {
                peopleInCharge = peopleInCharge
                    .trim()
                    .replace("\\s+", " ")
                    .toLowerCase(Locale.getDefault())
                    .capitalizeWords()
                true
            }
        }
    }

    /**.
     * Function That Checks The Event Phone Number For Validity
     * @return Whether the event phone number is valid
     */
    private fun isValidPhoneNumber(): Boolean {

        // When Context Is Null//
        if (context == null) {

            // Throws A Runtime Error//
            throw NullPointerException("Context must not be null")
        }

        // Checks The Event Phone Number For Validity//
        return try {

            // Successfully Find The Phone Number Via Regex//
            val reg = "^[+]?[0-9]{10,13}\$"
            val p: Pattern = Pattern.compile(reg)
            p.matcher(phoneNumber).find()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**.
     * Function That Capitalizes Every Word In A String
     * @return New string with the first letter of every word capitalized
     */
    private fun String.capitalizeWords(): String = split(" ")
        .map { it.capitalize(Locale.getDefault()) }
        .joinToString(" ")
}