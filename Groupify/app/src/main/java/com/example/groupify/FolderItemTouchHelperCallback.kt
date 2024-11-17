package com.example.groupify

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class FolderItemTouchHelperCallback(private val adapter: RecyclerView.Adapter<*>) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags = 0 // 드래그만 허용하고 스와이프는 금지
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, from: RecyclerView.ViewHolder, to: RecyclerView.ViewHolder): Boolean {
        val fromPosition = from.adapterPosition
        val toPosition = to.adapterPosition

        // 아이템을 이동시킨다
        (adapter as FolderAdapter).onItemMove(fromPosition, toPosition)

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 스와이프 동작은 처리하지 않음
    }

    override fun isLongPressDragEnabled(): Boolean = true // 아이템을 길게 눌러서 드래그 가능하도록 설정
}
