package dev.jzdevelopers.cstracker.libs

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import dev.jzdevelopers.cstracker.R
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

// Type Aliases For Lambda//
private typealias ItemClickFB       = suspend (position: Int)                                                  -> Unit
private typealias ItemLongClickFB   = suspend (position: Int)                                                  -> Unit
private typealias ItemMultiSelectFB = suspend (itemPosition: Int, itemSelectedCount: Int, isSelected: Boolean) -> Unit
private typealias ScrollFB          = suspend (dy: Int)                                                        -> Unit

/** Kotlin Class JZRecyclerAdapter,
 *  Class That Creates An Adapter To Show All Of The Items In A RecyclerView For A Given Type
 *  @author Jordan Zimmitti, Marcus Novoa
 *  @param [context]  The instance from the caller activity
 *  @param [scope]    The lifecycle coroutine scope for possible async/await calls
 *  @param [layoutId] The custom layout that shows the data saved in a model
 *  @param [query]    The query used to get the data for the model
 *  @param [model]    The model that structures and stores the data
 *  @param [views]    Matches the nodes in the layout with the properties in the model (lambda)
 */
class JZRecyclerAdapterFB<TYPE : Any>(
    private val context : Context,
    private val scope   : LifecycleCoroutineScope,
    layoutId : Int,
    query    : Query,
    model    : KClass<TYPE>,
    views    : suspend View.(TYPE) -> Unit
) {

    //<editor-fold desc="Class Variables">

    /**.
     * Property That Sets The Color For When An Item Is Multi-Selected
     */
    @ColorRes
    var multiSelectColor: Int = R.color.translucent

    /**.
     * Property That Gets The Amount Of Items In The RecyclerView Adapter
     * @return The amount of items
     */
    val itemCount: Int
        get() = fireBaseAdapter.itemCount

    // Defines RecyclerView Variable//
    private lateinit var recyclerView: RecyclerView

    // Define And Initialize Lambda Variables//
    private var itemClickFB       : ItemClickFB?       = null
    private var itemLongClickFB   : ItemLongClickFB?   = null
    private var itemMultiSelectFB : ItemMultiSelectFB? = null
    private var scrollFB          : ScrollFB?          = null

    // Define And Instantiates ArrayList Value//
    private val multiSelectedList = ArrayList<Int>()

    //</editor-fold>

    /**.
     * Function That Attaches The RecyclerView To The Adapter To Show The Items
     * @param [recyclerView] Any recycler-view node
     */
    fun attachRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView          = recyclerView
        recyclerView.adapter       = fireBaseAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.hasFixedSize()
        setSpacing()
    }

    /**.
     * Function That Gets An Item Shown In The RecyclerView
     * @return The item
     */
    fun getItem(position: Int): TYPE {
        return fireBaseAdapter.getItem(position)
    }

    /**.
     * Function That Gets The Item Id Shown In The RecyclerView
     * @return The item
     */
    fun getItemId(position: Int): String {
        return fireBaseAdapter.snapshots.getSnapshot(position).id
    }

    /**.
     * Function That Handles When A RecyclerView Item Is Clicked
     * @param [itemClick] The invoked function for when the item is clicked (lambda)
     */
    fun itemClick(itemClicked: ItemClickFB) {
        itemClickFB = itemClicked
    }

    /**.
     * Function That Handles When A RecyclerView Item Is Long Clicked
     * @param [itemLongClick] The invoked function for when the item is long clicked (lambda)
     */
    fun itemLongClick(itemLongClicked: ItemLongClickFB) {
        itemLongClickFB = itemLongClicked
    }

    /**.
     * Function That Handles When A RecyclerView Item Is Multi-Selected
     * @param [itemMultiSelect] The invoked function for when the item is multi-selected (lambda)
     */
    fun itemMultiSelect(itemMultiSelected: ItemMultiSelectFB) {
        itemMultiSelectFB = itemMultiSelected
    }

    /**.
     * Function That Handles When The RecyclerView Items Are Scrolling
     */
    fun itemsScrolling(scrolling: ScrollFB) {
        scrollFB = scrolling
        onScroll()
    }

    /**.
     * Function That Restarts The Adapter
     */
    fun restart() {
        fireBaseAdapter.stopListening()
        fireBaseAdapter.startListening()
        attachRecyclerView(recyclerView)
    }

    /**.
     * Function That Sets The Amount Of White Space Between Each Item And Underneath The Last Item
     * @param [gap]     The amount of white space between each item
     * @param [lastGap] The amount of white space after the last item
     */
    fun setSpacing(gap: Int = 60, lastGap: Int = 280) {

        // Removes All The Added Spacing Decorations//
        while (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }

        // Adds The New Spacing Decoration
        recyclerView.addItemDecoration(Spacing(gap, lastGap))
    }

    /**.
     * Function That Starts Listening For Database Changes And Populates The Adapter
     */
    fun startListening() {
        fireBaseAdapter.startListening()
    }

    /**.
     * Function That Stops Listening For Database Changes And Clears All Items In The Adapter
     */
    fun stopListening() {
        fireBaseAdapter.stopListening()
    }

    /**.
     * Function That Handles When A RecyclerView Item Is Multi-Selected
     * @param [holder]   Where the nodes are defined and initialized
     * @param [position] Where the specific item is located in the recycler-view
     */
    private fun onItemMultiSelect(holder: JZRecyclerAdapterFB<TYPE>.ViewHolder, position: Int) {

        // Gets The Index If The Item Is Already Selected//
        val index = multiSelectedList.indexOfFirst { savedPosition ->
            savedPosition == position
        }
        when(index) {
            -1   -> {

                // Highlights The Selected Item//
                val color = ColorDrawable(ContextCompat.getColor(context, multiSelectColor))
                holder.itemView.foreground = color

                // Adds Items Position To The Multi-Selected List//
                multiSelectedList.add(position)

                // Invokes The Item Multi-Select Lambda Function//
                scope.launch { itemMultiSelectFB?.invoke(position, multiSelectedList.count(), true) }
            }
            else -> {

                // Un-Highlights The Selected Item//
                holder.itemView.foreground = null

                // Removes Items Position From The Multi-Selected List//
                multiSelectedList.removeAt(index)

                // Invokes The Item Multi-Select Lambda Function//
                scope.launch { itemMultiSelectFB?.invoke(position, multiSelectedList.count(), false) }
            }
        }
    }

    /**.
     * Function That Handles When The RecyclerView Items Are Scrolling
     */
    private fun onScroll() {

        // When The RecyclerView Items Are Scrolling//
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scope.launch { scrollFB?.invoke(dy) }
            }
        })
    }

    /**
     * Builds The Options For The FirestoreRecyclerAdapter
     */
    private val options = FirestoreRecyclerOptions.Builder<TYPE>()
        .setQuery(query, model.java)
        .build()

    /**
     * Define And Instantiates The FirestoreRecyclerAdapter
     */
    private val fireBaseAdapter = object: FirestoreRecyclerAdapter<TYPE, ViewHolder>(options) {

        /**.
         * Function That Inflates The Custom View
         * @param [parent] The parent view
         * @return The custom view holder
         */
        override fun onCreateViewHolder(parent: ViewGroup, a: Int): JZRecyclerAdapterFB<TYPE>.ViewHolder {

            // Returns The Inflated Custom View//
            return ViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
        }

        /**.
         * Function That Binds The Nodes In The View With The Properties In The Model
         * @param [holder]   Where the nodes are defined and initialized
         * @param [position] Where the specific item is located in the recycler-view
         */
        override fun onBindViewHolder(holder: JZRecyclerAdapterFB<TYPE>.ViewHolder, position: Int, model: TYPE) {

            // Binds The Layout Nodes With The Model Properties//
            scope.launch { holder.itemView.views(model) }

            // When An RecyclerView Item Is Clicked//
            holder.itemView.setOnClickListener {

                // Selects The Long-Clicked Item//
                if (multiSelectedList.size != 0) {
                    onItemMultiSelect(holder, position)
                    return@setOnClickListener
                }

                // Invokes The Item Click Lambda Function//
                scope.launch { itemClickFB?.invoke(position) }
            }

            // When An RecyclerView Item Is Long Clicked//
            holder.itemView.setOnLongClickListener {
                when {

                    // When Both The Long-Click And Multi-Select Lambdas Are Being Used For The Same Adapter//
                    itemLongClickFB != null && itemMultiSelectFB != null -> {

                        // Throws A Runtime Error//
                        throw RuntimeException("An adapter instance can call either 'itemLongClick' or 'itemMultiSelect' but not both")
                    }

                    // When The Long-Click Lambda Is Being Used//
                    itemLongClickFB != null -> {

                        // Invokes The Item Long-Click Lambda Function//
                        scope.launch { itemLongClickFB?.invoke(position) }
                    }

                    // When The Multi-Select Lambda Is Being Used//
                    itemMultiSelectFB != null -> {

                        // Selects The Long-Clicked Item//
                        onItemMultiSelect(holder, position)
                    }
                }
                return@setOnLongClickListener true
            }
        }
    }

    /** Kotlin Class Spacing,
     *  Class That Specifies The Amount Of White Space Between Each Item And At The End Of The Last Item
     * @param [gap]     The amount of white space between each item
     * @param [lastGap] The amount of white space after the last item
     */
    private inner class Spacing(
        private val gap     : Int,
        private val lastGap : Int,
    ): RecyclerView.ItemDecoration() {

        /**.
         * Function that controls The Amount Of Spacing Between Items
         * @param spacing The amount of white space between each item
         */
        override fun getItemOffsets(spacing: Rect, view: View, parent: RecyclerView, c: RecyclerView.State) {

            // Sets The Spacing//
            spacing.bottom = gap

            // Sets The Spacing For The Last Item//
            if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1 )
                spacing.bottom = lastGap
        }
    }

    /** Inner Class ViewHolder,
     *  Class That Inflates The Custom View
     *  @param [view] Custom view that holds the nodes needed to display the model
     */
    private inner class ViewHolder(view: View): RecyclerView.ViewHolder(view)
}
