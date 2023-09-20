package com.example;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import redis.clients.jedis.Jedis;

public class ex14a 
{

    public static String USERS = "users"; 

    public static void autocomplete(String prefix, Jedis jedis) {

            // ler nomes da lista
            List<String> allNames = jedis.lrange(USERS, 0, -1);
            List<String> matchedNames = new ArrayList<>();

            for (String n : allNames) {
                if (n.toLowerCase().startsWith(prefix.toLowerCase())) {
                    matchedNames.add(n);
                }
            }

             //ordernar alfabeticamente 
            Collections.sort(matchedNames); 

            for (String user : matchedNames) {
                System.out.println(user);
            }
    }

    public static void main( String[] args )

    {
        Jedis jedis = new Jedis();
         Scanner sc = new Scanner(System.in);
         jedis.del(USERS);

        try(Scanner txt = new Scanner(new FileReader("names.txt"))){
            while(txt.hasNextLine()) {
                String name = txt.nextLine();
                jedis.rpush(USERS,name);

            }
        } catch (FileNotFoundException e){
            System.err.println("Error. File not found");
        }

        

        while(true){
            System.out.print("Search for ('Enter' for quit): ");
            String input = sc.nextLine();
            if (input.equals("")){
            	jedis.close();
                break;
            }
            else
            {
                autocomplete(input, jedis);
                System.out.println();
            }
        }
    }
}
