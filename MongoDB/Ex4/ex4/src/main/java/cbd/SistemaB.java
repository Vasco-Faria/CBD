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


class SistemaAtendimentoB{

     private MongoCollection<Document> collection;

    public SistemaAtendimentoB(){
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

    public void efetuarPedido(String username, String produto, int quantity) {
    Document usuarioExistente = collection.find(new Document("username", username)).first();

    if (usuarioExistente == null) {
        
        Document novoUsuario = new Document("username", username)
            .append("pedidos", new ArrayList<Document>());

        Document novoPedido = new Document()
            .append("produto", produto)
            .append("quantity", quantity)
            .append("tempoDeVida", System.currentTimeMillis() + 60*60*1000);

        novoUsuario.getList("pedidos", Document.class).add(novoPedido);

        collection.insertOne(novoUsuario);
        System.out.println("-> " + username + ": Pedido adicionado com sucesso! Quantidade pedida ate agora: "+ quantity );
    } else {
        List<Document> pedidos = (List<Document>) usuarioExistente.get("pedidos");

        int totalQuantidade = 0;
        for (Document pedido : pedidos) {
            totalQuantidade += pedido.getInteger("quantity", 0);
        }
        int totalagora=totalQuantidade+quantity;
        if (totalagora > 30) {
            System.out.println("-> " + username + ": Limite máximo de unidades excedido!");
        } else {
            Document novoPedido = new Document()
                .append("produto", produto)
                .append("quantity", quantity)
                .append("tempoDeVida", System.currentTimeMillis() + 60*60*1000);

            pedidos.add(novoPedido);

            collection.updateOne(
                new Document("username", username),
                new Document("$set", new Document("pedidos", pedidos))
            );

            System.out.println("-> " + username + ": Pedido adicionado com sucesso! Quantidade pedida ate agora: "+ totalagora );
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


public class SistemaB {
    public static void main(String[] args) {
        
        SistemaAtendimentoB sistema= new SistemaAtendimentoB();

        sistema.efetuarPedido("Julio", "Peras", 5);
        sistema.efetuarPedido("Julio", "Peras", 5);
        sistema.efetuarPedido("Julio", "Peras", 5);
        sistema.efetuarPedido("Julio", "Peras", 5);
        sistema.efetuarPedido("Julio", "Peras", 3);
        sistema.efetuarPedido("Julio", "Peras", 3);
        sistema.efetuarPedido("Caramulo", "Agua", 20);
        sistema.efetuarPedido("Caramulo", "Agua", 10);
        sistema.efetuarPedido("Julio", "Maca", 3);
        sistema.efetuarPedido("Caramulo", "Almondegas", 3);



    }
}
