package com.example.gestorxpress.ui.slideshow.Graficas;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class Grafica extends View {

    private Map<String, Integer> datos = new LinkedHashMap<>();
    private float animacionProgreso = 0f;

    private Paint paintBar;
    private Paint paintText;
    private Paint paintAxis;
    private Paint paintLineaPromedio;
    private Paint paintBarGradient;

    public Grafica(Context context) {
        super(context);
        init();
    }

    public Grafica(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Grafica(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintBar = new Paint();
        paintBar.setColor(Color.parseColor("#3F51B5"));
        paintBar.setStyle(Paint.Style.FILL);

        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(36f);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setAntiAlias(true);

        paintAxis = new Paint();
        paintAxis.setColor(Color.DKGRAY);
        paintAxis.setStrokeWidth(3f);

        paintLineaPromedio = new Paint();
        paintLineaPromedio.setColor(Color.RED);
        paintLineaPromedio.setStrokeWidth(3f);
        paintLineaPromedio.setStyle(Paint.Style.STROKE);
    }

    public void setDatos(Map<String, Integer> datos) {
        this.datos = datos;
        iniciarAnimacion();
    }

    private void iniciarAnimacion() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(900);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animacionProgreso = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int paddingLeft = 80;
        int paddingRight = 80;
        int paddingTop = 80;
        int paddingBottom = 160;

        int chartHeight = height - paddingTop - paddingBottom;

        // Dibujar ejes
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, paintAxis);
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, paintAxis);

        int numBarras = datos.size();
        if (numBarras == 0) return;

        int maxValue = 0;
        int total = 0;
        for (int valor : datos.values()) {
            if (valor > maxValue) maxValue = valor;
            total += valor;
        }
        maxValue = Math.max(maxValue, 1); // Evitar división por cero
        float promedio = (float) total / numBarras;

        int availableWidth = width - paddingLeft - paddingRight;
        int barWidth = Math.min(80, availableWidth / (numBarras * 2));
        int spaceBetween = (availableWidth - (barWidth * numBarras)) / (numBarras + 1);

        int index = 0;
        for (Map.Entry<String, Integer> entry : datos.entrySet()) {
            String fecha = entry.getKey();
            int valor = entry.getValue();

            float barHeight = ((float) valor / maxValue) * chartHeight * animacionProgreso;
            float left = paddingLeft + spaceBetween * (index + 1) + barWidth * index;
            float top = height - paddingBottom - barHeight;
            float right = left + barWidth;
            float bottom = height - paddingBottom;

            // Dibujar barra
            canvas.drawRect(left, top, right, bottom, paintBar);

            // Dibujar valor sobre la barra
            canvas.drawText(String.valueOf(valor), left + barWidth / 2f, top - 12, paintText);

            // Dibujar fecha abreviada debajo de la barra
            canvas.drawText(abreviarFecha(fecha), left + barWidth / 2f, height - paddingBottom + 50, paintText);

            index++;
        }

        // Dibujar línea horizontal de promedio
        float promedioY = height - paddingBottom - ((promedio / maxValue) * chartHeight * animacionProgreso);
        canvas.drawLine(paddingLeft, promedioY, width - paddingRight, promedioY, paintLineaPromedio);
        canvas.drawText("Promedio: " + String.format("%.1f", promedio), width - paddingRight + 10, promedioY - 10, paintText);
    }

    private String abreviarFecha(String fecha) {
        try {
            String[] partes = fecha.split("-");
            return partes[2] + "/" + partes[1];
        } catch (Exception e) {
            return fecha;
        }
    }
}
