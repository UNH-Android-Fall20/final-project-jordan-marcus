package dev.jzdevelopers.cstracker.libs

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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
private typealias ActivityResult = (requestCode: Int, resultCode: Int, data: Intent?) -> Unit
private typealias IntentOptions  = (intent: Intent)                                   -> Unit

//<editor-fold desc="Suspend Lambda Type Aliases">
private typealias Click              = suspend ()                                                                 -> Unit
private typealias ClickBack          = suspend ()                                                                 -> Unit
private typealias ClickRecycler      = suspend (position: Int)                                                    -> Unit
private typealias LongClick          = suspend ()                                                                 -> Unit
private typealias LongClickRecycler  = suspend (position: Int)                                                    -> Unit
private typealias ProgressChange     = suspend (seekBar: SeekBar?, progress: Int, isUser: Boolean)                -> Unit
private typealias ProgressTouchStart = suspend (seekBar: SeekBar?)                                                -> Unit
private typealias ProgressTouchStop  = suspend (seekBar: SeekBar?)                                                -> Unit
private typealias QueryChange        = suspend (newText: String?)                                                 -> Unit
private typealias QuerySubmit        = suspend (query: String?)                                                   -> Unit
private typealias SearchOpen         = suspend ()                                                                 -> Unit
private typealias SearchClose        = suspend ()                                                                 -> Unit
private typealias TextAfterChange    = suspend (text: String)                                                     -> Unit
private typealias TextBeforeChange   = suspend (text: String, totalCount: Int, currentCount: Int, newCount: Int)  -> Unit
private typealias TextCurrentChange  = suspend (text: String, totalCount: Int, lastCount: Int, currentCount: Int) -> Unit
//</editor-fold>

/** Kotlin Abstract Class JZActivity
 *  Abstract Class That Streamlines Various Android Activity Functions
 *  @author Jordan Zimmitti
 */
