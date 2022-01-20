package com.astroyodha.ui.astrologer.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.astroyodha.R
import com.astroyodha.ui.user.model.wallet.WalletModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.dateToStringFormat
import java.util.*

/**
 * Transaction list adapter : Show Transaction list.
 */
class AstrologerTransactionAdapter(
    private val mContext: Context, var mList: ArrayList<WalletModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<AstrologerTransactionAdapter.ViewHolder>() {
    var mDateFormat: String = "d MMM, hh:mm a"
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_astrologer_transaction, parent, false)
        return ViewHolder(
            v
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val model = mList[holder.adapterPosition]
        try {
            var mTranscation: String
            var mImage: Int
            if (model.isRefund) {

                mImage = R.drawable.ic_paid_money
                mTranscation = mContext.getString(
                    R.string.send_refund_user,
                    model.userName.substringBefore(" ")
                )
                holder.tvOperand.text = "-"
                holder.tvPaymentBy.text = mContext.getString(R.string.sent_from)
                if (model.paymentType == Constants.PAYMENT_TYPE_RAZOR_PAY) {
                    holder.imgPaymentBy.setImageResource(R.drawable.ic__credit_card)
                }
            } else{
                mImage = R.drawable.ic_add_money
                mTranscation = mContext.getString(
                    R.string.received_from_user,
                    model.userName.substringBefore(" ")
                )
                holder.tvOperand.text = "+"
                holder.tvPaymentBy.text = mContext.getString(R.string.receive_in)
            }
            holder.imgRupee.setImageResource(mImage)
            holder.tvTransaction.text = mTranscation
            holder.tvTime.text = model.createdAt?.toDate()?.dateToStringFormat(mDateFormat)
            holder.tvAmount.text = model.amount.toString().removePrefix("-")
            holder.itemView.setOnClickListener {
                viewHolderClicks.onClickItem(model, holder.adapterPosition)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgRupee: ImageView = itemView.findViewById(R.id.imgRupee)
        var tvTransaction: TextView = itemView.findViewById(R.id.tvTransaction)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        var tvOperand: TextView = itemView.findViewById(R.id.tvOperand)
        var tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        var imgPaymentBy: ImageView = itemView.findViewById(R.id.imgPaymentBy)
        var tvPaymentBy: TextView = itemView.findViewById(R.id.tvPaymentBy)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: WalletModel, position: Int)
    }
}