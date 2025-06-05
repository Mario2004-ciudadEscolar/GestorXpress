package com.example.gestorxpress.ui.slideshow.Graficas;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
import java.util.TreeMap;
import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;

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

    // Declaramos el formateador para fechas "yyyy-MM-dd"
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


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
        paintBar.setColor(Color.parseColor("#FF5722"));
        paintBar.setStyle(Paint.Style.FILL);

        // Pintura para los textos
        paintText = new Paint();
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(dpToPx(16));
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setAntiAlias(true);

        // Pintura para los ejes
        paintAxis = new Paint();
        paintAxis.setColor(Color.parseColor("#E0E0E0"));
        paintAxis.setStrokeWidth(dpToPx(2));

    }

    /**
     * Este metodo convierte una cantidad en dp (density-independent pixels)
     * a píxeles (px) según la densidad de pantalla.
     * Esto es útil para adaptar tamaños visuales en diferentes
     * dispositivos con distintas densidades.
     *
     * @param dp Valor en dp que se desea convertir.
     * @return Valor equivalente en píxeles (px).
     */
    private float dpToPx(float dp)
    {
        // Multiplica los dp por la densidad de pantalla para obtener px
        return dp * getResources().getDisplayMetrics().density;
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

        // Actualizamos el progreso de la animación y redibuja la vista
        animator.addUpdateListener(animation ->
        {
            animacionProgreso = (float) animation.getAnimatedValue();
            invalidate(); // Redibuja la vista
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

        if (datos == null || datos.isEmpty()) return;

        // Obtiene ancho y alto total del canvas
        int width = getWidth();
        int height = getHeight();

        // Espaciado interno
        int paddingLeft = (int) dpToPx(80);
        int paddingRight = (int) dpToPx(60);
        int paddingTop = (int) dpToPx(80);
        int paddingBottom = (int) dpToPx(120);

        // Altura disponible para las barras
        int chartHeight = height - paddingTop - paddingBottom;

        // Dibuja eje X (horizontal)
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, paintAxis);

        // Dibuja eje Y (vertical)
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, paintAxis);

        int numBarras = datos.size(); // Número de días con tareas
        if (numBarras == 0) return; // Si no hay datos, no hace nada

        // Dibuja las etiquetas del eje Y de 0 a 5
        // Osea que dibujamos a mano la parte izquiera del grafico, que eso
        // indicaria cuantas tareas tiene realiado en el dia
        int maxEscala = 10;
        for (int i = 0; i <= maxEscala; i++)
        {
            float y = height - paddingBottom - ((float) i / maxEscala) * chartHeight;
            canvas.drawText(String.valueOf(i), paddingLeft - dpToPx(30), y + dpToPx(10), paintText);
        }

        // Calcula el total pero fija el valor máximo en 5 porque es el límite del eje Y
        int maxValue = 10;

        for (int valor : datos.values())
        {
            if (valor > maxValue) maxValue = valor;
        }

        // Calcula el ancho de las barras y su espaciado para que entren 7 barras con margen
        int availableWidth = width - paddingLeft - paddingRight;
        int marginBetweenBars = (int) dpToPx(24);  // Margen fijo entre barras
        int barWidth = Math.min((availableWidth - marginBetweenBars * (numBarras - 1)) / numBarras, (int) dpToPx(48));

        int index = 0;

        /// Dibuja el nombre del mes y lo colacamos en la parte izquierda
        String primeraFecha = datos.keySet().iterator().next();
        int mes = Integer.parseInt(primeraFecha.substring(5, 7)) - 1;
        String primerMesAbreviado = new DateFormatSymbols(Locale.getDefault()).getShortMonths()[mes];
        canvas.drawText(primerMesAbreviado, paddingLeft - dpToPx(40), height - paddingBottom + dpToPx(60), paintText);


        // Recorrer los datos y dibujar cada barra
        for (Map.Entry<String, Integer> entry : datos.entrySet())
        {
            String fecha = entry.getKey(); // Fecha tipo yyyy-MM-dd
            int valor = entry.getValue(); // Número de tareas

            // Coordenadas de la barra
            float left = paddingLeft + (barWidth + marginBetweenBars) * index;
            float right = left + barWidth;
            float bottom = height - paddingBottom;

            // Solo dibujar la barra y el número si hay tareas
            if (valor > 0)
            {
                // Calcula altura proporcional de la barra según su valor
                float barHeight = ((float) valor / maxValue) * chartHeight * animacionProgreso;

                // Coordenadas de la barra
                float top = height - paddingBottom - barHeight;

                // Dibujar barra en pantall
                canvas.drawRect(left, top, right, bottom, paintBar);

                // Dibuja el número de tareas arriba de cada barra
                canvas.drawText(String.valueOf(valor), left + barWidth / 2f, top - dpToPx(8), paintText);
            }

            // Dibuja solo el número de día debajo de cada barra
            String dia = fecha.length() >= 10 ? fecha.substring(8, 10) : fecha;
            canvas.drawText(dia, left + barWidth / 2f, height - paddingBottom + dpToPx(60), paintText);


            index++;
        }
    }


    /**
     * Este metodo filtra un mapa con datos diarios para devolver
     * sólo los datos correspondientes a la semana actual.
     * .
     * La semana actual se considera de lunes a domingo,
     * y se ajusta según la fecha de hoy.
     *
     * @param originales Mapa con datos originales donde la clave es la fecha en formato String ("2025-05-31")
     *                   y el valor es la cantidad de tareas completadas ese día.
     * @return Devuelve un nuevo mapa con las fechas de la semana actual.
     */
    public Map<String, Integer> obtenerDatosSemanaActual(Map<String, Integer> originales)
    {
        // Usamos TreeMap para que los datos estén ordenados por fecha
        Map<String, Integer> datosSemana = new TreeMap<>();

        // Obtiene la fecha actual y la pone a medianoche (sin horas, minutos ni segundos)
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);

        // Establece el lunes como primer día de la semana para el cálculo
        hoy.setFirstDayOfWeek(Calendar.MONDAY);

        // Obtenemos el día de la semana actual
        int diaSemana = hoy.get(Calendar.DAY_OF_WEEK);

        // Calcula el offset (días que hay que restar para llegar al lunes de esta semana)
        // Si es domingo, retrocede 6 días para llegar al lunes
        // Si es otro día, ajusta con base al día actual y lunes
        int offset = (diaSemana == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - diaSemana;
        hoy.add(Calendar.DAY_OF_MONTH, offset); //Con esto ajustamos al lunes de la semana actual

        int mesActual = hoy.get(Calendar.MONTH); // mes actual desde el lunes de esta semana

        // Itera 7 días desde el lunes, agregando las fechas y valores al mapa resultado
        for (int i = 0; i < 7; i++)
        {
            /*int mes = hoy.get(Calendar.MONTH);
            if (mes != mesActual) break; // dejamos de agregar días si cambia de mes*/

            // Conviertimos la fecha a String con el formato esperado
            String fechaStr = formatoFecha.format(hoy.getTime());

            // Obtemos el valor original para esa fecha, o 0 si no hay dato
            int valor = originales.containsKey(fechaStr) ? originales.get(fechaStr) : 0;

            // Añadimos la fecha y el valor (incluso si es 0) para que se vea en la gráfica
            datosSemana.put(fechaStr, valor);

            // Avanzamos al siguiente día
            hoy.add(Calendar.DAY_OF_MONTH, 1);
        }
        // Devolvemos el mapa con sólo los datos de la semana actual
        return datosSemana;
    }

    /**
     * Este metodo es para establecer los datos filtrados de la semana actual en la gráfica.
     * Recibe el mapa con todos los datos originales, filtra la semana actual y luego
     * pasa ese subconjunto al método que dibuja la gráfica.
     *
     * @param datosOriginales Mapa con todos los datos originales (fecha -> cantidad).
     */
    public void setDatosSemanaActual(Map<String, Integer> datosOriginales)
    {
        Map<String, Integer> datosSemana = obtenerDatosSemanaActual(datosOriginales);
        setDatos(datosSemana);
    }
}
