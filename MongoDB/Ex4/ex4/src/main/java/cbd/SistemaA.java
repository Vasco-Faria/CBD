package cbd;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;

import com.mongodb.client.FindIterable;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

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
                .append("tempoDeVida", System.currentTimeMillis() + 10000); 

            novoUsuario.getList("pedidos", Document.class).add(primeiroPedido);

            collection.insertOne(novoUsuario);
            System.out.println("Pedido adicionado com sucesso!");
        } else {
             List<Document> pedidos = (List<Document>) usuarioExistente.get("pedidos");

            if (pedidos.size() >= 30) {
                System.out.println("Limite máximo de pedidos excedido!");
            } else {
               
                Document novoPedido = new Document()
                        .append("produto", product)
                        .append("tempoDeVida", System.currentTimeMillis() + 10000); // 10 segundos

                pedidos.add(novoPedido);

                // Atualizar o documento do usuário com a nova lista de pedidos
                collection.updateOne(
                        new Document("username", username),
                        new Document("$set", new Document("pedidos", pedidos))
                );

                System.out.println("Pedido adicionado com sucesso!");
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
        while(i<30){
            sistema.efetuarPedido("Vasco", "CBD");
            i++;
        }
        int n=0;
        while(n<30){
            sistema.efetuarPedido("Amelia", "IES");
            n++;
        }

        //pedidos 31 para mostrar limite
        sistema.efetuarPedido("Vasco", "CBD");
        sistema.efetuarPedido("Amelia", "IES");


        sistema.efetuarPedido("Maria", "SIO");
    }
}
