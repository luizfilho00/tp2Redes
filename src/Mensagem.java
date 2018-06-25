/**
 * Possui métodos que recebem parâmetros de informações do pacote,
 * como IP destino e porta. E formata e exibe as mensagens solicitadas.
 */
public class Mensagem {

    public static void encaminharPacote(String ipFinal, String roteadorDestino, int portaDestino) {
        System.out.println("forwarding packet for " + ipFinal + " to next hop "
                + roteadorDestino + " over interface " + portaDestino);
    }
    
    public static void destinoInexistente(String ipFinal) {
        System.out.println("destination " + ipFinal + " not found in routing table, dropping packet ");
    }
    
    public static void encaminharDiretamente(String[] informacoesPacote) {
        String ipOrigem = informacoesPacote[2];
        String ipDestino = informacoesPacote[0];
        String[] msgComTTL = informacoesPacote[3].split("TTL#CONTROL");
        String mensagem = msgComTTL[0];
        
        System.out.println("destination reached. From " + ipOrigem + " to " +
                ipDestino + " : " + mensagem);
    }

    public static void ttlExcedido(String ipFinal){
        System.out.println("Time to Live exceeded in Transit, dropping packet for " + ipFinal);
    }
}
