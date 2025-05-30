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

/**
 * Autor: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class Grafica extends View
{
    // Colección donde vamos a guardar los datos y luego hacer el grafico con ellos
    private Map<String, Integer> datos = new LinkedHashMap<>();

    // Progreso de animación (0 a 1) para animar la aparición de las barras.
    private float animacionProgreso = 0f;

    // Objetos Paint para dibujar distintos elementos.
    private Paint paintBar;   // Para las barras
    private Paint paintText;  // Para los textos
    private Paint paintAxis;  // Para los ejes
    private Paint paintLineaPromedio;  // Para la línea del promedio
    private Paint paintBarGradient;

    /**
     * Constructor principal de la vista.
     * @param context contexto del sistema
     */
    public Grafica(Context context)
    {
        super(context);
        init();
    }

    /**
     * Constructor utilizado al definir la vista en XML.
     * @param context contexto del sistema
     * @param attrs atributos definidos en XML
     */
    public Grafica(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    /**
     * Constructor utilizado con estilo personalizado.
     * @param context contexto del sistema
     * @param attrs atributos definidos en XML
     * @param defStyleAttr estilo personalizado
     */
    public Grafica(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Inicializa todos los objetos Paint necesarios para dibujar la gráfica.
     */
    private void init()
    {
        // Pintura para las barras
        paintBar = new Paint();
        paintBar.setColor(Color.parseColor("#3F51B5"));
        paintBar.setStyle(Paint.Style.FILL);

        // Pintura para los textos
        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(36f);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setAntiAlias(true);

        // Pintura para los ejes
        paintAxis = new Paint();
        paintAxis.setColor(Color.DKGRAY);
        paintAxis.setStrokeWidth(3f);

        // Pintura para la línea del promedio
        paintLineaPromedio = new Paint();
        paintLineaPromedio.setColor(Color.RED);
        paintLineaPromedio.setStrokeWidth(3f);
        paintLineaPromedio.setStyle(Paint.Style.STROKE);
    }

    /**
     * Asigna nuevos datos a la gráfica y lanza la animación.
     * @param datos Mapa con fechas como claves y valores numéricos como valores.
     */
    public void setDatos(Map<String, Integer> datos)
    {
        this.datos = datos;
        iniciarAnimacion();
    }

    /**
     * Inicia la animación que hace crecer las barras desde cero hasta su altura real.
     */
    private void iniciarAnimacion()
    {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f); // Animación de 0 a 1
        animator.setDuration(900); // Duración en milisegundos
        animator.setInterpolator(new DecelerateInterpolator()); // Efecto de desaceleración

        // Actualiza el progreso de la animación y redibuja la vista
        animator.addUpdateListener(animation ->
        {
            animacionProgreso = (float) animation.getAnimatedValue();
            invalidate(); // Fuerza el redibujado
        });
        animator.start(); // Inicia la animación
    }

    /**
     * Dibuja la gráfica de barras en el canvas.
     * Se llama automáticamente cuando se debe redibujar la vista.
     * @param canvas el lienzo sobre el que se dibujan los elementos
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int width = getWidth();   // Ancho total de la vista
        int height = getHeight(); // Alto total de la vista

        // Espaciado interno
        int paddingLeft = 80;
        int paddingRight = 80;
        int paddingTop = 80;
        int paddingBottom = 160;

        // Altura disponible para las barras
        int chartHeight = height - paddingTop - paddingBottom;

        // Dibuja los ejes X y Y
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, paintAxis);
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, paintAxis);

        int numBarras = datos.size();
        if (numBarras == 0) return;

        // Calcula el valor máximo y promedio
        int maxValue = 0;
        int total = 0;

        for (int valor : datos.values())
        {
            if (valor > maxValue) maxValue = valor;
            total += valor;
        }

        maxValue = Math.max(maxValue, 1); // Evita división por cero
        float promedio = (float) total / numBarras;

        // Calcular el ancho de las barras y su espaciado
        int availableWidth = width - paddingLeft - paddingRight;
        int barWidth = Math.min(80, availableWidth / (numBarras * 2));
        int spaceBetween = (availableWidth - (barWidth * numBarras)) / (numBarras + 1);

        int index = 0;

        // Recorrer los datos y dibujar cada barra
        for (Map.Entry<String, Integer> entry : datos.entrySet())
        {
            String fecha = entry.getKey();
            int valor = entry.getValue();

            // Altura de la barra animada
            float barHeight = ((float) valor / maxValue) * chartHeight * animacionProgreso;

            // Posiciones para la barra
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

    /**
     * Abrevia la fecha al formato "dd/MM" para mostrarla debajo de cada barra.
     * @param fecha Fecha en formato "yyyy-MM-dd"
     * @return Fecha abreviada como "dd/MM", o la original si hay error.
     */
    private String abreviarFecha(String fecha)
    {
        try
        {
            String[] partes = fecha.split("-");
            return partes[2] + "/" + partes[1]; // Retorna día/mes
        }
        catch (Exception e)
        {
            return fecha; // Si falla, retorna la fecha origina
        }
    }
}
