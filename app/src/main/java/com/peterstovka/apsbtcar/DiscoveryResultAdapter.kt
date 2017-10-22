package com.peterstovka.apsbtcar

import android.bluetooth.BluetoothDevice
import android.support.v7.util.DiffUtil
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author [Peter Stovka](mailto:stovka.peter@gmail.com)
 */
class DiscoveryResultAdapter : RecyclerView.Adapter<DiscoveryResultAdapter.ViewHolder>() {

    private val items = mutableListOf<Item>()

    public var itemClickListener: ((item: Item) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.holder_discovery_result, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.bindListener { itemClickListener?.invoke(items[it]) }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItems(items: List<Item>) {
        val diff = DiffUtil.calculateDiff(DiffHelper(this.items, items))
        this.items.clear()
        this.items.addAll(items)
        diff.dispatchUpdatesTo(this)
    }

    fun addItem(item: Item) {
        if (this.items.find { it.mac == item.mac } == null) {
            val newList = this.items.toMutableList()
            newList.add(item)
            this.setItems(newList)
        }
    }

    fun removeItem(item: Item) {
        val index = this.items.indexOfFirst { it.mac == item.mac }
        if (index >= 0) {
            val newList = this.items.toMutableList()
            newList.removeAt(index)
            this.setItems(newList)
        }
    }

    data class Item(
            val mac: String,
            val name: String,
            val device: BluetoothDevice
    )

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val name: AppCompatTextView = view.findViewById(R.id.holderDiscoveryName)
        private val mac: AppCompatTextView = view.findViewById(R.id.holderDiscoveryMac)

        private var listener: ((adapterPosition: Int) -> Unit)? = null

        init {
            view.setOnClickListener { listener?.invoke(adapterPosition) }
        }

        fun bind(item: Item) {
            name.text = item.name
            mac.text = item.mac
        }

        fun bindListener(listener: (adapterPosition: Int) -> Unit) {
            this.listener = listener
        }

    }

    private class DiffHelper(
            val oldList: List<Item>,
            val newList: List<Item>
    ): DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].mac == newList[newItemPosition].mac
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

}