
package ecoriego;

import java.awt.*;
import java.util.Random;
import javax.swing.*;


public class EcoRiegoGUI extends JFrame {

    private JLabel lblHumedad;
    private JLabel lblTemperatura;
    private JLabel lblAmbiente;
    private JLabel lblEstado;
    private JLabel lblBomba;
    private JLabel lblZumbador;

    private JProgressBar barra;

    private Random random = new Random();

    public EcoRiegoGUI() {

        setTitle("Sistema EcoRiego");
        setSize(600,450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(8,1,10,10));
        panel.setBackground(new Color(230,245,230));

        JLabel titulo =
                new JLabel(
                        "SISTEMA ECORIEGO",
                        SwingConstants.CENTER);

        titulo.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24));

        barra = new JProgressBar(0,100);
        barra.setStringPainted(true);

        lblHumedad = new JLabel();
        lblTemperatura = new JLabel();
        lblAmbiente = new JLabel();
        lblEstado = new JLabel();
        lblBomba = new JLabel();
        lblZumbador = new JLabel();

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,20,20,20));

        panel.add(titulo);
        panel.add(barra);
        panel.add(lblHumedad);
        panel.add(lblTemperatura);
        panel.add(lblAmbiente);
        panel.add(lblEstado);
        panel.add(lblBomba);
        panel.add(lblZumbador);

        add(panel);

        Timer timer =
                new Timer(
                        2000,
                        e -> actualizarDatos());

        timer.start();

        actualizarDatos();
    }

    private void actualizarDatos() {

        int humedad =
                random.nextInt(101);

        double temperatura =
                20 + random.nextDouble()*15;

        double ambiente =
                40 + random.nextDouble()*50;

        barra.setValue(humedad);

        lblHumedad.setText(
                "Humedad del Suelo: "
                        + humedad + "%");

        lblTemperatura.setText(
                String.format(
                        "Temperatura: %.1f °C",
                        temperatura));

        lblAmbiente.setText(
                String.format(
                        "Humedad Ambiente: %.1f %% ",
                        ambiente));

        if(humedad < 30){

            lblEstado.setText(
                    "Estado del Suelo: SECO");

            lblEstado.setForeground(
                    Color.RED);

            lblBomba.setText(
                    "Bomba: ENCENDIDA");

            lblBomba.setForeground(
                    Color.RED);

            lblZumbador.setText(
                    "Zumbador: ENCENDIDO");

            lblZumbador.setForeground(
                    Color.RED);

        }else{

            lblEstado.setText(
                    "Estado del Suelo: HÚMEDO");

            lblEstado.setForeground(
                    new Color(0,150,0));

            lblBomba.setText(
                    "Bomba: APAGADA");

            lblBomba.setForeground(
                    new Color(0,150,0));

            lblZumbador.setText(
                    "Zumbador: APAGADO");

            lblZumbador.setForeground(
                    new Color(0,150,0));
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new EcoRiegoGUI().setVisible(true);

        });
    }
}