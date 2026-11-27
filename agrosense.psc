Algoritmo EcoRiego
	Definir humedad, llenos, i Como Entero;
	Definir temperatura, ambiente Como Real;
	// Equivale al Timer: se repite indefinidamente
	Mientras Verdadero Hacer
		// Generar datos simulados de los sensores
		humedad <- Aleatorio(0,100);
		temperatura <- 20+Aleatorio(0,150)/10;
		ambiente <- 40+Aleatorio(0,500)/10;
		Limpiar Pantalla;
		Escribir '==============================';
		Escribir '      SISTEMA ECORIEGO';
		Escribir '==============================';
		// Barra de progreso (20 espacios, cada uno vale 5 MOD )
		llenos <- trunc(humedad/5);
		Escribir '['Sin Saltar;
		Para i<-1 Hasta 20 Con Paso 1 Hacer
			Si i<=llenos Entonces
				Escribir '#'Sin Saltar;
			SiNo
				Escribir '-'Sin Saltar;
			FinSi
		FinPara
		Escribir '] ', humedad, '%';
		Escribir 'Humedad del Suelo: ', humedad, '%';
		Escribir 'Temperatura: ', temperatura, ' °C';
		Escribir 'Humedad Ambiente: ', ambiente, ' %';
		// Control del riego
		Si humedad<30 Entonces
			Escribir 'Estado del Suelo: SECO';
			Escribir 'Bomba: ENCENDIDA';
			Escribir 'Zumbador: ENCENDIDO';
		SiNo
			Escribir 'Estado del Suelo: HUMEDO';
			Escribir 'Bomba: APAGADA';
			Escribir 'Zumbador: APAGADO';
		FinSi
		// El Timer de Java usaba 2000 ms
		Esperar 2 Segundos;
	FinMientras
FinAlgoritmo
