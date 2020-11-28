package dev.jzdevelopers.cstracker.libs

import android.content.Context
import androidx.core.content.edit

/** Kotlin Object JZPrefs
 *  Object That Streamlines The Getting And Saving Of Shared Preferences
 *  @author Jordan Zimmitti
 */
@Suppress("unused")
object JZPrefs {

    /**.
     * Function That Gets A Saved Preference Of Type Boolean
     * @return The boolean preference
     */
    fun getPref(context: Context, prefId: String, default: Boolean): Boolean {

        // Returns The Boolean Preference//
        return context.getSharedPreferences(prefId, 0).getBoolean(prefId, default)
    }

    /**.
     * Function That Gets A Saved Preference Of Type Float
     * @return The float preference
     */
    fun getPref(context: Context, prefId: String, default: Float): Float {

        // Returns The Float Preference//
        return context.getSharedPreferences(prefId, 0).getFloat(prefId, default)
    }

    /**.
     * Function That Gets A Saved Preference Of Type Int
     * @return The int preference
     */
    fun getPref(context: Context, prefId: String, default: Int): Int {

        // Returns The Int Preference//
        return context.getSharedPreferences(prefId, 0).getInt(prefId, default)
    }

    /**.
     * Function That Gets A Saved Preference Of Type Long
     * @return The long preference
     */
    fun getPref(context: Context, prefId: String, default: Long): Long {

        // Returns The Long Preference//
        return context.getSharedPreferences(prefId, 0).getLong(prefId, default)
    }

    /**.
     * Function That Gets A Saved Preference Of Type String Set
     * @return The string set preference
     */
    fun getPref(context: Context, prefId: String, default: MutableSet<String>): MutableSet<String>? {

        // Returns The String Set Preference//
        return context.getSharedPreferences(prefId, 0).getStringSet(prefId, default)
    }

    /**.
     * Function That Gets A Saved Preference Of Type String
     * @return The string preference
     */
    fun getPref(context: Context, prefId: String, default: String): String? {

        // Returns The String Preference//
        return context.getSharedPreferences(prefId, 0).getString(prefId, default)
    }


    /**.
     * Function That Saves A Preference Of Type Boolean
     * @param [context] The instance from the caller class
     * @param [prefId]  The unique id for the preference
     * @param [pref]    The preference of type boolean
     */
    fun savePref(context: Context, prefId: String, pref: Boolean) {

        // Saves The Boolean Preference//
        context.getSharedPreferences(prefId, 0).edit {

            // Clears The Old Saved Preference And Replaces It With The New One//
            clear()
            putBoolean(prefId, pref)
            apply()
        }
    }

    /**.
     * Function That Saves A Preference Of Type Float
     * @param [context] The instance from the caller class
     * @param [prefId]  The unique id for the preference
     * @param [pref]    The preference of type float
     */
    fun savePref(context: Context, prefId: String, pref: Float) {

        // Saves The Float Preference//
        context.getSharedPreferences(prefId, 0).edit {

            // Clears The Old Saved Preference And Replaces It With The New One//
            clear()
            putFloat(prefId, pref)
            apply()
        }
    }

    /**.
     * Function That Saves A Preference Of Type Int
     * @param [context] The instance from the caller class
     * @param [prefId]  The unique id for the preference
     * @param [pref]    The preference of type int
     */
    fun savePref(context: Context, prefId: String, pref: Int) {

        // Saves The Int Preference//
        context.getSharedPreferences(prefId, 0).edit {

            // Clears The Old Saved Preference And Replaces It With The New One//
            clear()
            putInt(prefId, pref)
            apply()
        }
    }

    /**.
     * Function That Saves A Preference Of Type Long
     * @param [context] The instance from the caller class
     * @param [prefId]  The unique id for the preference
     * @param [pref]    The preference of type long
     */
    fun savePref(context: Context, prefId: String, pref: Long) {

        // Saves The Long Preference//
        context.getSharedPreferences(prefId, 0).edit {

            // Clears The Old Saved Preference And Replaces It With The New One//
            clear()
            putLong(prefId, pref)
            apply()
        }
    }

    /**.
     * Function That Saves A Preference Of Type String Set
     * @param [context] The instance from the caller class
     * @param [prefId]  The unique id for the preference
     * @param [pref]    The preference of type string set
     */
    fun savePref(context: Context, prefId: String, pref: MutableSet<String>) {

        // Saves The String Set Preference//
        context.getSharedPreferences(prefId, 0).edit {

            // Clears The Old Saved Preference And Replaces It With The New One//
            clear()
            putStringSet(prefId, pref)
            apply()
        }
    }

    /**.
     * Function That Saves A Preference Of Type String
     *
     * @param [context] The instance from the caller class
     * @param [prefId]  The unique id for the preference
     * @param [pref]    The preference of type string
     */
    fun savePref(context: Context, prefId: String, pref: String) {

        // Saves The String Preference//
        context.getSharedPreferences(prefId, 0).edit {

            // Clears The Old Saved Preference And Replaces It With The New One//
            clear()
            putString(prefId, pref)
            apply()
        }
    }
}