@Suppress("unused", "SameParameterValue")
abstract class JZActivity: AppCompatActivity() {

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
                negativeButton(R.string.button_only)
            }
        }
    }

    //<editor-fold desc="Class Variables">

    // Define Menu Variable For Late Initialization//
    protected lateinit var menu: Menu

    // Define And Initializes ClickBack Variable//
    private var activityResult     : ActivityResult?     = null
    private var clickBack          : ClickBack?          = null
    private var progressChange     : ProgressChange?     = null
    private var progressTouchStart : ProgressTouchStart? = null
    private var progressTouchStop  : ProgressTouchStop?  = null
    private var queryChange        : QueryChange?        = null
    private var querySubmit        : QuerySubmit?        = null
    private var textAfterChange    : TextAfterChange?    = null
    private var textBeforeChange   : TextBeforeChange?   = null
    private var textCurrentChange  : TextCurrentChange?  = null

    // Define And Initializes Lambda Value//
    private val empty: suspend UI.() -> Unit = {}

    //</editor-fold>

    //<editor-fold desc="Override Functions">

    /**.
     * Called To Send Information From One Activity To Another
     * @param [requestCode] The request code allowing you to identify who this result came from
     * @param [resultCode]  The result code returned by the child activity
     * @param [data]        An Intent, which can return result data to the caller
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Sets The Activity Result//
        activityResult?.invoke(requestCode, resultCode, data)
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

    //</editor-fold>

    //<editor-fold desc="Click Listeners">

    /**.
     * Function That Handles When A Recycler-Adapter Is Clicked
     * @param [adapter]         Any type of recycler adapter
     * @param [clickedRecycler] The invoked function when the recycler-adapter is clicked (lambda)
     */
    protected fun click(adapter: JZRecyclerAdapterFB<*>, clickedRecycler: ClickRecycler) {

        // Sets The Click Listener//
        adapter.itemClick { position -> clickedRecycler.invoke(position) }
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
     * @param [clickBack] The invoked function for when the back button is clicked (lambda)
     */
    protected fun clickBack(clickBack: ClickBack) {this.clickBack = clickBack}

    /**.
     * Function That Handles When A Recycler-Adapter Is Long Clicked
     * @param [adapter]     Any type of recycler adapter
     * @param [longClicked] The invoked function for when the recycler-adapter is Long clicked (lambda)
     */
    protected fun longClick(adapter: JZRecyclerAdapterFB<*>, longClicked: LongClickRecycler) {

        // Sets The Long-Click Listener//
        adapter.itemLongClick { longClicked.invoke(it) }
    }

    /**.
     * Function That Handles When A Layout Is Long Clicked
     * @param [layouts]     Any type of layout
     * @param [longClicked] The invoked function for when the layout is Long clicked (lambda)
     */
    protected fun longClick(vararg layouts: ViewGroup, longClicked: LongClick) {

        // Sets The Long-Click Listener//
        for (layout in layouts) layout.setOnLongClickListener {
            lifecycleScope.launch { longClicked.invoke() }
            true
        }
    }

    /**.
     * Function That Handles When A View Is Long Clicked
     * @param [views]       Any type of view
     * @param [longClicked] The invoked function for when the view is long clicked (lambda)
     */
    protected fun longClick(vararg views: View, longClicked: LongClick) {

        // Sets The Long-Click Listener//
        for (view in views) view.setOnLongClickListener {
            lifecycleScope.launch { longClicked.invoke() }
            true
        }
    }

    //</editor-fold>

    //<editor-fold desc="Specific Listeners">

    /**.
     * Function That Handles When The SeekBar Progress Is Being Changed
     * @param [seekBar]        The seek-bar node
     * @param [progressChange] The invoked function for when the seek-bar progress is changing (lambda)
     */
    protected fun progressChange(seekBar: SeekBar, progressChange: ProgressChange) {
        this.progressChange = progressChange
        onSeekBarChange(seekBar)
    }

    /**.
     * Function That Handles When The User Starts Touching The SeekBar Progress Tracker
     * @param [seekBar]            The seek-bar node
     * @param [progressTouchStart] The invoked function for when the user starts touching the seek-bar progress tracker (lambda)
     */
    protected fun progressTouchStart(seekBar: SeekBar, progressTouchStart: ProgressTouchStart) {
        this.progressTouchStart = progressTouchStart
        onSeekBarChange(seekBar)
    }

    /**.
     * Function That Handles When The User Stops Touching The SeekBar Progress Tracker
     * @param [seekBar]            The seek-bar node
     * @param [progressTouchStop] The invoked function for when the user stops touching the seek-bar progress tracker (lambda)
     */
    protected fun progressTouchStop(seekBar: SeekBar, progressTouchStop: ProgressTouchStop) {
        this.progressTouchStop = progressTouchStop
        onSeekBarChange(seekBar)
    }

    /**.
     * Function That Handles When A SearchView Field Is Closed Into An Icon
     * @param [searchView]   The search-view node
     * @param [searchClosed] The invoked function for when the search-view field closes (lambda)
     */
    protected fun searchClose(searchView: SearchView, searchClosed: SearchClose) {

        // Sets The On Search Click Listener//
        searchView.setOnCloseListener {
            lifecycleScope.launch {
                searchClosed.invoke()
            }
            searchView.onActionViewCollapsed()
            return@setOnCloseListener true
        }
    }

    /**.
     * Function That Handles When A SearchView Icon Is Clicked And Opened Into A Field
     * @param [searchView]   The search-view node
     * @param [searchOpened] The invoked function for when the search-view field opens (lambda)
     */
    protected fun searchOpen(searchView: SearchView, searchOpened: SearchOpen) {

        // Sets The On Search Click Listener//
        searchView.setOnSearchClickListener {
            lifecycleScope.launch {
                searchOpened.invoke()
            }
        }
    }

    /**.
     * Function That Handles When A Search Query Is Changed
     * @param [searchView]  The search-view node
     * @param [queryChange] The invoked function for when the search-view query is changed (lambda)
     */
    protected fun searchQueryChange(searchView: SearchView, queryChange: QueryChange) {
        this.queryChange = queryChange
        onQuerySearch(searchView)
    }

    /**.
     * Function That Handles When A Search Query Is Submitted
     * @param [searchView]  The search-view node
     * @param [querySubmit] The invoked function for when the search-view query is submitted (lambda)
     */
    protected fun searchQuerySubmit(searchView: SearchView, querySubmit: QuerySubmit) {
        this.querySubmit = querySubmit
        onQuerySearch(searchView)
    }

    /**.
     * Function That Handles After An EditText Field's Text Has Been Changed
     * @param [editText]        The edit-text node
     * @param [textAfterChange] The invoked function for after an edit-text field's text has been changed
     */
    protected fun textAfterChange(editText: EditText, textAfterChange: TextAfterChange) {
        this.textAfterChange = textAfterChange
        onTextChange(editText)
    }

    /**.
     * Function That Handles Before An EditText Field's Text Has Been Changed
     * @param [editText]         The edit-text node
     * @param [textBeforeChange] The invoked function for before an edit-text field's text has been changed
     */
    protected fun textBeforeChange(editText: EditText, textBeforeChange: TextBeforeChange) {
        this.textBeforeChange = textBeforeChange
        onTextChange(editText)
    }

    /**.
     * Function That Handles When The EditText Field's Text Has Been Changed
     * @param [editText]          The edit-text node
     * @param [textCurrentChange] The invoked function for when the edit-text field's text has been changed
     */
    protected fun textCurrentChange(editText: EditText, textCurrentChange: TextCurrentChange) {
        this.textCurrentChange = textCurrentChange
        onTextChange(editText)
    }

    //</editor-fold>

    //<editor-fold desc="Activity Functions">

    /**.
     * Function That Shows The Layout And Theme Configurations
     * @param [layout] The layout resource id for the activity
     * @param [func]   The ui configurations
     */
    protected fun createUI(@LayoutRes layout: Int, func: suspend UI.() -> Unit = empty) {

        // Shows The Layout And Theme Configs//
        if (func === empty) setContentView(layout)
        else {
            setContentView(layout)
            lifecycleScope.launch { UI(layout).func() }
        }
    }

    /**.
     * Function That Gets A Drawable Resource Id For A File In The Drawable Folder
     * @param [drawable] The id of the drawable file
     * @return The drawable
     */
    protected fun getDrawableCompat(@DrawableRes drawable: Int): Drawable? {
        return ContextCompat.getDrawable(this, drawable)
    }

    /**.
     * Function That Gets A Color Resource id From The Colors.xml File In The Res Folder
     * @param [color] The id of the color
     * @return The color
     */
    protected fun getColorCompat(@ColorRes color: Int): Int {
        return getColor(this, color)
    }

    /**.
     * Function That Gets The Color Value Of A Theme Style Attr
     * @param [attrId] The id of the attr style
     * @return The color
     */
    protected fun getColorAttr(@AttrRes attrId: Int): Int {

        // Define And Initialize TypedValue Value//
        val typedValue = TypedValue()

        // Gets The Primary Attribute Color For The Current Theme//
        this@JZActivity.theme.resolveAttribute(
            attrId,
            typedValue,
            true
        )

        // Returns The Color Of Value Of The Given Style
        return typedValue.data
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
     * Function That Prepares An Email To Be Sent Using The Users Default Email App
     * @param [to]      Who the email is being sent to
     * @param [subject] The subject of the email
     * @param [body]    A predefined email body, maybe for the user to fill in
     */
    protected fun prepareEmailToSend(to: String, subject: String, body: String) {

        // Generates The Email Template//
        val uri = Uri.parse("mailto:")
            .buildUpon()
            .appendQueryParameter("to", to)
            .appendQueryParameter("subject", subject)
            .appendQueryParameter("body", body)
            .build()

        // Opens The Users Default Email App To Send The Message//
        val emailIntent = Intent(Intent.ACTION_SENDTO, uri)
        startActivity(Intent.createChooser(emailIntent, "Sending Email..."))
    }

    /**.
     * Function That Shows A Long Toast Message
     * @param [message] The message to show
     */
    protected fun toastLong(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**.
     * Function That Shows A Short Toast Message
     * @param [message] The message to show
     */
    protected fun toastShort(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    //</editor-fold>

    //<editor-fold desc="Activity Transitions">

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
     * Function That Starts A New Activity
     * @param [activity]      An android activity kotlin Class
     * @param [isAnimation]   Checks whether to use the default animation
     * @param [intentOptions] The different intent options for sending an activity
     */
    protected fun startActivity(activity: KClass<*>, isAnimation: Boolean, intentOptions: IntentOptions? = null) {

        // Define And Instantiate Intent Value//
        val newActivity = Intent(this, activity.java).apply {
            intentOptions?.invoke(this)
        }

        // Starts The Activity With The Default Animation//
        if (isAnimation) {
            startActivity(newActivity)
            return
        }

        // Removes The Activity Animation And Starts The Activity//
        newActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivityForResult(newActivity, 0)
        overridePendingTransition(0, 0)
    }

    /**.
     * Function That Starts A New Activity
     * @param [activity]      An android activity kotlin Class
     * @param [animIn]        The custom start animation
     * @param [animOut]       The custom end animation
     * @param [intentOptions] The different intent options for sending an activity
     */
    protected fun startActivity(activity: KClass<*>, animIn: Int, animOut: Int, intentOptions: IntentOptions? = null) {

        // Define And Instantiate Intent Value//
        val newActivity = Intent(this, activity.java).apply {
            intentOptions?.invoke(this)
        }

        // Starts The New Activity With The Custom Animation//
        startActivity(newActivity)
        overridePendingTransition(animIn, animOut)
    }

    /**.
     * Function That Launches An Activity Where A Result Will Be Given Once It Is Finished
     * @param [intent]         The intent to start
     * @param [requestCode]    The request code allowing you to identify who this result came from
     * @param [activityResult] The invoked function gor getting the activity result
     */
    protected fun startActivityResult(intent: Intent, requestCode: Int, activityResult: ActivityResult) {
        startActivityForResult(intent, requestCode)
        this.activityResult = activityResult
    }

    //</editor-fold>

    //<editor-fold desc="Private Specific Listeners">

    /**.
     * Function That Handles Search Text Queries
     * @param [searchView] The search-view node
     */
    private fun onQuerySearch(searchView: SearchView) {

        // Sets The Query Text Listener//
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {

            // When The Query Is Submitted//
            override fun onQueryTextSubmit(p0: String?): Boolean {
                lifecycleScope.launch {
                    querySubmit?.invoke(p0)
                }
                return true
            }

            // When The Query Text Is Changing//
            override fun onQueryTextChange(p0: String?): Boolean {
                lifecycleScope.launch {
                    queryChange?.invoke(p0)
                }
                return true
            }

        })
    }

    /**.
     * Function That Handles When A SeekBar Changes
     * @param [seekBar] The seek-bar node
     */
    private fun onSeekBarChange(seekBar: SeekBar) {

        // Sets The SeekBar Change Listener//
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            // When The SeekBar Progress Changes
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                lifecycleScope.launch {
                    progressChange?.invoke(p0, p1, p2)
                }
            }

            // When The User Starts Tracking The SeekBar//
            override fun onStartTrackingTouch(p0: SeekBar?) {
                lifecycleScope.launch {
                    progressTouchStart?.invoke(p0)
                }
            }

            // When The User Stops Tracking The SeekBar//
            override fun onStopTrackingTouch(p0: SeekBar?) {
                lifecycleScope.launch {
                    progressTouchStop?.invoke(p0)
                }
            }
        })
    }

    /**.
     * Function That Handles When A EditText's Text Changes
     * @param [editText] The edit-text node
     */
    private fun onTextChange(editText: EditText) {

        // Sets The EditText Text Change Listener//
        editText.addTextChangedListener(object : TextWatcher {

            // What Happens As Text Is Currently Being Changed//
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                lifecycleScope.launch {
                    textCurrentChange?.invoke(p0.toString(), p1, p2, p3)
                }
            }

            // What Happens Before The Text Is Changed//
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                lifecycleScope.launch {
                    textBeforeChange?.invoke(p0.toString(), p1, p2, p3)
                }
            }

            // What Happens After The Text Is Changed//
            override fun afterTextChanged(p0: Editable?) {
                lifecycleScope.launch {
                    textAfterChange?.invoke(p0.toString())
                }
            }
        })
    }

    //</editor-fold>

    /** Kotlin Inner Class UI
     *  Inner Class For Easily Configuring UI Options
     *  @author Jordan Zimmitti
     */
    protected inner class UI(@LayoutRes private val layout: Int) {

        // Define And Initializes Boolean Variables//
        private var isDarkIconsStatus     = false
        private var isDarkIconsNavigation = false

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
         * Function That Sets A Custom Navigation Bar Color And Icon Tint
         * @param [color]       The color resource, default is the style item 'colorPrimary'
         * @param [isDarkIcons] Whether the navigation bar icon tint should be dark
         */
        fun navigationColor(@ColorRes color: Int? = null, isDarkIcons: Boolean) {

            // When A Color Exists Or Not//
            when (color) {
                null -> window.navigationBarColor = getColorAttr(R.attr.colorPrimary)
                else -> window.navigationBarColor = getColorCompat(color)
            }

            // Sets The Icon Color of The System Bars//
            isDarkIconsNavigation = isDarkIcons
            setDarkSystemBarIcons()
        }

        /**.
         * Function That Sets A Custom Status Bar Color And Icon Tint
         * @param [color]       The color resource, default is the style item 'colorPrimaryDark'
         * @param [isDarkIcons] Whether the status bar icon tint should be dark
         */
        fun statusBarColor(@ColorRes color: Int? = null, isDarkIcons: Boolean) {

            // When A Color Exists Or Not//
            when (color) {
                null -> window.statusBarColor = getColorAttr(R.attr.colorPrimaryDark)
                else -> window.statusBarColor = getColorCompat(color)
            }

            // Sets The Icon Color of The System Bars//
            isDarkIconsStatus = isDarkIcons
            setDarkSystemBarIcons()
        }

        /**.
         * Function That Creates A Custom Title For The Activity
         * @param [textView] The TextView being used for the activity title
         * @param [title]    The title resource id for the activity
         */
        fun title(textView: TextView, @StringRes title: Int) {textView.text = getString(title)}
        fun title(textView: TextView, title: String) {textView.text = title}
        fun title(@StringRes title: Int) {setTitle(title)}
        fun title(title: String) {setTitle(title)}

        /**.
         * Function That Sets A Custom Theme For The Activity
         * It must go before every other function in createUI
         * @param [theme] The style resource id for the activity
         */
        fun theme(@StyleRes theme: Int) {

            // Sets The Theme And Layout//
            setTheme(theme)
            navigationColor(isDarkIcons = false)
            statusBarColor(isDarkIcons = false)
            setContentView(layout)
        }

        /**.
         * Function That Sets The Icon Color of The System Bars
         */
        @Suppress("DEPRECATION")
        @SuppressLint("InlinedApi")
        private fun setDarkSystemBarIcons() {
            when {

                // When Both Status And Navigation Icons Are Dark//
                isDarkIconsStatus && isDarkIconsNavigation -> {

                    if (SDK_INT in ANDROID_M..ANDROID_Q) {

                        // Enables Dark Status Bar Icons//
                        window.decorView.systemUiVisibility =
                            SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                    else if (SDK_INT >= ANDROID_R) {

                        // Enables Dark Status Bar Icons//
                        window.insetsController?.setSystemBarsAppearance(
                            APPEARANCE_LIGHT_STATUS_BARS,
                            APPEARANCE_LIGHT_STATUS_BARS

                        )
                        window.insetsController?.setSystemBarsAppearance(
                            APPEARANCE_LIGHT_NAVIGATION_BARS,
                            APPEARANCE_LIGHT_NAVIGATION_BARS
                        )
                    }
                }

                // When Only Status Bar Icons Are Dark//
                isDarkIconsStatus && !isDarkIconsNavigation -> {
                    @Suppress("DEPRECATION")
                    if (SDK_INT in ANDROID_M..ANDROID_Q) {

                        // Enables Dark Status Bar Icons//
                        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                    else if (SDK_INT >= ANDROID_R) {

                        // Enables Dark Status Bar Icons//
                        window.insetsController?.setSystemBarsAppearance(
                            APPEARANCE_LIGHT_STATUS_BARS,
                            APPEARANCE_LIGHT_STATUS_BARS
                        )
                    }
                }

                // When Only Navigation Icons Are Dark//
                !isDarkIconsStatus && isDarkIconsNavigation -> {
                    @Suppress("DEPRECATION")
                    if (SDK_INT in ANDROID_M..ANDROID_Q) {

                        // Enables Dark Status Bar Icons//
                        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    }
                    else if (SDK_INT >= ANDROID_R) {

                        // Enables Dark Status Bar Icons//
                        window.insetsController?.setSystemBarsAppearance(
                            APPEARANCE_LIGHT_NAVIGATION_BARS,
                            APPEARANCE_LIGHT_NAVIGATION_BARS
                        )
                    }
                }
            }
        }
    }
}