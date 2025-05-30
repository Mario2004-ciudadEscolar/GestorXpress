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

/**
 * Autor: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class GraficaCircular extends View
{

    private Paint paint; // Objeto Paint para dibujar
    private RectF rectF; // Rectángulo delimitador para el círculo

    // Colección que lo voy a útilizar para dibujar el grafico
    private List<String> etiquetas = new ArrayList<>();
    private List<Integer> valores = new ArrayList<>();
    private List<Integer> colores = new ArrayList<>();

    /**
     * Constructor para uso programático de la vista.
     * @param context contexto de la aplicación
     */
    public GraficaCircular(Context context)
    {
        super(context);
        init();
    }

    /**
     * Constructor utilizado cuando se define la vista en XML.
     * @param context contexto de la aplicación
     * @param attrs atributos definidos en XML
     */
    public GraficaCircular(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    /**
     * Inicializa los objetos gráficos necesarios para la gráfica.
     */
    private void init()
    {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG); // Para bordes suaves
        rectF = new RectF(); // Inicializa el rectángulo donde se dibuja el círculo
    }

    /**
     * Asigna nuevos datos a la gráfica circular.
     * @param datos mapa con claves (etiquetas) y valores (números).
     */
    public void setDatos(Map<String, Integer> datos)
    {
        etiquetas.clear();
        valores.clear();
        colores.clear();

        // Colores predeterminados que se irán reutilizando en ciclo
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
            colores.add(coloresBase[i % coloresBase.length]); // Reutiliza colores
            i++;
        }

        invalidate(); // Redibuja la vista
    }

    /**
     * Obtiene los colores asignados a cada sector.
     * @return lista de colores en orden
     */
    public List<Integer> getColores() {
        return colores;
    }

    /**
     * Obtiene las etiquetas de los sectores.
     * @return lista de etiquetas en orden
     */
    public List<String> getEtiquetas() {
        return etiquetas;
    }

    /**
     * Este método se encarga de dibujar la gráfica circular y su leyenda.
     * Se llama automáticamente cuando se debe redibujar la vista.
     * @param canvas el lienzo sobre el que se dibujan los elementos
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // Si no hay datos, muestra un mensaje informativo
        if (valores.isEmpty())
        {
            paint.setColor(Color.GRAY);
            paint.setTextSize(40);
            canvas.drawText("No hay datos", getWidth()/3f, getHeight()/2f, paint);
            return;
        }

        int width = getWidth(); // Ancho
        int height = getHeight(); // Altura

        // Define el tamaño del gráfico circular con margen
        int min = Math.min(width, height) - 100;
        int padding = 50;
        rectF.set(padding, padding, padding + min, padding + min);

        // Suma total para calcular porcentajes
        int total = 0;
        for (int val : valores) total += val;

        float startAngle = -90f; // Comienza desde arriba
        paint.setStyle(Paint.Style.FILL);

        // Dibuja cada sector circular
        for (int i = 0; i < valores.size(); i++)
        {
            float sweepAngle = (valores.get(i) * 360f) / total; // Proporción
            paint.setColor(colores.get(i));
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }

        // Dibuja la leyenda
        paint.setTextSize(36);
        paint.setColor(Color.BLACK);

        int leyendaX = padding;
        int leyendaY = padding + min + 60;
        int boxSize = 40; // Tamaño del recuadro de color
        int espacio = 20; // Espacio entre leyendas

        for (int i = 0; i < etiquetas.size(); i++)
        {
            // Recuadro de color
            paint.setColor(colores.get(i));
            canvas.drawRect(leyendaX, leyendaY - boxSize, leyendaX + boxSize, leyendaY, paint);

            // Texto de la etiqueta
            paint.setColor(Color.BLACK);
            canvas.drawText(etiquetas.get(i), leyendaX + boxSize + 10, leyendaY - 10, paint);

            // Mover posición horizontalmente
            leyendaX += paint.measureText(etiquetas.get(i)) + boxSize + espacio + 20;

            // Si se sale del ancho, pasa a la siguiente línea
            if (leyendaX > width - 200)
            {
                leyendaX = padding;
                leyendaY += boxSize + 30;
            }
        }
    }
}
