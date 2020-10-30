package com.zgy.tracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_adapter_view.*

class AdapterViewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_adapter_view)
        initView()

    }

    fun initView(){
        val items = ArrayList<String>(100)
        (0 until 100).mapTo(items) { "item $it" }

        lv.adapter = AdapterViewAdapter(items)
        gv.adapter = AdapterViewAdapter(items)

        val listener = AdapterView.OnItemClickListener { _, _, position, _ ->
            Toast.makeText(this, items[position], Toast.LENGTH_LONG).show()
        }
        lv.onItemClickListener = listener
        gv.onItemClickListener = listener
    }


    private inner class AdapterViewAdapter(val items: List<String>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (view == null) {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_adapter_view, parent, false)
            }

            with(view?.findViewById<TextView>(R.id.tv_clickable)) {
                this?.text = getString(R.string.text_clickable_mask, position.toString())
                this?.setOnClickListener {
                    Toast.makeText(context, this.text, Toast.LENGTH_SHORT).show()
                }
            }

            view?.findViewById<TextView>(R.id.tv_not_clickable)?.text = getString(
                R.string.text_not_clickable_mask, position.toString())

            return view!!
        }

        override fun getItem(position: Int) = items[position]

        override fun getItemId(position: Int) = position.toLong()

        override fun getCount() = items.size
    }
}