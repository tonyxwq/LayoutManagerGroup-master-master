package com.dingmouren.layoutmanagergroup.echelon;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen   git remote add origin git@github.com:tonyxwq/RecyclerView-.git
 * email: naildingmouren@gmail.com
 */

public class EchelonLayoutManager extends RecyclerView.LayoutManager
{

    private static final String TAG = "EchelonLayoutManager";

    private Context mContext;
    private int mItemViewWidth;
    private int mItemViewHeight;
    private int mItemCount;
    private int mScrollOffset = Integer.MAX_VALUE;//所有item集合的高度 的高度
    private float mScale = 0.9f;

    public EchelonLayoutManager(Context context)
    {
        this.mContext = context;
        mItemViewWidth = (int) (getHorizontalSpace() * 0.87f);//item的宽
        mItemViewHeight = (int) (mItemViewWidth * 1.46f);//item的高
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams()
    {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state)
    {
        if (state.getItemCount() == 0 || state.isPreLayout()) return;
        removeAndRecycleAllViews(recycler);
        //detachAndScrapAttachedViews(recycler);
        mItemViewWidth = (int) (getHorizontalSpace() * 0.87f);
        mItemViewHeight = (int) (mItemViewWidth * 1.46f);
        mItemCount = getItemCount();
        mScrollOffset = Math.min(Math.max(mItemViewHeight, mScrollOffset), mItemCount * mItemViewHeight);
        //Log.d("data","======/mItemViewHeight==========="+mItemViewHeight);// 913
        //Log.d("data","======/height==========="+mItemCount * mItemViewHeight);//9130
        //Log.d("data","======/mScrollOffset==========="+mScrollOffset);//9130
        layoutChild(recycler);
    }

    private int verticalScrollOffset;//垂直方向上的偏移量

    private int totalHeight = 0;

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state)
    {
        int travel = dy;
        mScrollOffset = Math.min(Math.max(mItemViewHeight, mScrollOffset + dy), mItemCount * mItemViewHeight);
        layoutChild(recycler);
        return travel;
    }


    @Override
    public boolean canScrollVertically()
    {
        return true;
    }

    @Override
    public boolean canScrollHorizontally()
    {
        return true;
    }

    private void layoutChild(RecyclerView.Recycler recycler)
    {
        if (getItemCount() == 0) return;
        int bottomItemPosition = (int) Math.floor(mScrollOffset / mItemViewHeight);// mScrollOffset 9130
        //Log.d("data","======bottomItemPosition==========="+bottomItemPosition);
        int remainSpace = getVerticalSpace() - mItemViewHeight;//remainSpace 205 getVerticalSpace() 1118

        int bottomItemVisibleHeight = mScrollOffset % mItemViewHeight;

        //Log.d("data","======bottomItemVisibleHeight==========="+bottomItemVisibleHeight);
        //Log.d("data","======remainSpace==========="+remainSpace);

        final float offsetPercentRelativeToItemView = bottomItemVisibleHeight * 1.0f / mItemViewHeight;

        ArrayList<ItemViewInfo> layoutInfos = new ArrayList<>();
        for (int i = bottomItemPosition -1, j = 1; i >= 0; i--, j++)
        {
            //Log.d("data","======i==========="+i);
            //Log.d("data","======j==========="+j);

            double maxOffset = (getVerticalSpace() - mItemViewHeight) / 2 * Math.pow(0.7, j);

            int start = (int) (remainSpace - offsetPercentRelativeToItemView * maxOffset);
            float scaleXY = (float) (Math.pow(mScale, j - 1) * (1 - offsetPercentRelativeToItemView * (1 - mScale)));

            ItemViewInfo info = new ItemViewInfo(start, scaleXY);
            layoutInfos.add(0, info);
            remainSpace = (int) (remainSpace - maxOffset);
            if (remainSpace <= 0)
            {
                info.setTop((int) (remainSpace + maxOffset));
                info.setScaleXY((float) Math.pow(mScale, j - 1));
                break;
            }
        }

        if (bottomItemPosition < mItemCount)
        {
            final int start = getVerticalSpace() - bottomItemVisibleHeight;
            layoutInfos.add(new ItemViewInfo(start, 1.0f) .setIsBottom());

        } else
        {
            bottomItemPosition = bottomItemPosition - 1;//99
        }
        int layoutCount = layoutInfos.size(); //6

        final int startPos = bottomItemPosition - (layoutCount - 1); // 14

        final int endPos = bottomItemPosition;//19
        //Log.d("data","======endPos==========="+bottomItemPosition);

        final int childCount = getChildCount();//获取当前可见的item的数量
        //Log.d("data","======childCount==========="+childCount);
        for (int i = childCount - 1; i >= 0; i--)
        {
            View childView = getChildAt(i);
            int pos = getPosition(childView);
            //Log.d("data","======childViewPosition==========="+pos);
            if (pos > endPos || pos < startPos)
            {
                removeAndRecycleView(childView, recycler);
            }
        }

        detachAndScrapAttachedViews(recycler);

        for (int i = 0; i < layoutCount; i++)
        {
            View view = recycler.getViewForPosition(startPos + i);
            ItemViewInfo layoutInfo = layoutInfos.get(i);
            addView(view);
            measureChildWithExactlySize(view);
            int left = (getHorizontalSpace() - mItemViewWidth) / 2;
            layoutDecoratedWithMargins(view, left, layoutInfo.getTop(), left + mItemViewWidth, layoutInfo.getTop() + mItemViewHeight);
            view.setPivotX(view.getWidth()/2);
            view.setPivotY(0);
            view.setScaleX(layoutInfo.getScaleXY());
            view.setScaleY(layoutInfo.getScaleXY());
        }

        Log.d("data", " childView count:" + getChildCount());
    }

    /**
     * 测量itemview的确切大小
     */
    private void measureChildWithExactlySize(View child)
    {
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(mItemViewWidth, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(mItemViewHeight, View.MeasureSpec.EXACTLY);
        child.measure(widthSpec, heightSpec);
    }

    /**
     * 获取RecyclerView的显示高度
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

