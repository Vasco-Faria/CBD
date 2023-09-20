package com.example;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import redis.clients.jedis.Jedis;


public class App 
{
    public static String USERS_KEY = "users"; //set
    public static String USERS_LIST_KEY = "users_list";  //list
    public static String USERS_HASHMAP_KEY = "users_hashmap";  //hashmap
    public static void main( String[] args )
    {
        Jedis jedis = new Jedis();
        // sets
        String[] users = { "Ana", "Pedro", "Maria", "Luis" };
        
        for (String user:users){
            jedis.sadd(USERS_KEY,user);
        }

        System.out.println("\nUsers in the set:");
        jedis.smembers(USERS_KEY).forEach(System.out::println);

        // Lists

        String[] users2 = { "Andreia", "Paulo", "Mariana", "Lara" };

        for (String user : users2) {
            jedis.rpush(USERS_LIST_KEY, user);
        }

        System.out.println("\nUsers in the list:");
        jedis.lrange(USERS_LIST_KEY, 0, -1).forEach(System.out::println);


        //Hashmap

        Map<String,String> users3 = new HashMap<>();

        users3.put("1","Sandra");
        users3.put("2","Isabel");
        users3.put("3","Bruno");
        users3.put("4","Miguel");

        jedis.hmset(USERS_HASHMAP_KEY,users3);

        Map<String,String> newUserMap = jedis.hgetAll(USERS_HASHMAP_KEY);
        System.out.println("\nUsers in the hashmap:");

        for (Map.Entry<String,String> entry : newUserMap.entrySet()){
            System.out.println("ID: " + entry.getKey() + ", Name: " + entry.getValue());
        }

        jedis.close();

    }
}
 