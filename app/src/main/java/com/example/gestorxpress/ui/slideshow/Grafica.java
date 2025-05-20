package com.example.gestorxpress.ui.slideshow;

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

    /**
     * Explicación de porque uso CANVA y PAINT.
     * .
     * Uso PAINT y CANVA porque no me dejaba implementar una dependecia que ya me dibujaba
     * los graficos, entonces decidimos utilizamos utilizar otra solución donde nosotros mismo
     * dibujamos la grafica segun los datos que queremos.
     * .
     * .
     * Uso de PAINT:
     * Paint se usa para definir cómo se dibujan los elementos: color,
     * estilo de trazo, tamaño de texto, etc.
     * .
     * .
     * Uso de CANVAS:
     * Canvas es el objeto que representa el lienzo sobre el cual dibujas.
     * Lo utilizas en el método onDraw(Canvas canvas):
     */

    private Map<String, Integer> datos = new LinkedHashMap<>();
    private float animacionProgreso = 0f;

    private Paint paintBar;
    private Paint paintText;
    private Paint paintAxis;
    private Paint paintLineaPromedio;

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
        paintText.setTextSize(40f);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintAxis = new Paint();
        paintAxis.setColor(Color.DKGRAY);
        paintAxis.setStrokeWidth(4f);

        paintLineaPromedio = new Paint();
        paintLineaPromedio.setColor(Color.RED);
        paintLineaPromedio.setStrokeWidth(3f);
        paintLineaPromedio.setStyle(Paint.Style.STROKE);
        paintLineaPromedio.setPathEffect(null);
    }

    public void setDatos(Map<String, Integer> datos) {
        this.datos = datos;
        iniciarAnimacion();
    }

    private void iniciarAnimacion() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(800);
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

        int paddingLeft = 100;
        int paddingRight = 100;
        int paddingTop = 100;
        int paddingBottom = 200;

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
        int barWidth = Math.min(100, availableWidth / (numBarras * 2));
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

            // Valor encima de la barra
            canvas.drawText(String.valueOf(valor), left + barWidth / 2f, top - 10, paintText);

            // Fecha debajo de la barra (abreviada)
            canvas.drawText(abreviarFecha(fecha), left + barWidth / 2f, height - paddingBottom + 50, paintText);

            index++;
        }

        // Dibujar línea horizontal de promedio
        float promedioY = height - paddingBottom - ((promedio / maxValue) * chartHeight * animacionProgreso);
        canvas.drawLine(paddingLeft, promedioY, width - paddingRight, promedioY, paintLineaPromedio);
        canvas.drawText("Promedio: " + String.format("%.1f", promedio), width - paddingRight, promedioY - 10, paintText);
    }

    // Formato de fecha "YYYY-MM-DD" → "DD/MM"
    private String abreviarFecha(String fecha) {
        try {
            String[] partes = fecha.split("-");
            return partes[2] + "/" + partes[1];
        } catch (Exception e) {
            return fecha;
        }
    }
}
