
package cbd;

import redis.clients.jedis.Jedis;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Ex24c {

    public static void main(String[] args) {
        // Conectando ao Redis
        Jedis jedis = new Jedis("localhost");

        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase mongoDatabase = mongoClient.getDatabase("cbd");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("phones");

        long redisStartTime = System.currentTimeMillis();
        jedis.set("ola", "cbd");
        long redisEndTime = System.currentTimeMillis();
        long redisExecutionTime = redisEndTime - redisStartTime;

        long redisStartTime2 = System.currentTimeMillis();
        
        String redisValue2 = jedis.get("ola");
        long redisEndTime2 = System.currentTimeMillis();
        long redisExecutionTime2 = redisEndTime2 - redisStartTime2;

        long mongoStartTime = System.currentTimeMillis();
        Document document = new Document("key", "mongo_key").append("value", "mongo_value");
        mongoCollection.insertOne(document);
        long mongoEndTime = System.currentTimeMillis();
        long mongoExecutionTime = mongoEndTime - mongoStartTime;


        long mongoStartTime2 = System.currentTimeMillis();
        Document mongoDocument2 = mongoCollection.find(new Document("key", "mongo_key")).first();
        String mongoValue2 = mongoDocument2.getString("value");
        long mongoEndTime2 = System.currentTimeMillis();
        long mongoExecutionTime2 = mongoEndTime2 - mongoStartTime2;

        System.out.println("Operacoes de escrita:");
        System.out.println("Tempo gasto no Redis: " + redisExecutionTime + " milissegundos");
        System.out.println("Tempo gasto no MongoDB: " + mongoExecutionTime + " milissegundos");


        System.out.println("\nOperacoes de leitura:");
        System.out.println("Tempo gasto no Redis: " + redisExecutionTime2 + " milissegundos");
        System.out.println("Tempo gasto no MongoDB: " + mongoExecutionTime2+ " milissegundos");


        jedis.close();
        mongoClient.close();
    }
}
