package com.zgy.tracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.zgy.tracker.MyAdapter
import com.zgy.tracker.R
import kotlinx.android.synthetic.main.activity_main.*

class RecycleViewFragment: BaseFragment() {
    private val myDataset = arrayOfNulls<String>(100)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View = inflater.inflate(
        R.layout.activity_main,
        container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        rv.setHasFixedSize(true)
        rv.layoutManager = GridLayoutManager(activity, 2)

        for (i in myDataset.indices) {
            myDataset[i] = "item $i"
        }

        rv.adapter = MyAdapter(myDataset)
    }
}