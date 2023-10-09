package cbd;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import com.mongodb.client.model.Projections;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;


import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;

import org.bson.Document;
import org.bson.conversions.Bson;


class SistemaC{

        private MongoCollection<Document> collection;

    public SistemaC(){
        try {
            MongoClient mongoCliente = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoCliente.getDatabase("cbd");
            collection = database.getCollection("restaurants");
        } catch (MongoException e) {
            e.printStackTrace();
            System.exit(1);
            collection=null;
        }
    }

    public void exercicio1(){

        try {
            
            Bson filtro = Filters.eq("localidade", "Bronx");

           
            long count = collection.countDocuments(filtro);

            System.out.println("Número de documentos com localidade 'Bronx': " + count);
        } catch (Exception e) {
            System.err.println("Erro ao executar a consulta: " + e);
        }

    }
    public void exercicio2(){
        try {
    
            Bson filtro = Filters.not(Filters.elemMatch("grades", Filters.gt("score", 3)));

            
            Bson projecao = Projections.fields(
                Projections.include("nome","localidade", "gastronomia", "grades.score"),
                Projections.excludeId() 
            );

            FindIterable<Document> docs = collection.find(filtro).projection(projecao);

            
            for (Document d : docs) {
                System.out.println(d.toJson());
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar a consulta: " + e);
        }

    }
    public void exercicio3(){
        try {
           
            Bson filtro = Filters.and(
                Filters.eq("grades.1.grade", "A"),
                Filters.eq("grades.1.date", Date.from(Instant.parse("2014-08-11T00:00:00Z")))
            );

           
            Bson projecao = Projections.fields(
                Projections.include("nome", "restaurant_id", "grades.grade"),
                Projections.excludeId() 
            );

            
            FindIterable<Document> docs = collection.find(filtro).projection(projecao);

            
            for (Document d : docs) {
                System.out.println(d.toJson());
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar a consulta: " + e);
        }

    }

    public void exercicio4(){

       

        try {
            
            Document addFieldsStage = new Document("$addFields",
                new Document("sum", new Document("$sum", "$grades.score")));

            
            Document matchStage = new Document("$match",
                Filters.and(
                    Filters.gt("sum", 50),
                    Filters.eq("gastronomia", "Portuguese"),
                    Filters.lt("address.coord.0", -60)
                ));

            
            AggregateIterable<Document> result = collection.aggregate(Arrays.asList(addFieldsStage, matchStage));

            
            for (Document d : result) {
                System.out.println(d.toJson());
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar a agregação: " + e);
        }


    }
    public void exercicio5(){
        try {
            
            Document groupStage = new Document("$group",
                new Document("_id", "$localidade")
                    .append("sum", new Document("$sum", 1)));

           
            Document sortStage = new Document("$sort",
                new Document("sum", -1));

            
            Document limitStage = new Document("$limit", 1);

            
            AggregateIterable<Document> result = collection.aggregate(Arrays.asList(groupStage, sortStage, limitStage));

            
            for (Document d : result) {
                System.out.println(d.toJson());
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar a agregação: " + e);
        }
    }

}



public class AlineaC {

    public static void main(String[] args) {
        
        SistemaC sistema = new SistemaC();
        System.out.println();
        System.out.println("Exercicio 1:");
        System.out.println("4. Indique o total de restaurantes localizados no Bronx.\n");
        sistema.exercicio1();
        System.out.println();
        System.out.println("Exercicio 2:");
        System.out.println("11. Liste o nome, a localidade, o score e gastronomia dos restaurantes que alcançaram sempre pontuações inferiores ou igual a 3.\n");
        sistema.exercicio2();
        System.out.println();
        System.out.println("Exercicio 3:");
        System.out.println("15. Liste o restaurant_id, o nome e os score dos restaurantes nos quais a segunda avaliação foi grade \"A\" e ocorreu em ISODATE \"2014-08-11T00: 00: 00Z\".\n");
        sistema.exercicio3();
        System.out.println();
        System.out.println("Exercicio 4:");
        System.out.println("23. Indique os restaurantes que têm gastronomia \"Portuguese\", o somatório de score é superior a 50 e estão numa latitude inferior a -60.\n");
        sistema.exercicio4();
        System.out.println();
        System.out.println("Exercicio 5:");
        System.out.println("30. Apresente a localizaçao mais utilizada pelos restaurantes\n");
        sistema.exercicio5();
        System.out.println();
    }
    
}
