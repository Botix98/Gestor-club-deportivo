import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class Cliente {
    public void ejecutar() {
        String server = "localhost";
        int puerto = 5000;
        int numComandos = 0;

        try {
            Socket socketComandos = new Socket(server, puerto);
            BufferedReader br = new BufferedReader(new InputStreamReader(socketComandos.getInputStream()));
            PrintWriter pw = new PrintWriter(socketComandos.getOutputStream(), true);

            Socket socketDatos = null;
            ObjectInputStream recibirObjeto = null;
            ObjectOutputStream enviarObjeto = null;

            Scanner sc = new Scanner(System.in);

            String mensaje = "";
            String respuesta = "";

            do{
                mensaje = sc.nextLine();
                numComandos++;
                pw.println(numComandos + " " + mensaje);
                pw.flush();

                //Respuesta del server
                respuesta = br.readLine();
                System.out.println(respuesta);
                switch (mensaje.split(" ")[0].toUpperCase()) {
                    case "ADDCLUB":
                    case "UPDATECLUB":
                    case "GETCLUB":
                    case "LISTCLUBES":
                        if (!respuesta.startsWith("FAILED")) {
                            String ip = respuesta.split(" ")[3];
                            int puertoDatos = Integer.parseInt(respuesta.split(" ")[4]);
                            socketDatos = new Socket(ip, puertoDatos);
                            enviarObjeto = new ObjectOutputStream(socketDatos.getOutputStream());
                            recibirObjeto = new ObjectInputStream(socketDatos.getInputStream());

                            if (mensaje.split(" ")[0].equalsIgnoreCase("ADDCLUB")) {
                                addClub(sc, enviarObjeto, br);
                            } else if (mensaje.split(" ")[0].equalsIgnoreCase("UPDATECLUB")) {
                                updateClub(sc, enviarObjeto, br);
                            } else if (mensaje.split(" ")[0].equalsIgnoreCase("LISTCLUBES")) {
                                listaClubes(recibirObjeto);
                            } else {
                                System.out.println(recibirObjeto.readObject());
                                System.out.println(br.readLine());
                            }

                            enviarObjeto.close();
                            recibirObjeto.close();
                            socketDatos.close();
                        } else {
                            //CREO QUE AQUI NO HAY QUE PONER NADA PERO NI ZORRA. YO LO DEJO ESTO AQUI POR SI TENGO QUE USARLO
                        }
                        break;
                    /*default:
                        Thread.sleep(200);

                        while (br.ready()) {
                            respuesta = br.readLine();
                            System.out.println("\t" + respuesta);
                        }
                        break;*/
                }

            }while(!respuesta.endsWith("Bye"));

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void listaClubes(ObjectInputStream recibirObjeto) throws IOException, ClassNotFoundException {
        Map<String, Club> clubs = (Map<String, Club>) recibirObjeto.readObject();

        System.out.println("Lista de clubes:");
        for (Club club : clubs.values()) {
            System.out.println(" - " + club);
        }
    }

    private void addClub(Scanner sc, ObjectOutputStream enviarObjeto, BufferedReader br) throws IOException {
        System.out.print("Introduce el id del club: ");
        String id = sc.nextLine();
        System.out.print("Introduce el nombre del club: ");
        String nombre = sc.nextLine();
        Club club =  new  Club(id, nombre);

        enviarObjeto.writeObject(club);
        enviarObjeto.flush();

        System.out.println(br.readLine());
    }

    private void updateClub(Scanner sc, ObjectOutputStream enviarObjeto, BufferedReader br) throws IOException {
        System.out.print("Introduce el nuevo id del club: ");
        String id = sc.nextLine();
        System.out.print("Introduce el nuevo nombre del club: ");
        String nombre = sc.nextLine();
        Club club = new Club(id, nombre);

        enviarObjeto.writeObject(club);
        enviarObjeto.flush();

        System.out.println(br.readLine());
    }

    public static void main(String[] args) {
        (new Cliente()).ejecutar();
    }
}
