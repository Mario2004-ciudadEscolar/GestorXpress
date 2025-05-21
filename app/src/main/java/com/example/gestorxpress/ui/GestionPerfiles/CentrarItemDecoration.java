package com.example.gestorxpress.ui.GestionPerfiles;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CentrarItemDecoration extends RecyclerView.ItemDecoration
{
    private final int itemWidthPx;
    private final int itemMarginPx;

    public CentrarItemDecoration(int itemWidthPx, int itemMarginPx)
    {
        this.itemWidthPx = itemWidthPx;
        this.itemMarginPx = itemMarginPx;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);
        int itemCount = state.getItemCount();

        int totalWidth = (itemWidthPx + itemMarginPx * 2) * itemCount;
        int recyclerWidth = parent.getWidth();

        if (totalWidth < recyclerWidth)
        {
            // Calcula el padding lateral para centrar los items
            int sidePadding = (recyclerWidth - totalWidth) / 2;

            if (position == 0)
            {
                outRect.left = sidePadding + itemMarginPx;
                outRect.right = itemMarginPx;
            }
            else if (position == itemCount - 1)
            {
                outRect.left = itemMarginPx;
                outRect.right = sidePadding + itemMarginPx;
            }
            else
            {
                outRect.left = itemMarginPx;
                outRect.right = itemMarginPx;
            }
        }
        else
        {
            // Sin centrar: márgenes normales para permitir scroll
            outRect.left = itemMarginPx;
            outRect.right = itemMarginPx;
        }
    }
}
