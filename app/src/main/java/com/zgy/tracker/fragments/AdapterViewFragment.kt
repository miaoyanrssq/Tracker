package com.zgy.tracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.zgy.tracker.R
import kotlinx.android.synthetic.main.fragment_adapter_view.*

/**
 * 包含ListView以及GridView的Fragment，用于验证AdapterView的点击统计
 * @author chenchong
 * 2017/11/28
 * 上午10:17
 */
class AdapterViewFragment : BaseFragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View = inflater.inflate(
    R.layout.fragment_adapter_view,
      container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val items = ArrayList<String>(100)
    (0 until 100).mapTo(items) { "item $it" }

    lv.adapter = AdapterViewAdapter(items)
    gv.adapter = AdapterViewAdapter(items)

    val listener = AdapterView.OnItemClickListener { _, _, position, _ ->
      Toast.makeText(this@AdapterViewFragment.context, items[position], Toast.LENGTH_LONG).show()
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