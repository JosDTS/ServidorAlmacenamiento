/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ServidorAlmacenamiento;

/**
 *
 * @author ESTUDIANTE
 */
import java.net.*;
import java.io.*;
import java.util.*;

public class Flujo extends Thread {

    Socket nsfd;
    DataInputStream FlujoLectura;
    DataOutputStream FlujoEscritura;

    public Flujo(Socket sfd) {
        nsfd = sfd;
        try {
            FlujoLectura = new DataInputStream(new BufferedInputStream(sfd.getInputStream()));
            FlujoEscritura = new DataOutputStream(new BufferedOutputStream(sfd.getOutputStream()));
        } catch (IOException ioe) {
            System.out.println("IOException(Flujo): " + ioe);
        }
    }
    private InputStream inputStream;
    private OutputStream outputStream;

    public Flujo(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void run() {
        broadcast(nsfd.getInetAddress() + "se ha conectado");
        Servidor.usuarios.add((Object) this);
        while (true) {
            try {
                String linea = FlujoLectura.readUTF();
                if (!linea.equals("")) {
                    linea = nsfd.getInetAddress() + ">" + linea;
                    broadcast(linea);
                }
            } catch (IOException ioe) {
                Servidor.usuarios.removeElement(this);
                broadcast(nsfd.getInetAddress() + "se ha desconectado ");
                break;
            }
        }
    }

    public void recibirArchivo(String nombreArchivo, long tamanoArchivo) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(nombreArchivo);
        byte[] buffer = new byte[4096];
        int bytesRead;
        long bytesLeft = tamanoArchivo;

        while (bytesLeft > 0 && (bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesLeft))) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
            bytesLeft -= bytesRead;
        }

        fileOutputStream.close();
    }

    public void broadcast(String mensaje) {
        synchronized (Servidor.usuarios) {
            Enumeration e = Servidor.usuarios.elements();
            while (e.hasMoreElements()) {
                Flujo f = (Flujo) e.nextElement();
                try {
                    synchronized (f.FlujoEscritura) {
                        f.FlujoEscritura.writeUTF(mensaje);
                        f.FlujoEscritura.flush();
                    }
                } catch (IOException ioe) {
                    System.out.println("Error: " + ioe);
                }
            }
        }
    }
}
