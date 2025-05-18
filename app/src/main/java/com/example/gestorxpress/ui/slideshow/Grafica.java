package com.example.gestorxpress.ui.slideshow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Map;

public class Grafica extends View
{

    private int tareasRealizadas = 0;
    private int porcentaje = 0;

    private Paint paintBarRealizadas;
    private Paint paintBarPendientes;
    private Paint paintText;
    private Paint paintAxis;

    public Grafica(Context context)
    {
        super(context);
        init();
    }

    public Grafica(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public Grafica(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        paintBarRealizadas = new Paint();
        paintBarRealizadas.setColor(Color.parseColor("#3F51B5")); // Azul vivo
        paintBarRealizadas.setStyle(Paint.Style.FILL);

        paintBarPendientes = new Paint();
        paintBarPendientes.setColor(Color.parseColor("#B0BEC5")); // Gris claro
        paintBarPendientes.setStyle(Paint.Style.FILL);

        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(60f);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintAxis = new Paint();
        paintAxis.setColor(Color.DKGRAY);
        paintAxis.setStrokeWidth(4f);
    }

    public void setDatos(Map<String, Integer> datos)
    {
        // Ejemplo simple: imprimir datos recibidos
        for (Map.Entry<String, Integer> entry : datos.entrySet()) {
            Log.d("Grafica", "Fecha: " + entry.getKey() + " | Total: " + entry.getValue());
        }

        // Aquí deberías hacer el redibujo de la gráfica usando estos datos
        invalidate(); // Si es una vista personalizada
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int paddingLeft = 150;
        int paddingRight = 150;
        int paddingTop = 100;
        int paddingBottom = 200;

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        // Dibujar eje horizontal
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, paintAxis);
        // Dibujar eje vertical
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, paintAxis);

        // Definimos max valor para escala (puede ser tareasRealizadas o porcentaje, aquí usamos 100 para %)
        int maxValue = 100;

        // Ancho barras
        int barWidth = 150;

        // Espacio entre barras
        int spaceBetweenBars = 150;

        // Coordenadas para barra "Tareas Realizadas"
        float tareasHeight = ((float) tareasRealizadas / maxValue) * chartHeight;
        tareasHeight = Math.min(tareasHeight, chartHeight); // Para no sobrepasar

        float tareasLeft = paddingLeft + spaceBetweenBars;
        float tareasTop = height - paddingBottom - tareasHeight;
        float tareasRight = tareasLeft + barWidth;
        float tareasBottom = height - paddingBottom;

        // Coordenadas para barra "Porcentaje"
        float porcentajeHeight = ((float) porcentaje / maxValue) * chartHeight;
        porcentajeHeight = Math.min(porcentajeHeight, chartHeight);

        float porcentajeLeft = tareasRight + spaceBetweenBars;
        float porcentajeTop = height - paddingBottom - porcentajeHeight;
        float porcentajeRight = porcentajeLeft + barWidth;
        float porcentajeBottom = height - paddingBottom;

        // Dibujar barras
        canvas.drawRect(tareasLeft, tareasTop, tareasRight, tareasBottom, paintBarRealizadas);
        canvas.drawRect(porcentajeLeft, porcentajeTop, porcentajeRight, porcentajeBottom, paintBarPendientes);

        // Dibujar valores encima de las barras
        canvas.drawText(String.valueOf(tareasRealizadas), tareasLeft + barWidth / 2f, tareasTop - 20, paintText);
        canvas.drawText(porcentaje + "%", porcentajeLeft + barWidth / 2f, porcentajeTop - 20, paintText);

        // Dibujar etiquetas debajo de las barras
        paintText.setTextSize(50f);
        canvas.drawText("Tareas", tareasLeft + barWidth / 2f, height - paddingBottom + 60, paintText);
        canvas.drawText("Porcentaje", porcentajeLeft + barWidth / 2f, height - paddingBottom + 60, paintText);
    }

}
