package com.astroyodha.ui.user.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.astroyodha.R

/**
 * Custom Popup list adapter : Show item list in popup
 */
class CustomPopupAdapter(
    private val mContext: Context,
    private val customItems: ArrayList<String>,
    private val delegate: Delegate
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Delegate {
        fun onCustomItemClick(customItem: String, position: Int)
    }

    var mInflater: LayoutInflater = mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var lastCheckedPosition = -1

    val RADIO_TYPE = 1
    val TEXTVIEW_TYPE = 2
    val CHECKBOX_TYPE = 3
    val CUSTOM_TYPE = 4
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            RADIO_TYPE -> {
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.raw_time_popup_list, parent, false)
                return RadioViewHolder(v)
            }
            TEXTVIEW_TYPE -> {
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.raw_time_popup_list, parent, false)
                return TextViewHolder(v)
            }
            CHECKBOX_TYPE -> {
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.raw_time_popup_list, parent, false)
                return CheckBoxViewHolder(v)
            }
            CUSTOM_TYPE -> {
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.raw_time_popup_list, parent, false)
                return CustomViewHolder(v)
            }
        }
        val v =
                mInflater.inflate(R.layout.raw_time_popup_list, parent, false)
        return CustomViewHolder(
                v
        )
    }

    /*override fun getItemViewType(position: Int): Int {
        when (customItems[position].viewType) {
            1 -> return RADIO_TYPE
            2 -> return TEXTVIEW_TYPE
            3 -> return CHECKBOX_TYPE
            4 -> return CUSTOM_TYPE
        }
        return RADIO_TYPE
    }*/

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = this.customItems[position]
        //for default check in first item
        if (holder is RadioViewHolder) {
        } else if (holder is TextViewHolder) {
        } else if (holder is CheckBoxViewHolder) {
        } else if (holder is CustomViewHolder) {
            holder.tvTitle.text = model

            // don't know but divider was not showing on second item show added this logic
            /*if (position == customItems.size - 1) {
                holder.view2.makeGone()
            } else {
                holder.view2.makeVisible()
            }

            if (position == 0) {
                holder.view.makeGone()
            } else {
                holder.view.makeVisible()
            }
*/
            holder.itemView.setOnClickListener {
                delegate.onCustomItemClick(model, position)
            }
        }


    }

    fun addCustomItem(customList: List<String>) {
        this.customItems.addAll(customList)
        notifyDataSetChanged()
    }

    override fun getItemCount() = this.customItems.size

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val view: View = itemView.findViewById(R.id.view)
        val view2: View = itemView.findViewById(R.id.view2)
    }

    class RadioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class CheckBoxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
