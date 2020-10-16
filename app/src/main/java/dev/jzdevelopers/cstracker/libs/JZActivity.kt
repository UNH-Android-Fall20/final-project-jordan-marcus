package dev.jzdevelopers.cstracker.libs

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomappbar.BottomAppBar
import dev.jzdevelopers.cstracker.R
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import android.os.Build.VERSION_CODES.M as ANDROID_M
import android.os.Build.VERSION_CODES.Q as ANDROID_Q
import android.os.Build.VERSION_CODES.R as ANDROID_R

// Type Aliases For Lambda Functions//
private typealias Click         = suspend ()                          -> Unit
private typealias ClickBack     = suspend ()                          -> Unit
private typealias ClickRecycler = suspend (position: Int, view: View) -> Unit
private typealias LongClick     = suspend ()                          -> Unit

/** Kotlin Abstract Class JZActivity
 *  Abstract Class That Streamlines Various Android Activity Functions
 *  @author Jordan Zimmitti
 */
@Suppress("unused")
abstract class JZActivity: AppCompatActivity() {

    //<editor-fold desc="Class Variables">

    // Define Menu Variable For Late Initialization//
    protected lateinit var menu: Menu

    // Define And Initialize ClickBack Variable//
    private var clickBack: ClickBack? = null

    // Define And Initialize Function Value//
    private val empty: UI.() -> Unit = {}

    //</editor-fold>


    /**.
     * Configures Static Variables And Functions
     */
    companion object {

        /**.
         * Function That Shows An Error Dialog
         * @param [context] Gets the instance from the caller activity
         * @param [title]   the title of the error dialog
         * @param [error]   The error message for the error dialog
         */
        fun showGeneralDialog(context: Context, @StringRes title: Int, @StringRes error: Int) {

            // Shows The Error Dialog//
            MaterialDialog(context).show {
                title(title)
                message(error)
                negativeButton(R.string.negative_button_only)
            }
        }
    }


    /**.
     * Runs When The Activity Is Created
     * @param [savedInstanceState] Previously saved instance data
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls Starting Functions//
        createActivity()
        createListeners()

        // Handles API Calls//
        lifecycleScope.launch { apiCalls() }
    }

    /**.
     * Runs When A Certain Key Is Pressed
     * @param [keyCode] Type of key
     * @param [event]   The key pressed
     * @return The invoked function for the clicked key
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        super.onKeyDown(keyCode, event)

        // Handles Back Button Click Listener//
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                lifecycleScope.launch { clickBack?.invoke() }
                false
            }
            else -> false
        }
    }


    /**.
     * Abstract Function That Runs When The Activity Is Created
     */
    protected abstract fun createActivity()

    /**.
     * Abstract Function That Handles All Listeners For The Activity
     */
    protected abstract fun createListeners()

    /**.
     * Abstract Function That Handles All API Calls For The Activity
     */
    protected open suspend fun apiCalls() {}


    /**.
     * Function That Handles When A Recycler Adapter Is Clicked
     * @param [adapters]        Any type of recycler adapter
     * @param [clickedRecycler] The invoked function when the recycler adapter is clicked (lambda)
     */
    protected fun click(vararg adapters: JZRecyclerAdapter<*>, clickedRecycler: ClickRecycler) {

        // Sets The Click Listener//
        for (adapter in adapters) adapter.onItemClick { position, view ->
            lifecycleScope.launch { clickedRecycler.invoke(position, view) }
        }
    }

    /**.
     * Function That Handles When A MenuItem Is Clicked
     * @param [items]   Any Menu Item
     * @param [clicked] The invoked function for when the menu item is clicked (lambda)
     */
    protected fun click(vararg items: MenuItem, clicked: Click) {

        // Sets The Click Listener For All Menu Items//
        for (item in items) item.setOnMenuItemClickListener {
            lifecycleScope.launch { clicked.invoke() }
            true
        }
    }

    /**.
     * Function That Handles When A View Is Clicked
     * @param [views]   Any type of view
     * @param [clicked] The invoked function for when the view is clicked (lambda)
     */
    protected fun click(vararg views: View, clicked: Click) {

        // Sets The Click Listener For All Views//
        for (view in views) view.setOnClickListener {
            lifecycleScope.launch { clicked.invoke() }
        }
    }

