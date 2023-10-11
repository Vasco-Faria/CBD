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


import java.util.Arrays;

import org.bson.Document;
import org.bson.conversions.Bson;


class SistemaB{

    private MongoCollection<Document> collection;

     public SistemaB() {
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
    

    public void PesquisarComIndexs() {
        
            try {
                collection.createIndex(Indexes.ascending("gastronomia"));
            } catch (Exception e) {
                System.err.println("Erro ao criar um indice ascendente para gastronomia: " + e);
            }

            try {
                collection.createIndex(Indexes.ascending("localidade"));
            } catch (Exception e) {
                System.err.println("Erro ao criar um indice ascendente para localidade: " + e);
            }

            try {
                collection.createIndex(Indexes.text(("nome")));
            } catch (Exception e) {
                System.err.println("Erro ao criar um indice de texto para nome: " + e);
            }

            System.out.println("Pesquisa com Indices");
            System.out.println("Liste o nome, a localidade e a gastronomia dos restaurantes que pertencem ao Bronx e cuja gastronomia é do tipo \"American\" ou \"Chinese\".");

            long inicioComIndex = System.nanoTime();

            Bson filtro = Filters.and(
                Filters.eq("localidade", "Bronx"),
                Filters.in("gastronomia", Arrays.asList("American", "Chinese"))
            );

            Bson projecao = Projections.fields(
                Projections.include("nome", "localidade", "gastronomia"),
                Projections.excludeId() 
            );

            FindIterable<Document> docs = collection.find(filtro).projection(projecao);

            //for (Document d : docs) {
                //System.out.println(d.toJson());
            //}

            long fimComIndex = System.nanoTime();
            
            System.out.println("Duração nas pesquisas com índices: " + (fimComIndex - inicioComIndex));
    }

    public void PesquisarSemIndexs(){
        
        System.out.println("Pesquisa sem Indices");
        System.out.println("Liste o nome, a localidade e a gastronomia dos restaurantes que pertencem ao Bronx e cuja gastronomia é do tipo \"American\" ou \"Chinese\".");

        long inicioSemIndex = System.nanoTime();

            Bson filtro = Filters.and(
                Filters.eq("localidade", "Bronx"),
                Filters.in("gastronomia", Arrays.asList("American", "Chinese"))
            );

            Bson projecao = Projections.fields(
                Projections.include("nome", "localidade", "gastronomia"),
                Projections.excludeId() 
            );

            FindIterable<Document> docs = collection.find(filtro).projection(projecao);

            //for (Document d : docs) {
               // System.out.println(d.toJson());
            //}

            long fimSemIndex = System.nanoTime();
            System.out.println("Duração nas pesquisas sem índices: " + (fimSemIndex - inicioSemIndex));
    }

}

public class AlineaB {
    
    public static void main( String[] args )
    {
        SistemaB sistema = new SistemaB();
        System.out.println();
        sistema.PesquisarSemIndexs();
        System.out.println();
        sistema.PesquisarComIndexs();
        System.out.println();
    }
}
