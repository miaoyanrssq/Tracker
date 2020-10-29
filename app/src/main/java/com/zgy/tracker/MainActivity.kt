package com.zgy.tracker

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private val myDataset = arrayOfNulls<String>(100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hello.setOnClickListener {
            println("click")
        }

        initRecycleViewq()
    }

    private fun initRecycleViewq() {
        for (i in myDataset.indices) {
            myDataset[i] = "item $i"
        }
        my_recycler_view.setHasFixedSize(true)
        my_recycler_view.layoutManager = LinearLayoutManager(this)

        for (i in myDataset.indices) {
            myDataset[i] = "item $i"
        }

        my_recycler_view.setAdapter(MyAdapter(myDataset))
    }
}