    /**.
     * Function That Handles When A Layout Is Clicked
     * @param [layouts] Any type of layout
     * @param [clicked] The invoked function for when the layout is clicked (lambda)
     */
    protected fun click(vararg layouts: ViewGroup, clicked: Click) {

        // Sets The Click Listener For All Layouts//
        for (layout in layouts) layout.setOnClickListener {
            lifecycleScope.launch { clicked.invoke() }
        }
    }

    /**.
     * Function That Handles When The System Back Button Is Clicked
     * @param [clickBack] The invoked function for when the back button is clicked
     */
    protected fun clickBack(clickBack: ClickBack) {this.clickBack = clickBack}

    /**.
     * Function That Handles When A Layout Is Long Clicked
     * @param [layouts]     Any type of layout
     * @param [longClicked] The invoked function for when the layout is Long clicked
     */
    protected fun longClick(vararg layouts: ViewGroup, longClicked: LongClick) {

        // Sets The Long Click Listener//
        for (layout in layouts) layout.setOnLongClickListener {
            lifecycleScope.launch { longClicked.invoke() }
            true
        }
    }

    /**.
     * Function That Handles When A View Is Long Clicked
     * @param [views]       Any type of view
     * @param [longClicked] The invoked function for when the view is long clicked
     */
    protected fun longClick(vararg views: View, longClicked: LongClick) {

        // Sets The Long Click Listener//
        for (view in views) view.setOnLongClickListener {
            lifecycleScope.launch { longClicked.invoke() }
            true
        }
    }


    /**.
     * Function That Shows The Layout And Theme Configurations
     * @param [layout] The layout resource id for the activity
     * @param [func]   The ui configurations
     */
    protected fun createUI(@LayoutRes layout: Int, func: UI.() -> Unit = empty) {

        // Shows The Layout And Theme Configs//
        if (func === empty) setContentView(layout)
        else {
            setContentView(layout)
            UI(layout).func()
        }
    }

    /**.
     * Function That Correctly Exits The Activity
     * @param [isAnimation] Checks whether to exit with the default animation
     */
    protected fun exitActivity(isAnimation: Boolean) {

        // Finishes With Default Animation//
        if (isAnimation) {finish(); return}

        // Finishes With No Animation//
        finish()
        overridePendingTransition(0, 0)
    }

    /**.
     * Function That Correctly Exits The Activity
     * @param [animIn]  The custom start animation
     * @param [animOut] The custom end animation
     */
    protected fun exitActivity(animIn: Int, animOut: Int) {

        // Finishes With A Custom Animation//
        finish()
        overridePendingTransition(animIn, animOut)
    }

    /**.
     * Function That Correctly Exits The Application
     */
    protected fun exitApp() {

        // Finishes The Application//
        val killApp = Intent(Intent.ACTION_MAIN)
        killApp.addCategory(Intent.CATEGORY_HOME)
        killApp.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(killApp)
    }

    /**.
     * Function That Gets A [color] Resource id From The Colors.xml File In The Res Folder
     * @return The color
     */
    protected fun getColorCompat(@ColorRes color: Int): Int {
        return getColor(this, color)
    }

    /**.
     * Replaces The BottomBar Menu UI With A New One
     * @param [bottomBar] The bottomBar for the activity
     * @param [menu]      The new menu ui resource id for the activity
     */
    protected fun replaceMenu(bottomBar: BottomAppBar, @MenuRes menu: Int) {

        // Replaces The Current Menu With The New One//
        bottomBar.replaceMenu(menu)
        this.menu = bottomBar.menu
    }

    /**.
     * Function That Starts A New Activity
     * @param [activity]    An android activity kotlin Class
     * @param [isAnimation] Checks whether to use the default animation
     */
    protected fun startActivity(activity: KClass<*>, isAnimation: Boolean) {

        // Define And Instantiate Intent Value//
        val newActivity = Intent(this, activity.java)

        // Starts The Activity With The Default Animation//
        if (isAnimation) {
            startActivity(newActivity);
            return
        }

        // Removes The Activity Animation And Starts The Activity//
        newActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivityForResult(newActivity, 0)
        overridePendingTransition(0, 0)
    }

