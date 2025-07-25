package com.ai302.app.view
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class SmartScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private var targetRecyclerView: RecyclerView? = null
    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var initialY = 0f
    private var isDragging = false

    // 设置关联的 RecyclerView
    fun setTargetRecyclerView(recyclerView: RecyclerView) {
        this.targetRecyclerView = recyclerView.apply {
            isVerticalScrollBarEnabled = false // 隐藏滚动条
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val recyclerView = targetRecyclerView ?: return super.onInterceptTouchEvent(ev)

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialY = ev.y
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDragging) {
                    val deltaY = ev.y - initialY
                    if (Math.abs(deltaY) > touchSlop) {
                        isDragging = true
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
        }

        // 判断是否应该拦截事件
        return if (isDragging && shouldInterceptEvent(recyclerView, ev)) {
            super.onInterceptTouchEvent(ev)
        } else {
            false
        }
    }

    // 核心逻辑：判断是否需要拦截事件
    private fun shouldInterceptEvent(recyclerView: RecyclerView, ev: MotionEvent): Boolean {
        val deltaY = ev.y - initialY
        val canRecyclerViewScroll = canRecyclerViewScroll(recyclerView, deltaY > 0)
        return !canRecyclerViewScroll // RecyclerView 无法滚动时，由 ScrollView 接管
    }

    // 判断 RecyclerView 是否能继续滚动
    private fun canRecyclerViewScroll(recyclerView: RecyclerView, isDownDirection: Boolean): Boolean {
        if (recyclerView.isLastPositionVisible()) {
            return true
        }

        if (!recyclerView.canScrollVertically(if (isDownDirection) 1 else -1)) {
            // 已经到达滚动边界（顶部或底部）
            return false
        }


        // 更精确的判断：通过内容高度和可见区域计算
        val verticalScrollOffset = recyclerView.computeVerticalScrollOffset()
        val verticalScrollRange = recyclerView.computeVerticalScrollRange()
        val verticalScrollExtent = recyclerView.computeVerticalScrollExtent()

        return if (isDownDirection) {
            // 向下滑动：检查是否到达底部
            verticalScrollOffset + verticalScrollExtent < verticalScrollRange
        } else {
            // 向上滑动：检查是否到达顶部
            verticalScrollOffset > 0
        }
    }

    // 平滑滚动到底部
    fun smoothScrollToBottom() {
        val targetView = getChildAt(0)
        if (targetView != null) {
            smoothScrollTo(0, targetView.height)
        }
    }

    // 直接跳转到底部（无动画）
    fun scrollToBottom() {
        val targetView = getChildAt(0)
        if (targetView != null) {
            scrollTo(0, targetView.height)
        }
    }

    fun RecyclerView.isLastPositionVisible(): Boolean {
        val adapter = this.adapter ?: return false
        val itemCount = adapter.itemCount
        if (itemCount == 0) return false

        val lastVisiblePosition = when (val lm = this.layoutManager) {
            is LinearLayoutManager -> lm.findLastVisibleItemPosition()
            is GridLayoutManager -> lm.findLastVisibleItemPosition()
            is StaggeredGridLayoutManager -> {
                val lastPositions = IntArray(lm.spanCount)
                lm.findLastVisibleItemPositions(lastPositions)
                lastPositions.maxOrNull() ?: -1
            }
            else -> -1
        }

        return lastVisiblePosition == itemCount - 1
    }

}