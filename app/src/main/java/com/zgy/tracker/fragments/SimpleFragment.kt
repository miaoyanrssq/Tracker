package com.zgy.tracker.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.foolchen.lib.tracker.demo.fragments.NestedFragment
import com.zgy.tracker.R
import kotlinx.android.synthetic.main.fragment_simple.*

/**
 * 简单的Fragment，内部不包含任何其他的Framgent
 * @author chenchong
 * 2017/11/23
 * 下午3:33
 */
class SimpleFragment : BaseFragment() {

  private var isVisibilityEnable = false
  private var isChildrenEnable = false

  private var nestedFragment: Fragment? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    isVisibilityEnable = arguments?.getBoolean("visibility_enable") ?: false
    isChildrenEnable = arguments?.getBoolean("children_enable") ?: false

    setHasOptionsMenu(isChildrenEnable)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? =
      inflater.inflate(R.layout.fragment_simple, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    if (isChildrenEnable) {
      fragment_container.visibility = View.VISIBLE

      nestedFragment = NestedFragment.newInstance()
      childFragmentManager.beginTransaction().replace(R.id.fragment_container,
        nestedFragment as NestedFragment
      ).commit()

    } else {
      fragment_container.visibility = View.GONE
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater?.inflate(R.menu.menu_fragments, menu)
  }

  override fun onPrepareOptionsMenu(menu: Menu) {
    menu?.findItem(R.id.action_show)?.isVisible = isVisibilityEnable
    menu?.findItem(R.id.action_hide)?.isVisible = isVisibilityEnable
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val itemId = item?.itemId
    when (itemId) {
      R.id.action_show -> {
        if (nestedFragment?.isHidden == true) {
          childFragmentManager.beginTransaction().show(nestedFragment!!).commit()
        }
        return true
      }
      R.id.action_hide -> {
        if (nestedFragment?.isHidden == false) {
          childFragmentManager.beginTransaction().hide(nestedFragment!!).commit()
        }
        return true
      }
    }

    return super.onOptionsItemSelected(item)
  }

  override fun isIgnored(): Boolean = isChildrenEnable
}