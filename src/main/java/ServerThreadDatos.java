import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThreadDatos extends Thread {
    private Socket socket;
    private String comando;
    private PrintWriter pw;
    private String codigo;
    private String id;

    public ServerThreadDatos(Socket socket, String comando, PrintWriter pw, String codigo,  String id) {
        this.socket = socket;
        this.comando = comando;
        this.pw = pw;
        this.codigo = codigo;
        this.id = id;
    }

    public ServerThreadDatos(Socket socket, String comando, PrintWriter pw, String codigo) {
        this.socket = socket;
        this.comando = comando;
        this.pw = pw;
        this.codigo = codigo;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream enviarObjeto = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream recibirObjeto = new ObjectInputStream(socket.getInputStream());

            Club club;
            switch (comando){
                case "ADDCLUB":
                    club = (Club) recibirObjeto.readObject();
                    if (club.getId().isEmpty() || club.getNombre().isEmpty()) {
                        pw.println("FAILED " + codigo + " 411 El club debe contener <id> y <nombre>");
                        pw.flush();
                    } else if (!Server.clubes.containsKey(club.getId())){
                        Server.clubes.put(club.getId(), club);
                        pw.println("OK " + codigo + " 206 Club añadido correctamente");
                        pw.flush();
                    }
                    else{
                        pw.println("FAILED " + codigo + " 409 El <id> del club está en uso");
                        pw.flush();
                    }
                    break;
                case "UPDATECLUB":
                    club = (Club) recibirObjeto.readObject();
                    if (club.getId().isEmpty() || club.getNombre().isEmpty()) {
                        pw.println("FAILED " + codigo + " 411 El club debe contener <id> y <nombre>");
                        pw.flush();
                    } else{
                        Server.clubes.remove(this.id);
                        Server.clubes.put(club.getId(), club);
                        pw.println("OK " + codigo + " 207 Club actualizado correctamente");
                        pw.flush();
                    }
                    break;
                case "GETCLUB":
                    enviarObjeto.writeObject(Server.clubes.get(this.id));
                    pw.println("OK " + codigo + " 208 Transferencia terminada");
                    pw.flush();
                    break;
                case "LISTCLUBES":
                    enviarObjeto.writeObject(Server.clubes);
                    break;
            }

            recibirObjeto.close();
            enviarObjeto.close();
            this.socket.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
