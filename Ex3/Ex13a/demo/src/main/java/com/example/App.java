package com.example;

import redis.clients.jedis.Jedis;

public class App 
{
    public static void main( String[] args )
    {
        Jedis jedis = new Jedis();
        System.out.println(jedis.ping());
        System.out.println(jedis.info());
        jedis.close();
    }
}

