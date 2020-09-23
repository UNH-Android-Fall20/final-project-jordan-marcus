package dev.jzdevelopers.libs

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter

// Type Aliases For Lambda Functions//
private typealias ItemClicked = (position: Int, view: View)                                    -> Unit
private typealias MultiSelect = (itemSelected: Int, allItemSelected: Int, isSelected: Boolean) -> Unit
private typealias Scroll      = (dy: Int)                                                      -> Unit

/** Kotlin Class JZRecyclerAdapter
 *  Class That Creates An Adapter To Show All Of The Items In A RecyclerView List For A Given Type
 *  @author Jordan Zimmitti
 */
@Suppress("unused")
class JZRecyclerAdapter<TYPE>(@LayoutRes private val layoutId : Int,
                                         private val list     : List<TYPE>,
                                         private val views    : View.(TYPE) -> Unit): MultiChoiceAdapter<JZRecyclerAdapter<TYPE>.ViewHolder>() {

    // Defines ItemClicked Variable//
    private var itemClicked: ItemClicked? = null

    /**.
     * Configures Static Variables And Functions
     */
    companion object {

        // Defines multiSelect And scroll Variables//
        private var multiSelect: MultiSelect? = null
        private var scroll     : Scroll?      = null

        /**.
         * Function That Invokes A Function When The RecyclerAdapter Is Multi Selected
         * @param [recyclerAdapter] Any recycler adapter
         * @param [multiSelect]     The invoked function when the recycler adapter is multi selected
         */
        fun onMultiSelect(recyclerAdapter: JZRecyclerAdapter<*>, multiSelect: MultiSelect) {
            this.multiSelect = multiSelect
            onMultiSelect(recyclerAdapter)
        }

        /**.
         * Function That Invokes A Function When The RecyclerView Is Scrolling
         * @param [recyclerView] Any recycler view
         * @param [scroll]       The invoked function when the recycler view is scrolling
         */
        fun onScroll(recyclerView: RecyclerView, scroll: Scroll) {
            this.scroll = scroll
            onScroll(recyclerView)
        }

        /**.
         * Function That Handles When The RecyclerView Is Scrolling
         * @param [recyclerView] Any recycler view
         */
        private fun onScroll(recyclerView: RecyclerView) {

            // When The RecyclerView Is Scrolling//
            recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    scroll?.invoke(dy)
                }
            })
        }

        /**.
         * Function That Handles When An Item Is MultiSelected
         * @param [recyclerAdapter] Any recycler adapter
         */
        private fun onMultiSelect(recyclerAdapter: JZRecyclerAdapter<*>) {

            // When An Item Is Multi Selected//
            recyclerAdapter.setMultiChoiceSelectionListener (object: Listener {

                // When An Item Is Selected//
                override fun OnItemSelected(selectedPosition: Int, itemSelectedCount: Int, allItemCount: Int) {

                    // The Code For When An Item Is Selected//
                    multiSelect?.invoke(selectedPosition, itemSelectedCount, true)
                }

                // When An Item Is De-Selected//
                override fun OnItemDeselected(deselectedPosition: Int, itemSelectedCount: Int, allItemCount: Int) {

                    // The Code For When An Item Is De-Selected//
                    multiSelect?.invoke(deselectedPosition, itemSelectedCount, false)
                }

                // Not Implemented Yet...//
                override fun OnSelectAll(itemSelectedCount: Int, allItemCount: Int) {
                }
                override fun OnDeselectAll(itemSelectedCount: Int, allItemCount: Int) {
                }
            })
        }
    }

    /**.
     * Function That Gets The Amount Of Items In The List
     * @return The amount of items
     */
    override fun getItemCount(): Int {return list.size}

    /**.
     * Function That Creates And Inflates A Custom View
     * @param [parent] The parent view
     * @return The custom view
     */
    override fun onCreateViewHolder(parent: ViewGroup, a: Int): JZRecyclerAdapter<TYPE>.ViewHolder {

        // Returns The Custom View//
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
    }

    /**.
     * Function That Handles When A List Item Is Clicked
     * @param [holder]   Where the views are defined and initialized
     * @param [position] Where the specific item is located in the list
     * @return The on-click action
     */
    override fun defaultItemViewClickListener(holder: ViewHolder?, position: Int): View.OnClickListener? {

        // Returns The On Click Action//
        return View.OnClickListener { view -> itemClicked?.invoke(position, view)}
    }

    /**.
     * Function that Sets The List's Items To Their Corresponding View
     * @param [holder]   Where the views are defined and initialized
     * @param [position] Where the specific item is located in the list
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Differs To Parent Class//
        super.onBindViewHolder(holder, position)

        // Sets The Item To Its Corresponding View//
        holder.itemView.views(list[position])
    }

    /**.
     * Function That Invokes A Function When An Item is Clicked
     * @param [itemClick] The invoked function when the item is clicked
     */
    fun onItemClick(itemClick: ItemClicked) {this.itemClicked = itemClick}

    /**.
     * Class That Specifies The Amount Of White Space Between Each Item
     * @param [height] The amount of white space between each item
     */
    class Spacing(private val height: Int): RecyclerView.ItemDecoration() {

        /**.
         * Function that controls The Amount Of Spacing Between Items
         * @param spacing The amount of white space between each item
         */
        override fun getItemOffsets(spacing: Rect, view: View, parent: RecyclerView, c: RecyclerView.State) {

            // Sets The Spacing//
            spacing.bottom = height

            // Sets The Spacing For The Last Item//
            if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) spacing.bottom = 280
        }
    }

    /**.
     * Class That Sets Up The Views To Show The Lists Items
     * @param [customView] Custom view that shows the lists items
     */
    inner class ViewHolder(customView: View): RecyclerView.ViewHolder(customView)
}