import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author luizfilho
 * Recebe uma entrada no formato porta rede-destino/máscara/gateway/interface
 * que definirá seu funcionamento.
 * Após criado, ele ficará escutando na porta definida na entrada até receber um pacote.
 * Caso venha a receber um pacote, ele lerá o campo de mensagem do pacote,
 * o qual carrega o IP destino, o IP do roteador ( para o qual a mensagem está sendo enviada),
 * o IP origem (de quem criou o pacote) e a mensagem em si.
 * Após lê-lo, ele verificará na sua tabela de roteamento o que fazer com o pacote
 * (se descarta-o, se encaminha-o diretamente ou repassa para um próximo roteador,
 * alterando e recriando corretamente a parte de mensagem do pacote).
 */
public class Roteador {
    private int portaExecucao = 12345;
    private byte[] msg;
    private TabelaRoteamento tabela;
    DatagramSocket socketReceptor = null;

    /*
    * O primeiro parâmetro é a porta que ficará "aguardando entrarem"
    * Os outros são para criar sua tabela de roteamento
     */
    public Roteador(String[] args) throws IOException {
        portaExecucao = Integer.parseInt(args[0]);
        tabela = new TabelaRoteamento(args.length - 1);

        for (int a = 1; a < args.length; a++) {
            tabela.adicionarLinhaDeComando(args[a], a - 1);
        }
        criarSocketReceptor();
        while(true){
            byte[] msgSerializada = new byte[256];
            DatagramPacket pacote = new DatagramPacket(msgSerializada, msgSerializada.length);
            socketReceptor.receive(pacote);
            executar(pacote);
        }
    }

    public static void main(String[]args) throws IOException {
        //Cria novo roteador de acordo com os parametros recebidos por argumento
        if (args.length < 2) throw new IllegalArgumentException("Ao menos 2 argumentos são esperados: \"porta\"  rede-destino/máscara/gateway/interface");
        Roteador r = new Roteador(args);
        r.socketReceptor.close();
    }

    /*
    * Cria o Socket na porta informada para ficar escutando
    * Caso receba um pacote, é perguntado para sua tabela de roteamento para onde reencaminhar
    * Caso não retorne nada, significa que chegou onde deveria
    * Caso retorne um endereço, reencaminhá-lo
    * Caso o TTL do pacote seja <= 1, o pacote é dropado!
    *
    * Mensagem -> ipOrigem, ipDestino, mensagem
     */
    public void executar(DatagramPacket pacote) {
        String[] informacoesMensagem = decodificarPacote(pacote);
        String ipDestinoFinal = informacoesMensagem[0];
        String ipProxRoteador;
        String ipOrigem = informacoesMensagem[2];
        String[] msgComTTL = informacoesMensagem[3].split("TTL#CONTROL");
        //Posicao 0 fica mensagem, posicao 1 fica o TTL
        String mensagem = msgComTTL[0];
        int ttl = Integer.parseInt(msgComTTL[1]);
        ttl--;
        informacoesMensagem[3] = mensagem + "TTL#CONTROL" + String.valueOf(ttl);

        ProximoRoteador proximoRoteador = tabela.verificarIP(informacoesMensagem[0]);
        if (proximoRoteador == null)
            Mensagem.destinoInexistente(ipDestinoFinal);
        else if (proximoRoteador.getIpProxRoteador().equals("0.0.0.0"))
            Mensagem.encaminharDiretamente(informacoesMensagem);
        else if (ttl <= 1)
            Mensagem.ttlExcedido(ipDestinoFinal);
        else {
            ipProxRoteador = proximoRoteador.getIpProxRoteador();
            int portaProxRoteador = proximoRoteador.getPortaProxRoteador();
            mensagem = ipDestinoFinal + " " + ipProxRoteador + " " + ipOrigem + " " + mensagem + "TTL#CONTROL" + String.valueOf(ttl);
            enviarPacote(mensagem, ipProxRoteador, portaProxRoteador);
            Mensagem.encaminharPacote(ipDestinoFinal, ipProxRoteador, portaProxRoteador);
        }
    }

    private void enviarPacote(String mensagem, String ipRoteador, int portaProxRoteador) {
        InetAddress enderecoRoteador = null;
        byte[] msgSerializada;
        DatagramPacket pacote;
        DatagramSocket socketEnvio = null;

        try {
            enderecoRoteador = InetAddress.getByName(ipRoteador);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Roteador.class.getName()).log(Level.SEVERE, null, ex);
        }

        msgSerializada = mensagem.getBytes();

        pacote = new DatagramPacket(msgSerializada, msgSerializada.length, enderecoRoteador, portaProxRoteador);
        try {
            socketEnvio = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(Roteador.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socketEnvio.send(pacote);
        } catch (IOException ex) {
            Logger.getLogger(Roteador.class.getName()).log(Level.SEVERE, null, ex);
        }

        socketEnvio.close();
    }

    private void criarSocketReceptor() {
        try {
            socketReceptor = new DatagramSocket(portaExecucao);
        } catch (SocketException ex) {

            Logger.getLogger(Roteador.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void esperarReceber(DatagramPacket pacote) {
        try {
            socketReceptor.receive(pacote);
        } catch (IOException ex) {
            socketReceptor.close();
            Logger.getLogger(Roteador.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String[] decodificarPacote(DatagramPacket pacote) {
        String informacoesPacote = new String(pacote.getData()).trim();
        String[] informacoesQuebradas = informacoesPacote.split(" ", 4);

        return informacoesQuebradas;
    }
    

}
