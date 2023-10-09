package cbd;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.FindIterable;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;


class Sistema {

    private MongoCollection<Document> collection;

     public Sistema() {
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
    public void pesquisarRegistos(String nome){
         Document filtro = new Document("nome", nome);
        FindIterable<Document> resultados = collection.find(filtro);

        if (resultados != null) {
            MongoCursor<Document> cursor = resultados.iterator();

            while (cursor.hasNext()) {

                System.out.println();

                Document documento = cursor.next();
                System.out.println("Registro encontrado:");
                System.out.println("ID: " + documento.getObjectId("_id"));
                System.out.println("Nome: " + documento.getString("nome"));
                System.out.println("Localidade: " + documento.getString("localidade"));
                System.out.println("Gastronomia: " + documento.getString("gastronomia"));

               

                System.out.println();
            }

            cursor.close();
        } else {
            System.out.println("Nenhum registro encontrado para o nome: " + nome);
        }
    }


   public void InserirRegistro(String nome, String localidade, String gastronomia) {
    
        if (nome == null || nome.isEmpty() || localidade == null || localidade.isEmpty() || gastronomia == null || gastronomia.isEmpty()) {
            System.out.println("Não é possível inserir o registro.");
            return; 
        }


        Bson filtroNomeExistente = Filters.eq("nome", nome);
        if (collection.countDocuments(filtroNomeExistente) > 0) {
            System.out.println("Já existe um registro com o mesmo nome na coleção.\n");
            return;
        }
    
        Document documento = new Document("nome", nome)
            .append("localidade", localidade)
            .append("gastronomia", gastronomia);

        
        collection.insertOne(documento);

        System.out.println("Registro inserido com sucesso!");
        System.out.println();
    }
    
    public void EditarRegistro(String id, String novoNome, String novaLocalidade, String novaGastronomia) {
        
        if (id == null || id.isEmpty()) {
            System.out.println("Erro: O ID do documento a ser editado não foi fornecido.");
            return;
        }

        
        if (novoNome == null && novaLocalidade == null && novaGastronomia == null) {
            System.out.println("Erro: Nenhum campo de atualização fornecido.");
            return;
        }

        
        Bson filtro = Filters.eq("_id", new ObjectId(id));

        Document documentoAntes = collection.find(filtro).first();

        
        Document atualizacao = new Document();
        if (novoNome != null && !novoNome.isEmpty()) {
            atualizacao.append("nome", novoNome);
        }
        if (novaLocalidade != null && !novaLocalidade.isEmpty()) {
            atualizacao.append("localidade", novaLocalidade);
        }
        if (novaGastronomia != null && !novaGastronomia.isEmpty()) {
            atualizacao.append("gastronomia", novaGastronomia);
        }

        
        UpdateResult resultado = collection.updateOne(filtro, new Document("$set", atualizacao));

        Document documentoDepois = collection.find(filtro).first();

        if (resultado.getModifiedCount() > 0) {
            System.out.println("Registro editado com sucesso!");

            System.out.println("Estado anterior:");
            System.out.println(documentoAntes.toJson());

            System.out.println("Estado posterior:");
            System.out.println(documentoDepois.toJson()+"\n");
        } else {
            System.out.println("Nenhum registro foi encontrado para editar.");
        }
    }


}


public class AlineaA 
{
    public static void main( String[] args )
    {
        Sistema sistema = new Sistema();

        sistema.pesquisarRegistos("C & C Catering Service");
        sistema.pesquisarRegistos("Seuda Foods");
        sistema.InserirRegistro("Tasca do Chameau", "Serreleis", "Portuguese");
        sistema.pesquisarRegistos("Tasca do Chameau");
        sistema.InserirRegistro("Alicarius", "Aveiro", "Portuguese");
        sistema.EditarRegistro("6523d8adc28185502bc7b4d7", "Eusebio", "Vila do Conde", "Portuguese");
    }
}
