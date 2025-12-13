import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    protected static Map<String, Club> clubes = new HashMap<>();
    //protected static ArrayList<Integer> listaPuertos = new ArrayList<>(); //De momento no se usa pero la idea es que se vayan liberando y reutilizando los puertos cuando un cliente se va
    protected static int sesiones = 0;
    protected static ArrayList<String> listaComandosSinSesion = new ArrayList<>(Arrays.asList("USER", "PASS"));
    protected static ArrayList<String> listaComandosConSesion = new ArrayList<>(Arrays.asList("SESIONES", "ADDCLUB", "UPDATECLUB", "GETCLUB", "REMOVECLUB", "LISTCLUBES", "COUNTCLUBES"));

    private int puertoDatos = 5001;

    public void ejecutar(){
        clubes.put("C1", new Club("C1", "Warriors"));
        clubes.put("C2", new Club("C2", "Los 4 Fantabulosos"));
        clubes.put("C3", new Club("C3", "Los Mas Capitos"));

        System.out.println("Servidor Iniciado");
        int puertoServer = 5000;

        try {
            ServerSocket serverSocket = new ServerSocket(puertoServer);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Conexion recibido " + socket.getRemoteSocketAddress());

                new ServerThread(socket, puertoDatos).start();
                puertoDatos++;
            }

        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        (new Server()).ejecutar();
    }
}
