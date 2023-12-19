package cbd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import static org.neo4j.driver.Values.parameters;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;



class Neo4jConnection {

    private Driver driver;
    
    public void connect() {
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "olaolaola"));
        try (Session session = driver.session()) {
            Result result = session.run("SHOW DATABASES");
            boolean databaseExists = false;
            while (result.hasNext()) {
                Record record = result.next();
                if ("Transfers".equalsIgnoreCase(record.get("name").asString())) {
                    databaseExists = true;
                    break;
                }
            }

            
            if (!databaseExists) {
                try (Session systemSession = driver.session(SessionConfig.forDatabase("system"))) {
                    systemSession.run("CREATE DATABASE Transfers");
                    System.out.println("Base de dados 'Transfers' criada!");
                }
            } else {
                System.out.println("Base de dados 'Transfers' já existe.");
            }
        }
    }
    
        public Driver getDriver() {
            return driver;
        }
    
        public void close() {
            if (driver != null) {
                driver.close();
            }
        }
}
    

public class Transfers
{

    private static final String CSV_FILE_PATH = "../transfers-all.csv";

    public static void main(String[] args) {
        Neo4jConnection neo4jConnection = new Neo4jConnection();
        neo4jConnection.connect(); 
        Driver driver = neo4jConnection.getDriver();
        int lineCount = 0;
        String filePath = "../CBD_L44c_output.txt";

        try (Session session = driver.session()) {
            Result result = session.run("MATCH (n) RETURN count(n) as count");
            Record record = result.single();
            int count = record.get("count").asInt();

            if (count == 0) {
                BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH));
                String line = reader.readLine();
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    System.out.println(lineCount);
                    if (line.trim().isEmpty()) {
                       continue;
                    }
                    processCSVLine(session, line);

                    if (lineCount >= 50000) {
                        break;
                    }
                }
                reader.close();
                deleteIncorrectRelationships(session);
            } else {
                System.out.println("Data already exists in the database, skipping CSV import.");
            }

            //QUERY 1
            //10 Clubes que mais venderam nos ultimos anos
            runQuery(session, "10 Clubes que mais venderam nos ultimos anos","MATCH (t:Transfer)-[:FROM_CLUB]->(c:Club) RETURN c.name As Club, count(t) As Number_of_Transfers ORDER BY Number_of_Transfers DESC LIMIT 10", filePath);


            //QUERY 2
            //Jogadores com transferencias mais caras
            runQuery(session,"Jogadores com transferencias mais caras", "MATCH (player:Player)<-[:OF_PLAYER]-(t:Transfer)-[:TO_CLUB]->(buyerClub:Club),(t)-[:FROM_CLUB]->(sellerClub:Club)RETURN player.name AS Player, sellerClub.name AS SellerClub, buyerClub.name AS BuyerClub, t.fee AS TransferFee ORDER BY t.fee DESC LIMIT 3", filePath);


            //QUERY 3
            //Jogadores Mais Valiosos por Posição:
            runQuery(session,"Jogadores Mais Valiosos por Posição:", "MATCH (t:Transfer)-[:OF_PLAYER]->(p:Player) RETURN p.position AS Position, max(t.fee) AS MaxFee ORDER BY MaxFee DESC LIMIT 10", filePath);

            //QUERY 4
            //Clubes com Maior Gasto em Transferências:
            runQuery(session,"Clubes com Maior Gasto em Transferências", "MATCH (t:Transfer)-[:TO_CLUB]->(c:Club) RETURN c.name AS Club, sum(t.fee) AS TotalSpent ORDER BY TotalSpent DESC LIMIT 5", filePath);

            //QUERY 5
            //Relação Entre Idade e Taxa de Transferência
            runQuery(session, "Relação Entre Idade e Taxa de Transferência", 
            "MATCH (t:Transfer)-[r:OF_PLAYER]->(p:Player) RETURN p.age AS Age, avg(t.fee) AS AverageTransferFee  ORDER BY Age", filePath);

            //QUERY 6
            //Clube que Mais Comprou Jogadores
            runQuery(session, "Clube que Mais Comprou Jogadores", 
            "MATCH (t:Transfer)-[:TO_CLUB]->(c:Club)RETURN c.name AS Club, count(t) AS NumberOfPlayersBought ORDER BY NumberOfPlayersBought DESC LIMIT 1", filePath);

            //QUERY 7
            //Jogadores Mais Vendidos por clubes de uma Nacionalidade
            runQuery(session, "Jogadores Mais Vendidos por Nacionalidade", 
            "MATCH (t:Transfer)-[:FROM_CLUB]->(sellerClub:Club)-[:PART_OF]->(country:Country) RETURN country.name AS Nationality, count(t) AS TotalSales ORDER BY TotalSales DESC LIMIT 10", filePath);

            //QUERY 8 
            //Paises com mais gastos em transferencias
            runQuery(session, "Jogadores Mais Vendidos por Nacionalidade", 
            "MATCH (c:Club)-[:PART_OF]->(country:Country), (c)<-[:TO_CLUB]-(t:Transfer) RETURN country.name AS Country, sum(t.fee) AS TotalSpending ORDER BY TotalSpending DESC LIMIT 10", filePath);

            //QUERY 9
            //Posicao mais popular nas transferencias
            runQuery(session, "Posicao mais popular nas transferencias", 
            "MATCH (t:Transfer)-[r:OF_PLAYER]->(p:Player) RETURN p.position AS Position, count(r) AS NumberOfTransfers ORDER BY NumberOfTransfers DESC LIMIT 1", filePath);

            //QUERY 10
            //Quantas transferencias houve acima de 25 milhoes
            runQuery(session, "Quantas transferencias houve acima de 25 milhoes", 
            "MATCH (t:Transfer) WHERE t.fee > 25000000 RETURN count(t) AS NumberOfTransfers", filePath);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                neo4jConnection.close();
        }
     }

    private static void runQuery(Session session,String enunciado, String cypherQuery, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            Result result = session.run(cypherQuery);
    
            writer.println("\n" + enunciado + "\n");
            System.out.println("\n" + enunciado + "\n");

            writer.println("\nQuery: " + cypherQuery + "\n");
            System.out.println("\n" + cypherQuery + "\n");
    
            while (result.hasNext()) {
                Record record = result.next();
                writer.println(record.asMap().toString());
                System.out.println(record.asMap());
            }
    
            writer.println();
        } catch (IOException e) {
            System.err.println("Erro ao escrever no arquivo: " + e.getMessage());
        }
    }
    private static void deleteIncorrectRelationships(Session session) {
        String deleteQuery = "MATCH ()-[r:OF_PLAYER]->(c:Country) DELETE r";
        session.run(deleteQuery);
        System.out.println("Incorrect OF_PLAYER relationships deleted.");
    }


    private static void processCSVLine(Session session, String line) {
    String[] data = line.split(",");


    for (String field : data) {
        if (field.trim().isEmpty()) {
            System.out.println("Campo vazio encontrado na linha: " + line);
            return; 
        }
    }
    
    String season = data[0].trim();
    String playerUri = data[1].trim();
    String playerName = data[2].trim();
    String playerPosition = data[3].trim();
    int playerAge = Integer.parseInt(data[4].trim());
    String sellerClubUri = data[5].trim();
    String sellerClubName = data[6].trim();
    String countryOfSellerClub = data[7].trim(); 
    String buyerClubUri = data[8].trim();
    String buyerClubName = data[9].trim();
    String countryOfBuyerClub = data[10].trim();
    String transferUri = data[11].trim();
    double transferFee = parseTransferFee(data[12]);
    String playerNationality = data[14].trim(); 

    String query = "MERGE (player:Player {uri: $playerUri, name: $playerName, position: $playerPosition, age: $playerAge}) " +
                   "MERGE (sellerClub:Club {uri: $sellerClubUri, name: $sellerClubName}) " +
                   "MERGE (buyerClub:Club {uri: $buyerClubUri, name: $buyerClubName}) " +
                   "MERGE (sellerCountry:Country {name: $countryOfSellerClub}) " +
                   "MERGE (buyerCountry:Country {name: $countryOfBuyerClub}) " +
                   "MERGE (playerCountry:Country {name: $playerNationality}) " +
                   "MERGE (transfer:Transfer {uri: $transferUri, fee: $transferFee, season: $season}) " +
                   "MERGE (player)-[:PLAYS_FOR]->(playerCountry) " +
                   "MERGE (transfer)-[:OF_PLAYER]->(player) " + 
                   "MERGE (transfer)-[:FROM_CLUB]->(sellerClub) " +
                   "MERGE (transfer)-[:TO_CLUB]->(buyerClub) " +
                   "MERGE (buyerClub)-[:PART_OF]->(buyerCountry) " +
                   "MERGE (sellerClub)-[:PART_OF]->(sellerCountry)";

    session.run(query, parameters(
        "playerUri", playerUri,
        "playerName", playerName,
        "playerPosition", playerPosition,
        "playerAge", playerAge,
        "sellerClubUri", sellerClubUri,
        "sellerClubName", sellerClubName,
        "countryOfSellerClub", countryOfSellerClub,
        "buyerClubUri", buyerClubUri,
        "buyerClubName", buyerClubName,
        "countryOfBuyerClub", countryOfBuyerClub, 
        "transferUri", transferUri,
        "transferFee", transferFee,
        "playerNationality", playerNationality,
        "season", season
    ));
    }

    private static double parseTransferFee(String feeString) {
        feeString = feeString.trim();
        if (feeString.isEmpty() || feeString.equals("Free transfer") || feeString.equals("Loan")|| feeString.equals("?") || feeString.equals("-")) {
            return 0.0;
        }
    
    
        
        feeString = feeString.replaceAll("[^0-9mk.]", "");
    
        double multiplier = 1;
        if (feeString.toLowerCase().endsWith("m")) {
            multiplier = 1_000_000; 
            feeString = feeString.substring(0, feeString.length() - 1); //milhoes
        } else if (feeString.toLowerCase().endsWith("k")) {
            multiplier = 1_000; 
            feeString = feeString.substring(0, feeString.length() - 1); //milhares
        }
    
        try {
            return Double.parseDouble(feeString) * multiplier;
        } catch (NumberFormatException e) {
            System.out.println("Erro ao converter taxa de transferência: " + feeString);
            return 0.0;
        }
    }
}