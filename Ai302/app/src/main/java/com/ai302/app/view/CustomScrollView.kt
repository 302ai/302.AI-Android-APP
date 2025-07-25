package com.ai302.app.view

/**
 * author :
 * e-mail :
 * time   : 2025/4/29
 * desc   :
 * version: 1.0
 */
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBindings.findChildViewById
import com.ai302.app.R

class CustomScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.core.widget.NestedScrollView(context, attrs, defStyleAttr), NestedScrollingParent3 {

    private var recyclerView: RecyclerView? = null
    private var lastY = 0f

    override fun onFinishInflate() {
        super.onFinishInflate()
        // 查找RecyclerView（根据id匹配，需与布局中的id一致）
        recyclerView = findViewById(R.id.chatRecyclerView)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val currentY = ev.y
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = currentY
                return super.onInterceptTouchEvent(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = currentY - lastY
                lastY = currentY

                // 关键：判断RecyclerView是否需要优先处理滑动
                if (recyclerView != null && shouldLetRecyclerViewScroll(dy)) {
                    // RecyclerView未到滚动边界，ScrollView不拦截事件
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun shouldLetRecyclerViewScroll(dy: Float): Boolean {
        val recyclerView = recyclerView ?: return false
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return false

        // 获取RecyclerView滚动状态
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
        val itemCount = layoutManager.itemCount

        return when {
            dy > 0 -> {  // 手指向下滑动（ScrollView尝试向上滚动）
                // RecyclerView是否可以向下滚动（未到顶部）
                firstVisibleItem > 0
            }
            dy < 0 -> {  // 手指向上滑动（ScrollView尝试向下滚动）
                // RecyclerView是否可以向上滚动（未到底部）
                lastVisibleItem < itemCount - 1
            }
            else -> false
        }
    }
}
