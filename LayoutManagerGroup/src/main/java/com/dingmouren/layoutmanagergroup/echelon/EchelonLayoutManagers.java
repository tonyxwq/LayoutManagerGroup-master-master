package com.dingmouren.layoutmanagergroup.echelon;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author:XWQ
 * Time   2018/8/7
 * Descrition: this is EchelonLayoutManagers
 */

public class EchelonLayoutManagers extends RecyclerView.LayoutManager
{
    private Context mContext;

    private int verticalScrollOffset;//垂直方向上的偏移量

    private int totalHeight = 0;

    private int mItemViewWidth;
    private int mItemViewHeight;
    private int mItemCount;
    private int mScrollOffset = Integer.MAX_VALUE;//所有item集合的高度 的高度
    private float mScale = 0.9f;

    public EchelonLayoutManagers(Context context)
    {
        this.mContext = context;
        mItemViewWidth = (int) (getHorizontalSpace() * 0.87f);//item的宽
        mItemViewHeight = (int) (mItemViewWidth * 1.46f);//item的高
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state)
    {
        super.onLayoutChildren(recycler, state);

        //如果没有item，直接返回
        if (getItemCount() <= 0) return;
        // 跳过preLayout，preLayout主要用于支持动画
        if (state.isPreLayout())
        {
            return;
        }
        //在布局之前，将所有的子View先Detach掉，放入到Scrap缓存中
        detachAndScrapAttachedViews(recycler);

        mItemViewWidth = (int) (getHorizontalSpace() * 0.87f);
        mItemViewHeight = (int) (mItemViewWidth * 1.46f);
        mItemCount = getItemCount();
        mScrollOffset = Math.min(Math.max(mItemViewHeight, mScrollOffset), mItemCount * mItemViewHeight);

        //定义竖直方向的偏移量
        int offsetY = 0;
        totalHeight = 0;
        for (int i = 0; i < getItemCount(); i++)
        {
            //这里就是从缓存里面取出
            View view = recycler.getViewForPosition(i);
            //将View加入到RecyclerView中
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);
            //最后，将View布局
            layoutDecorated(view, 0, offsetY, width, offsetY + height);
            //将竖直方向偏移量增大height
            offsetY += height;
            //
            totalHeight += height;
        }
        //如果所有子View的高度和没有填满RecyclerView的高度，
        // 则将高度设置为RecyclerView的高度
        totalHeight = Math.max(totalHeight, getVerticalSpace());
    }

    @Override
    public boolean canScrollVertically()
    {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state)
    {
        int travel = dy;//实际要滑动的距离 verticalScrollOffset
        mScrollOffset = Math.min(Math.max(mItemViewHeight, mScrollOffset + dy), mItemCount * mItemViewHeight);
        if (verticalScrollOffset + dy < 0)//滑动到最顶部
        {
            travel = -verticalScrollOffset;

        } else if (verticalScrollOffset + dy > totalHeight - getVerticalSpace())
        {
            travel = totalHeight - getVerticalSpace() - verticalScrollOffset;
        }

        verticalScrollOffset += travel;
        offsetChildrenVertical(-travel);

        return travel;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams()
    {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 返回Reciverview的显示高度
     *
     * @return
     */
    public int getVerticalSpace()
    {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    /**
     * 获取RecyclerView的显示宽度
     */
    public int getHorizontalSpace()
    {
        //
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
}
