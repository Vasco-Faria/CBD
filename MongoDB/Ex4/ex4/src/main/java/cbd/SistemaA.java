package cbd;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;


import com.mongodb.client.FindIterable;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;


class SistemaAtendimentoA{

     private MongoCollection<Document> collection;

    public SistemaAtendimentoA(){
        try {
            MongoClient mongoCliente = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoCliente.getDatabase("atendimento_db");
            collection = database.getCollection("pedidos");
        } catch (MongoException e) {
            e.printStackTrace();
            System.exit(1);
            collection=null;
        }
        cleanupExpiredOrders(collection);
     }

     public void efetuarPedido(String username, String product){
         Document usuarioExistente = collection.find(new Document("username", username)).first();

        if (usuarioExistente == null) {
            Document novoUsuario = new Document("username", username)
                .append("pedidos", new ArrayList<Document>());

            Document primeiroPedido = new Document()
                .append("produto", product)
                .append("tempoDeVida", System.currentTimeMillis() + 60 * 60 * 1000);


            novoUsuario.getList("pedidos", Document.class).add(primeiroPedido);

            collection.insertOne(novoUsuario);
            System.out.println("-> "+ username + ": Pedido adicionado com sucesso!Ainda pode fazer mais " + 29 + " pedidos.");
        } else {
             List<Document> pedidos = (List<Document>) usuarioExistente.get("pedidos");

            if (pedidos.size() >= 30) {
                System.out.println("-> "+ username + ": Limite máximo de pedidos excedido!");
            } else {
               
                Document novoPedido = new Document()
                        .append("produto", product)
                       .append("tempoDeVida", System.currentTimeMillis() + 60 * 60 * 1000);


                pedidos.add(novoPedido);

               
                collection.updateOne(
                        new Document("username", username),
                        new Document("$set", new Document("pedidos", pedidos))
                );

                int ap =30-pedidos.size();
                if (ap==0){
                     System.out.println("-> "+ username + ": Pedido adicionado com sucesso! Ultimo pedido possivel.");
                }else{
                    System.out.println("-> "+ username + ": Pedido adicionado com sucesso! Ainda pode fazer mais " + ap + " pedidos.");
                }
            }
         }
     }

     private static void cleanupExpiredOrders(MongoCollection<Document> collection) {
        FindIterable<Document> usuarios = collection.find();

        for (Document usuario : usuarios) {
            List<Document> pedidos = (List<Document>) usuario.get("pedidos");
            List<Document> pedidosRemovidos = new ArrayList<>();

            for (Document pedido : pedidos) {
                long tempoDeVida = pedido.getLong("tempoDeVida");
                if (System.currentTimeMillis() > tempoDeVida) {
                   
                    pedidosRemovidos.add(pedido);
                }
            }

           
            pedidos.removeAll(pedidosRemovidos);

           
           if (pedidos.isEmpty()) {
            collection.deleteOne(new Document("_id", usuario.getObjectId("_id")));
        } else {
            collection.updateOne(new Document("_id", usuario.getObjectId("_id")),
                    new Document("$set", new Document("pedidos", pedidos)));
        }
        }
    }
}


public class SistemaA 
{
    public static void main( String[] args )
    {
        
        SistemaAtendimentoA sistema=new SistemaAtendimentoA();
        int i=0;

        System.out.println("Sistema de Atendimento A");
        System.out.println();

        while(i<30){
            sistema.efetuarPedido("Vasco", "CBD");
            i++;
        }
        int n=0;
        while(n<30){
            sistema.efetuarPedido("Amelia", "IES");
            n++;
        }

        sistema.efetuarPedido("Vasco", "IES");
        sistema.efetuarPedido("Amelia", "IES");
        sistema.efetuarPedido("Maria", "SIO");
    }
}
