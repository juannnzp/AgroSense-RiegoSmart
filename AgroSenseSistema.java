package agrosense;

import java.util.Locale;

public class AgroSenseSistema {

    // ==================================================
    // CONFIGURACIÓN AP / SERVIDOR (REFERENCIA)
    // ==================================================
    public static final String NOMBRE_RED = "AgroSenseESP32";
    public static final String CLAVE_RED = "AgroSense123";
    public static final int PUERTO_SERVIDOR = 80;

    // ==================================================
    // CALIBRACIÓN Y UMBRALES
    // ==================================================
    public static final int VALOR_SECO = 4095;
    public static final int VALOR_MOJADO = 1200;
    public static final int LIMITE_SECO = 3000;

    public static final double UMBRAL_DIST_CM = 14.0;
    public static final int UMBRAL_AGUA = 1500;
    public static final boolean RELE_ACTIVO_HIGH = true;
    public static final long DURACION_ALERTA_MS = 2500L;

    // ==================================================
    // TEMPORIZADORES (ms)
    // ==================================================
    public static final long INTERVALO_PARPADEO = 300L;
    public static final long INTERVALO_SERIAL = 500L;
    public static final long INTERVALO_DHT = 2000L;
    public static final long INTERVALO_LECTURA = 150L;

    // ==================================================
    // ESTADO DE SENSORES Y ACTUADORES
    // ==================================================
    private int humedad = 0;
    private int porcentajeHumedad = 0;
    private double temperatura = 0.0;
    private double humedadAmbiente = 0.0;

    private double distCm = -1.0;
    private int valAgua = 0;
    private boolean pirDetectado = false;

    private boolean bombaActiva = false;
    private boolean estadoRelePrevio = false;
    private boolean inicioHecho = false;
    private int alertaTipo = 0;
    private int estadoActual = 0;
    private boolean estadoParpadeo = false;

    private boolean bombaRiegoEncendida = false;
    private boolean bombaTanqueEncendida = false;
    private boolean buzzerEncendido = false;
    private boolean ledRojoEncendido = false;
    private boolean ledVerdeEncendido = false;
    private boolean ledAlertaEncendido = false;
    private boolean ledNormalEncendido = true;
    private boolean ledAmarilloEncendido = false;
    private String[] lcdFilas = { "                ", "                " };

    private long tiempoAnterior = 0L;
    private long tiempoSerial = 0L;
    private long tiempoDHT = 0L;
    private long tAlerta = 0L;
    private long tLectura = 0L;

    public void actualizarLecturas(
            long ahora,
            int lecturaSuelo,
            Double nuevaTemp,
            Double nuevaHumAmb,
            Double nuevaDistCm,
            Integer nuevoValAgua,
            boolean pir
    ) {
        this.humedad = lecturaSuelo;
        this.porcentajeHumedad = mapConstrained(humedad, VALOR_SECO, VALOR_MOJADO, 0, 100);

        if (!inicioHecho || (ahora - tiempoDHT >= INTERVALO_DHT)) {
            tiempoDHT = ahora;
            if (nuevaTemp != null && !nuevaTemp.isNaN()) {
                this.temperatura = nuevaTemp;
            }
            if (nuevaHumAmb != null && !nuevaHumAmb.isNaN()) {
                this.humedadAmbiente = nuevaHumAmb;
            }
        }

        if (!inicioHecho || (ahora - tLectura >= INTERVALO_LECTURA)) {
            tLectura = ahora;
            if (nuevaDistCm != null) {
                this.distCm = nuevaDistCm;
            }
            if (nuevoValAgua != null) {
                this.valAgua = nuevoValAgua;
            }
        }

        this.pirDetectado = pir;
        this.ledAmarilloEncendido = pirDetectado;

        boolean aguaLimite = (valAgua >= UMBRAL_AGUA);
        boolean tanqueVacio = (distCm >= UMBRAL_DIST_CM);

        if (aguaLimite) {
            bombaActiva = false;
            alertaTipo = 1;
            tAlerta = ahora;
        } else if (tanqueVacio) {
            bombaActiva = true;
            alertaTipo = 2;
            tAlerta = ahora;
        }

        if (alertaTipo != 0 && (ahora - tAlerta > DURACION_ALERTA_MS)) {
            alertaTipo = 0;
        }

        bombaTanqueEncendida = (bombaActiva && !aguaLimite);
        boolean cambioRele = !inicioHecho || (bombaTanqueEncendida != estadoRelePrevio);
        estadoRelePrevio = bombaTanqueEncendida;

        boolean sueloSeco = (humedad >= LIMITE_SECO);
        if (sueloSeco) {
            if (estadoActual != 1) {
                estadoActual = 1;
                System.out.println("---------------------------------------");
                System.out.println("*** ALERTA: Suelo seco - Bomba ACTIVADA ***");
            }
            bombaRiegoEncendida = true;
            ledVerdeEncendido = false;

            if (ahora - tiempoAnterior >= INTERVALO_PARPADEO) {
                tiempoAnterior = ahora;
                estadoParpadeo = !estadoParpadeo;
                ledRojoEncendido = estadoParpadeo;
                if (!aguaLimite && !(tanqueVacio || alertaTipo == 2)) {
                    buzzerEncendido = estadoParpadeo;
                }
            }
        } else {
            if (estadoActual != 2) {
                estadoActual = 2;
                System.out.println("---------------------------------------");
                System.out.println("*** Suelo húmedo - Bomba APAGADA ***");
            }
            bombaRiegoEncendida = false;
            ledRojoEncendido = false;
            ledVerdeEncendido = true;
            estadoParpadeo = false;

            if (!aguaLimite && !(tanqueVacio || alertaTipo == 2)) {
                buzzerEncendido = false;
            }
        }

        boolean sonarPipTanque = (!aguaLimite && (tanqueVacio || alertaTipo == 2));
        if (sonarPipTanque) {
            long pos = ahora % 1200L;
            if (pos < 720L) {
                buzzerEncendido = ((pos % 180L) < 90L);
            } else {
                buzzerEncendido = false;
            }
        }

        if (cambioRele) {
            lcdFilas[0] = formatearFila16("");
            lcdFilas[1] = formatearFila16("");
        }

        String l1 = (distCm >= 0.0)
                ? String.format(Locale.US, "D:%.1fcm %s", distCm, bombaTanqueEncendida ? "B:ON" : "B:OFF")
                : String.format(Locale.US, "D:--.-cm %s", bombaTanqueEncendida ? "B:ON" : "B:OFF");

        if (alertaTipo == 1) {
            ledAlertaEncendido = true;
            ledNormalEncendido = false;
            lcdFilas[0] = formatearFila16("AGUA AL LÍMITE");
            lcdFilas[1] = formatearFila16("BOMBA: APAGADA");
        } else if (alertaTipo == 2) {
            ledAlertaEncendido = true;
            ledNormalEncendido = false;
            lcdFilas[0] = formatearFila16("TANQUE VACÍO");
            lcdFilas[1] = formatearFila16(l1);
        } else {
            ledAlertaEncendido = false;
            ledNormalEncendido = true;
            lcdFilas[0] = formatearFila16("ESTADO NORMAL");
            lcdFilas[1] = formatearFila16(l1);
        }

        if (!inicioHecho || (ahora - tiempoSerial >= INTERVALO_SERIAL)) {
            tiempoSerial = ahora;
            System.out.println("---------------------------------------");
            System.out.println("Sensor suelo: " + humedad);
            System.out.println("Humedad suelo: " + porcentajeHumedad + "%");
            System.out.println("Temperatura: " + String.format("%.1f", temperatura) + " C");
            System.out.println("Humedad ambiente: " + String.format("%.1f", humedadAmbiente) + "%");
            System.out.println("Distancia: " + String.format("%.1f", distCm) + " cm");
            System.out.println("Nivel de agua: " + valAgua);
        }

        inicioHecho = true;
    }

