package com.zgy.tracker

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class RecycleViewActivity: BaseActivity() {

    private val myDataset = arrayOfNulls<String>(100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {

        rv.setHasFixedSize(true)
        rv.layoutManager = GridLayoutManager(this, 2)

        for (i in myDataset.indices) {
            myDataset[i] = "item $i"
        }

        rv.adapter = MyAdapter(myDataset)
    }
}