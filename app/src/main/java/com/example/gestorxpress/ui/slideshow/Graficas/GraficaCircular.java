package com.example.gestorxpress.ui.slideshow.Graficas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraficaCircular extends View
{

    private Paint paint;
    private RectF rectF;

    private List<String> etiquetas = new ArrayList<>();
    private List<Integer> valores = new ArrayList<>();
    private List<Integer> colores = new ArrayList<>();

    public GraficaCircular(Context context)
    {
        super(context);
        init();
    }

    public GraficaCircular(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
    }

    public void setDatos(Map<String, Integer> datos)
    {
        etiquetas.clear();
        valores.clear();
        colores.clear();

        int[] coloresBase =
                {
                Color.parseColor("#FF5722"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#009688")
        };

        int i = 0;
        for (Map.Entry<String, Integer> entry : datos.entrySet())
        {
            etiquetas.add(entry.getKey());
            valores.add(entry.getValue());
            colores.add(coloresBase[i % coloresBase.length]);
            i++;
        }

        invalidate();
    }

    public List<Integer> getColores() {
        return colores;
    }

    public List<String> getEtiquetas() {
        return etiquetas;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (valores.isEmpty())
        {
            paint.setColor(Color.GRAY);
            paint.setTextSize(40);
            canvas.drawText("No hay datos", getWidth()/3f, getHeight()/2f, paint);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int min = Math.min(width, height) - 100;
        int padding = 50;

        rectF.set(padding, padding, padding + min, padding + min);

        int total = 0;
        for (int val : valores) total += val;

        float startAngle = -90f;
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < valores.size(); i++)
        {
            float sweepAngle = (valores.get(i) * 360f) / total;
            paint.setColor(colores.get(i));
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }

        // Leyenda
        paint.setTextSize(36);
        paint.setColor(Color.BLACK);

        int leyendaX = padding;
        int leyendaY = padding + min + 60;
        int boxSize = 40;
        int espacio = 20;

        for (int i = 0; i < etiquetas.size(); i++)
        {
            paint.setColor(colores.get(i));
            canvas.drawRect(leyendaX, leyendaY - boxSize, leyendaX + boxSize, leyendaY, paint);

            paint.setColor(Color.BLACK);
            canvas.drawText(etiquetas.get(i), leyendaX + boxSize + 10, leyendaY - 10, paint);

            leyendaX += paint.measureText(etiquetas.get(i)) + boxSize + espacio + 20;
            if (leyendaX > width - 200)
            {
                leyendaX = padding;
                leyendaY += boxSize + 30;
            }
        }
    }
}