    public String construirJsonDatos() {
        boolean sueloSeco = (humedad >= LIMITE_SECO);
        String estadoSuelo = sueloSeco ? "SECO" : "HÚMEDO";
        String estadoBombaRiego = sueloSeco ? "ENCENDIDA" : "APAGADA";
        String estadoZumbadorRiego = sueloSeco ? "ENCENDIDO" : "APAGADO";
        String estadoBombaTanque = bombaTanqueEncendida ? "ENCENDIDA" : "APAGADA";

        return String.format(
                Locale.US,
                "{\"humedad\":%d,\"temperatura\":%.1f,\"humedadAmbiente\":%.1f,\"valorSensor\":%d,"
                        + "\"estado\":\"%s\",\"bomba\":\"%s\",\"zumbador\":\"%s\","
                        + "\"distanciaCm\":%.1f,\"valorAgua\":%d,\"bombaTanque\":\"%s\","
                        + "\"alertaTanque\":%d,\"pir\":%s}",
                porcentajeHumedad,
                temperatura,
                humedadAmbiente,
                humedad,
                estadoSuelo,
                estadoBombaRiego,
                estadoZumbadorRiego,
                distCm,
                valAgua,
                estadoBombaTanque,
                alertaTipo,
                pirDetectado ? "true" : "false"
        );
    }

    private static int mapConstrained(int x, int inMin, int inMax, int outMin, int outMax) {
        if (inMax == inMin) {
            return outMin;
        }
        double mapped = (double) (x - inMin) * (outMax - outMin) / (double) (inMax - inMin) + outMin;
        int val = (int) Math.round(mapped);
        return Math.max(outMin, Math.min(outMax, val));
    }

    private static String formatearFila16(String texto) {
        String limpio = texto == null ? "" : texto;
        if (limpio.length() > 16) {
            return limpio.substring(0, 16);
        }
        return String.format("%-16s", limpio);
    }

    public static void main(String[] args) {
        AgroSenseSistema sistema = new AgroSenseSistema();
        sistema.actualizarLecturas(1000L, 3400, 27.5, 68.0, 16.2, 400, true);
        
        System.out.println("=======================================");
        System.out.println("JSON /datos:");
        String json = sistema.construirJsonDatos();
        json = json.replace("\",", "\",\n  ");
        System.out.println(json);
        System.out.println();
        System.out.println("=======================================");
        System.out.println("PANTALLA LCD:");
        System.out.println("Fila 0: [" + sistema.lcdFilas[0] + "]");
        System.out.println("Fila 1: [" + sistema.lcdFilas[1] + "]");
        System.out.println("=======================================");
    }
}





    

