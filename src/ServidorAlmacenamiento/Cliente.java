/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ServidorAlmacenamiento;

/**
 *
 * @author ESTUDIANTE
 */
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class Cliente extends Frame implements ActionListener {

    static Socket sfd = null;
    static DataInputStream EntradaSocket;
    static DataOutputStream SalidaSocket;
    static TextField salida;
    static TextArea entrada;
    String texto;
    Button enviarButton;
    Button cargarButton;

    public Cliente() {
        setTitle("Chat");
        setSize(350, 200);
        salida = new TextField(30);
        salida.addActionListener(this);

        entrada = new TextArea();
        entrada.setEditable(false);

        enviarButton = new Button("Enviar");
        enviarButton.addActionListener(this);
        
        cargarButton = new Button("Cargar archivo"); 
        cargarButton.addActionListener(this);
        
        

        Panel buttonPanel = new Panel();
        buttonPanel.add(enviarButton);
        buttonPanel.add(cargarButton);

        add("South", salida);
        add("Center", entrada);
        setVisible(true);
    }

    public static void main(String[] args) {
        /**
         * File file = new File("ruta_del_archivo"); // Reemplaza
         * "ruta_del_archivo" con la ruta de tu archivo try { byte[] fileBytes =
         * convertFileToBytes(file); String bits =
         * convertBytesToBits(fileBytes); System.out.println(bits); } catch
         * (IOException e) { e.printStackTrace(); }
        *
         */
        Cliente cliente = new Cliente();
        try {
            sfd = new Socket(" 192.168.0.3", 7000);
            EntradaSocket = new DataInputStream(new BufferedInputStream(sfd.getInputStream()));
            SalidaSocket = new DataOutputStream(new BufferedOutputStream(sfd.getOutputStream()));
        } catch (UnknownHostException uhe) {
            System.out.println("No se puede acceder al servidor.");
            System.exit(1);
        } catch (IOException ioe) {
            System.out.println("Comunicación rechazada.");
            System.exit(1);
        }
        while (true) {
            try {
                String linea = EntradaSocket.readUTF();
                entrada.append(linea + "\n");
            } catch (IOException ioe) {
                System.exit(1);
            }
        }
    }

    // Método para leer el archivo y convertirlo en un array de bytes
    public static byte[] convertFileToBytes(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] bytesArray = new byte[(int) file.length()];
        fis.read(bytesArray);
        fis.close();
        return bytesArray;
    }

    // Método para convertir un array de bytes en una cadena de bits
    public static String convertBytesToBits(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            for (int i = 7; i >= 0; i--) {
                sb.append((b >> i) & 1);
            }
        }
        return sb.toString();
    }

    public boolean handleEvent(Event e) {
        if ((e.target == this) && (e.id == Event.WINDOW_DESTROY)) {
            if (sfd != null) {
                try {
                    sfd.close();
                } catch (IOException ioe) {
                    System.out.println("Error: " + ioe);
                }
                this.dispose();
            }
        }
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == enviarButton) {
            texto = salida.getText();
            salida.setText("");
            try {
                SalidaSocket.writeUTF(texto);
                SalidaSocket.flush();
            } catch (IOException ioe) {
                System.out.println("Error: " + ioe);
            }
        } else if (e.getSource() == cargarButton) {
            FileDialog fileDialog = new FileDialog(this, "Seleccionar Archivo",
                    FileDialog.LOAD);
            fileDialog.setVisible(true);
            String fileName = fileDialog.getFile();
            if (fileName != null) {
                try {
                    File file = new File(fileDialog.getDirectory(), fileName);
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[(int) file.length()];
                    fileInputStream.read(buffer);

                    SalidaSocket.writeInt(buffer.length); //Envía la longitud
                    //del archivo
                    SalidaSocket.write(buffer, 0, buffer.length); // Envía el 
                    //archivo
                    SalidaSocket.flush();

                    entrada.append("Archivo \"" + fileName + "\" enviado.\n");

                    fileInputStream.close();
                } catch (IOException ex) {
                    System.out.println("Error al enviar el archivo: " + ex);
                }
            }
        }
    }
}
