import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author luizfilho
 * Recebe uma entrada no formato: IP_RoteadorDestino/Porta_RoteadorDestino/IP_Origem/IP_Destino/Mensagem
 * cria o pacote com o campo de mensagem contendo o
 * IP_Destino, IP_RoteadorDestino, IP_Origem e a mensagem, e
 * a envia para o Roteador que está no IP_RoteadorDestino
 * escutando na porta Porta_RoteadorDestino.
 */
public class Emissor {

    private static void enviaMensagem(String args) throws IOException {
        args = args.replaceAll("   ", " ");
        args = args.replaceAll("  ", " ");
        String[] entrada = args.split(" ", 5);

        if (entrada.length < 4){
            System.out.println("Por favor forneça a entrada correta");
            return;
        }
        String mensagem;
        String ipProxRoteador = entrada[0];
        int portaRoteador = Integer.parseInt(entrada[1]);
        String ipOrigem = entrada[2];
        String ipDestinoFinal = entrada[3];

        if (entrada.length == 4) mensagem = "";
        else mensagem = entrada[4];

        InetAddress enderecoRoteador;
        byte[] msgSerializada;
        DatagramPacket pacote;
        DatagramSocket socketEnvio;

        enderecoRoteador = InetAddress.getByName(ipProxRoteador);

        mensagem = ipDestinoFinal + " " + ipProxRoteador + " " + ipOrigem + " " + mensagem + "TTL#CONTROL" + "5";
        msgSerializada = mensagem.getBytes();

        pacote = new DatagramPacket(msgSerializada, msgSerializada.length, enderecoRoteador, portaRoteador);
        socketEnvio = new DatagramSocket();
        socketEnvio.send(pacote);
    }

    public static void main(String[] args) throws IOException {
        String mensagem;
        if (args.length < 4) throw new IllegalArgumentException("Argumentos inválidos!");

        String ipProxRoteador = args[0];
        int portaRoteador = Integer.parseInt(args[1]);
        String ipOrigem = args[2];
        String ipDestinoFinal = args[3];
        if (args.length == 4) mensagem = "";
        else mensagem = args[4];
        
        InetAddress enderecoRoteador;
        byte[] msgSerializada;
        DatagramPacket pacote;
        DatagramSocket socketEnvio;   
        
        enderecoRoteador = InetAddress.getByName(ipProxRoteador);

        mensagem = ipDestinoFinal + " " + ipProxRoteador + " " + ipOrigem + " " + mensagem + "TTL#CONTROL" + "5";
        msgSerializada = mensagem.getBytes();
        
        pacote = new DatagramPacket(msgSerializada, msgSerializada.length, enderecoRoteador, portaRoteador);
        socketEnvio = new DatagramSocket();
        socketEnvio.send(pacote);

        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        String lido;
        while(!(lido = bf.readLine()).isEmpty()){
            enviaMensagem(lido);
        }
        socketEnvio.close();
    }
}
