package com.example;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import redis.clients.jedis.Jedis;

public class App 
{

    public static String USERS = "users"; 

    public static void autocomplete(String prefix, Jedis jedis) {

            HashMap<String,String> datafromjedis= (HashMap<String,String>) jedis.hgetAll(USERS);

           
            List<Map.Entry<String, String>> matchingEntries = new ArrayList<>();

            for (Map.Entry<String, String> entry : datafromjedis.entrySet()) {
                String key = entry.getKey();
                if (key.toLowerCase().startsWith(prefix.toLowerCase())) {
                    matchingEntries.add(entry);
                }
            }
        
            matchingEntries.sort((entry1, entry2) -> Integer.compare(
                    Integer.parseInt(entry2.getValue()), Integer.parseInt(entry1.getValue())
            ));
        
            for (Map.Entry<String, String> entry : matchingEntries) {
                System.out.println(entry.getKey());
            }
    }

    public static void main( String[] args )
    {
        Jedis jedis = new Jedis();
        String line;
        Scanner scanner = new Scanner(System.in);
        jedis.del(USERS);
        HashMap<String,String> data= new HashMap<>();

        try(BufferedReader csvfile = new BufferedReader(new FileReader("nomes-pt-2021.csv"))){
            
            while((line = csvfile.readLine()) != null){
                String[] dados = line.split(";");
                data.put(dados[0],dados[1]);
            }
        }catch (IOException e){
            jedis.close();
            scanner.close();
            System.err.println("Could not open file");
        }

         jedis.hmset(USERS,data); 

        while(true){
            System.out.print("Search for ('Enter' for quit): ");
            String input = scanner.nextLine();
            if (input.equals("")){
                jedis.close();
                scanner.close();
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