    /**.
     * Function That Starts A New Activity
     * @param [activity] An android activity kotlin Class
     * @param [animIn]   The custom start animation
     * @param [animOut]  The custom end animation
     */
    protected fun startActivity(activity: KClass<*>, animIn: Int, animOut: Int) {

        // Define And Instantiate Intent Value//
        val newActivity = Intent(this, activity.java)

        // Starts The New Activity With The Custom Animation//
        startActivity(newActivity)
        overridePendingTransition(animIn, animOut)
    }


    /** Kotlin Inner Class UI
     *  Inner Class For Easily Configuring UI Options
     *  @author Jordan Zimmitti
     */
    protected inner class UI(@LayoutRes private val layout: Int) {

        /**.
         * Function That Creates The Menu For The Activity
         * @param [bottomBar]    The bottomBar for the activity
         * @param [activityMenu] The menu ui resource id for the activity
         */
        fun menu(bottomBar: BottomAppBar, @MenuRes activityMenu: Int) {

            // Sets The MainActivity Menu For The Activity//
            bottomBar.replaceMenu(activityMenu)
            menu = bottomBar.menu
        }

        /**.
         * Function That Sets The Custom Navigation Bar Color For The Activity
         * @param [color] The color resource
         */
        fun navigationColor(@ColorRes color: Int, isDarkNavBar: Boolean = true) {

            // Sets The Custom Navigation Bar Color//
            window.navigationBarColor = getColorCompat(color)

            // When The Nav Bar Is A Dark Color
            if (isDarkNavBar) return

            @Suppress("DEPRECATION")
            @SuppressLint("InlinedApi")
            if (SDK_INT in Build.VERSION_CODES.O..ANDROID_Q) {

                // Enables Dark Nav Bar Icons//
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            else if (SDK_INT >= ANDROID_R) {

                // Enables Dark Nav Bar Icons//
                window.insetsController?.setSystemBarsAppearance(
                    APPEARANCE_LIGHT_NAVIGATION_BARS,
                    APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            }
        }

        /**.
         * Function That Creates A Custom Title For The Activity
         * @param [textView] The TextView being used for the activity title
         * @param [title]    The title resource id for the activity
         */
        fun title(textView: TextView, @StringRes title: Int) {textView.text = getString(title)}
        fun title(@StringRes title: Int) {setTitle(title)}

        /**.
         * Function That Sets A Custom Theme For The Activity.
         * It must go before every other function in createUI
         * @param [theme]           The style resource id for the activity
         * @param [isDarkStatusBar] Checks whether the status bar color is a dark color
         */
        fun theme(@StyleRes theme: Int, isDarkStatusBar: Boolean? = null) {

            // Sets The Theme And Layout//
            setTheme(theme)
            setContentView(layout)

            when {

                // Their Is No Preference Set//
                isDarkStatusBar == null -> {}

                // When The Status Bar Icons Should Be Black//
                !isDarkStatusBar -> {

                    @Suppress("DEPRECATION")
                    if (SDK_INT in ANDROID_M..ANDROID_Q) {

                        // Enables Dark Status Bar Icons//
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                    else if (SDK_INT >= ANDROID_R) {

                        // Enables Dark Status Bar Icons//
                        window.insetsController?.setSystemBarsAppearance(
                            APPEARANCE_LIGHT_STATUS_BARS,
                            APPEARANCE_LIGHT_STATUS_BARS
                        )
                    }
                }

                // When The Status Bar Icons Should Be White And Color Should Be Primary Dark//
                isDarkStatusBar -> {

                    // Define And Initialize TypedValue Value//
                    val typedValue = TypedValue()

                    // Gets The Primary Attribute Color For The Current Theme//
                    this@JZActivity.theme.resolveAttribute(
                        R.attr.colorPrimary,
                        typedValue,
                        true
                    )

                    // Define And Initialize Int primaryColor//
                    val primaryColor = typedValue.data

                    // Sets The Theme's Primary Color//
                    window.statusBarColor = primaryColor
                }
            }
        }
    }
}