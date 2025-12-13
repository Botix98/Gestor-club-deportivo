import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket socket;
    private ServerSocket serverSocketDatos;
    private int puertoDatos;
    public ServerThread(Socket socket, int puertoDatos) {
        this.socket = socket;
        this.puertoDatos = puertoDatos;
        try {
            this.serverSocketDatos = new ServerSocket(this.puertoDatos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Hilo lanzado...");
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            boolean sesionIniciada = false;
            String codigo = "";
            String msg = "";
            String[] partes;

            //Opciones antes de iniciar sesion
            while(!sesionIniciada){
                msg = br.readLine();
                partes = msg.split(" ");
                codigo = partes[0];
                if (partes.length >= 2) {
                    switch(partes[1].toUpperCase()){
                        case "USER":
                            if (partes.length == 3) {
                                pw.println("OK " + codigo + " 201 Envie contraseña");
                                pw.flush();
                                String auxUser = partes[2];
                                msg = br.readLine();
                                partes = msg.split(" ");
                                codigo =  partes[0];
                                if (partes.length == 3) {
                                    if (partes[1].equalsIgnoreCase("PASS")) {
                                        if (auxUser.equals("admin") && partes[2].equals("admin")) {
                                            pw.println("OK " + codigo + " 202 Welcome admin");
                                            pw.flush();
                                            Server.sesiones++;
                                            sesionIniciada = true;
                                        } else {
                                            pw.println("FAILED " + codigo + " 402 Usuario o contraseña incorrecto");
                                            pw.flush();
                                        }
                                    } else {
                                        pw.println("FAILED " + codigo + " 405 Comando erroneo. Vuelve a empezar");
                                        pw.flush();
                                    }
                                } else{
                                    pw.println("FAILED " + codigo + " 403 Formato de comando incorrecto. PASS <name>. Vuelve a empezar");
                                    pw.flush();
                                }
                            } else{
                                pw.println("FAILED " + codigo + " 403 Formato de comando incorrecto. USER <name>");
                                pw.flush();
                            }
                            break;
                        case "EXIT":
                            if (partes.length == 2) {
                                pw.println("OK " + codigo + " 210 Bye");
                                pw.flush();
                                //Cierra los buffers y el serverThread
                                pw.close();
                                br.close();
                                socket.close();
                                return;
                            } else{
                                pw.println("FAILED " + codigo + " 403 Formato de comando incorrecto. EXIT");
                                pw.flush();
                            }
                            break;
                        default:
                            //Comando no valido
                            if (partes[1].equalsIgnoreCase("PASS")) {
                                pw.println("FAILED " + codigo + " 401 Para usar el comando PASS, debes usar primero el comando USER");
                                pw.flush();
                            } else if (Server.listaComandosConSesion.contains(partes[1].toUpperCase())) {
                                pw.println("FAILED " + codigo + " 406 Inicia sesion para usar el comando");
                                pw.flush();
                            } else{
                                pw.println("FAILED " + codigo + " 404 Comando no valido");
                                pw.flush();
                            }
                            break;
                    }
                }
                else {
                    pw.println("FAILED " + codigo + " 403 Comando no valido"); //Tiene menos partes de las minimas necesarias
                    pw.flush();
                }
            }
            //Opciones tras iniciar sesion
            while(true){
                msg = br.readLine();
                partes = msg.split(" ");
                codigo =  partes[0];
                if (partes.length >= 2) {
                    switch(partes[1].toUpperCase()){
                        case "ADDCLUB":
                        case "LISTCLUBES":
                            pw.println("PREOK " + codigo + " 209 localhost " + this.puertoDatos);
                            pw.flush();
                            gestionarDatos(partes[1].toUpperCase(), codigo, pw, "");
                            break;
                        case "GETCLUB":
                        case "UPDATECLUB":
                            if (partes.length == 3) {
                                if (!Server.clubes.containsKey(partes[2].toUpperCase())) {
                                    pw.println("FAILED " + codigo + " 410 No hay clubs asociados al id " + partes[2].toUpperCase());
                                    pw.flush();
                                } else {
                                    pw.println("PREOK " + codigo + " 209 localhost " + this.puertoDatos);
                                    pw.flush();
                                    gestionarDatos(partes[1].toUpperCase(), codigo, pw, partes[2].toUpperCase());
                                }
                            } else{
                                pw.println("FAILED " + codigo + " 411 Formato de comando incorrecto. " + partes[1].toUpperCase() + " <id>");
                                pw.flush();
                            }
                            break;
                        case "COUNTCLUBES":
                            //HAY QUE CAPTURAR EL ERROR SI NO SE HA PODIDO SACAR EL TOTAL DE CLUBES
                            //SINCERAMENTE NO SE CUANDO NI COMO SE DA ESE ERROR PERO OK
                            int totalClubes = Server.clubes.size();
                            pw.println("OK " + codigo + " 203 " + totalClubes);
                            pw.flush();
                            break;
                        case "REMOVECLUB":
                            if (Server.clubes.containsKey(partes[2])){
                                Server.clubes.remove(partes[2]);
                                pw.println("OK " + codigo + " 204 Club eliminado");
                                pw.flush();
                                break;
                            }
                            pw.println("FAILED " + codigo + " 408 No se ha encontrado el club");
                            pw.flush();
                            break;
                        case "SESIONES":
                            pw.println("OK " + codigo + " 205 " + Server.sesiones);
                            pw.flush();
                            break;
                        case "EXIT":
                            pw.println("OK " + codigo + " 210 Bye");
                            Server.sesiones--;
                            //Cierra los buffers y el serverThread
                            pw.flush();
                            pw.close();
                            br.close();
                            socket.close();
                            return;
                        default:
                            //Comando no valido
                            if (Server.listaComandosSinSesion.contains(partes[1].toUpperCase())) {
                                pw.println("FAILED " + codigo + " 407 Comando no valido una vez iniciada sesion");
                                pw.flush();
                            } else{
                                pw.println("FAILED " + codigo + " 404 Comando no valido");
                                pw.flush();
                            }
                            break;
                    }
                } else{
                    pw.println("FAILED " + codigo + " 403 Comando no valido"); //Tiene menos partes de las minimas necesarias
                    pw.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private void gestionarDatos(String comando, String codigo, PrintWriter pw, String id) throws IOException, ClassNotFoundException {
        Socket socketDatos = this.serverSocketDatos.accept();
        if (id.isEmpty()) {
            new ServerThreadDatos(socketDatos, comando, pw, codigo).start();
        } else{
            new ServerThreadDatos(socketDatos, comando, pw, codigo, id).start();
        }

    }
}
