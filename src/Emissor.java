import java.io.IOException;
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
    public static void main(String[] args) throws IOException {
        if (args.length < 4) throw new IllegalArgumentException("Argumentos inválidos!");

        String ipProxRoteador = args[0];
        int portaRoteador = Integer.parseInt(args[1]);
        String ipOrigem = args[2];
        String ipDestinoFinal = args[3];
        String mensagem = args[4];
        
        InetAddress enderecoRoteador;
        byte[] msgSerializada;
        DatagramPacket pacote;
        DatagramSocket socketEnviador;   
        
        enderecoRoteador = InetAddress.getByName(ipProxRoteador);

        mensagem = ipDestinoFinal + " " + ipProxRoteador + " " + ipOrigem + " " + mensagem + "TTL#CONTROL" + "5";
        msgSerializada = mensagem.getBytes();
        
        pacote = new DatagramPacket(msgSerializada, msgSerializada.length, enderecoRoteador, portaRoteador);
        socketEnviador = new DatagramSocket();
        socketEnviador.send(pacote);

        socketEnviador.close();
    }
}
