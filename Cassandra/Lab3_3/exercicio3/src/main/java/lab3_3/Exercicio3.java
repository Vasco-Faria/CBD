package lab3_3;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.Session;
import java.net.InetSocketAddress;

import com.datastax.oss.driver.api.core.CqlSession;



import java.util.Scanner;


class CassandraConnector {

    private Session session;
    private Cluster cluster;

    public void connect(String node, int port, String keyspace) {
        if (!keyspaceExists(node, port, keyspace)) {
            createKeyspace(node, port, keyspace);
        }

        cluster = Cluster.builder()
                .addContactPoint(node)
                .withPort(port)
                .build();

        session = cluster.connect(keyspace);
        System.out.println();
        System.out.println("Connection Established to keyspace "+ keyspace);
        
    }

    private boolean keyspaceExists(String node, int port, String keyspace) {
        try (Cluster tempCluster = Cluster.builder()
                .addContactPoint(node)
                .withPort(port)
                .build();
             Session tempSession = tempCluster.connect()) {

            Metadata metadata = tempCluster.getMetadata();
            KeyspaceMetadata keyspaceMetadata = metadata.getKeyspace(keyspace);

            return keyspaceMetadata != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createKeyspace(String node, int port, String keyspace) {
        try (Cluster tempCluster = Cluster.builder()
                .addContactPoint(node)
                .withPort(port)
                .build();
             Session tempSession = tempCluster.connect()) {

            tempSession.execute("CREATE KEYSPACE " + keyspace + " WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        session.close();
    }

    public Session getSession() {
        return session;
    }

    public void insert(String query) {
        System.out.println();
        if (query.trim().toLowerCase().startsWith("insert")) {
            session.execute(query);
            System.out.println("Insert executed successfully.");
        } else {
            System.out.println("Error: Provided query is not an INSERT statement.");
        }
    }

    public void update(String query){
        System.out.println();
         if (query.trim().toLowerCase().startsWith("update")) {
            session.execute(query);
            System.out.println("Update executed successfully.");
        } else {
            System.out.println("Error: Provided query is not an UPDATE statement.");
        }
    }


    public void search(String query){
        System.out.println();
        if (query.trim().toLowerCase().startsWith("select")) {
            ResultSet resultSet = session.execute(query);
            System.out.println();
            for (Row row : resultSet) {
                System.out.println(row);
            }
        } else {
            System.out.println("Error: Provided query is not a SELECT statement.");
        }
    }

}

public class Exercicio3
{
    public static void main( String[] args )
    {

        CassandraConnector connector = new CassandraConnector();
        connector.connect("127.0.0.1", 9042, "cassandra3_2");


        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    executeInsert(connector, scanner);
                    break;
                case 2:
                    executeUpdate(connector, scanner);
                    break;
                case 3:
                    executeSearch(connector, scanner);
                    break;
                case 4:
                    showQueries(connector);
                    break;
                case 5:
                    connector.close();
                    System.out.println("Bye!!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 5.");
            }
        }
        
    }





    private static void printMenu() {
        System.out.println("\n*** Menu ***");
        System.out.println("1. Insert");
        System.out.println("2. Update");
        System.out.println("3. Search");
        System.out.println("4. Show Queries from Another Exercise");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void executeInsert(CassandraConnector connector, Scanner scanner) {
        System.out.print("Enter INSERT query: ");
        String insertQuery = scanner.nextLine();
        connector.insert(insertQuery);
    }

    private static void executeUpdate(CassandraConnector connector, Scanner scanner) {
        System.out.print("Enter UPDATE query: ");
        String updateQuery = scanner.nextLine();
        connector.update(updateQuery);
    }

    private static void executeSearch(CassandraConnector connector, Scanner scanner) {
        System.out.print("Enter SEARCH query: ");
        String searchQuery = scanner.nextLine();
        connector.search(searchQuery);
    }
    private static void showQueries(CassandraConnector connector) {
        System.out.println();
        System.out.println("Queries");
        System.out.println();
        System.out.println("1. Os últimos 3 comentários introduzidos para um vídeo;");
        System.out.println();
        connector.search("select * from cassandra3_2.comentarios_video where video_id=16 limit 3;");
        System.out.println();
        System.out.println("/------------------------------------------------------------------/");
        System.out.println();
        System.out.println("3. Todos os vídeos com a tag Aveiro;");
        System.out.println();
        connector.search("select video_id from cassandra3_2.tags where name = 'Aveiro';");
        System.out.println();
        System.out.println("/------------------------------------------------------------------/");
        System.out.println();
        System.out.println("5. Vídeos partilhados por determinado utilizador (maria1987, por exemplo) num determinado período de tempo (Agosto de 2017, por exemplo);");
        System.out.println();
        connector.search("select * from cassandra3_2.video_autor where autor = 'vasco' and upload <= '2017-08-31 23:59:59' and upload >= '2017-08-01 00:00:00';");
        System.out.println();
        System.out.println("/------------------------------------------------------------------/");
        System.out.println();
         System.out.println("10. Uma query que retorne todos os vídeos e que mostre claramente a forma pela qual estão ordenados;");
        System.out.println();
        connector.search("select * from cassandra3_2.video_video;");
        System.out.println();
        System.out.println("/------------------------------------------------------------------/");
    }
}


