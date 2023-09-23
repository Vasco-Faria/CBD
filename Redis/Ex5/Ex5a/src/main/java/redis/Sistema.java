package redis;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.concurrent.TimeUnit;


public class Sistema
{
    private final Jedis jedis;
    private final int limit;
    private final int timeslot;

    public Sistema(Jedis jedis,int limit, int timeslot) {
        this.limit = limit;
        this.timeslot = timeslot;
        this.jedis=jedis;
    }



    public void Request(String username,String pedido){
        String key="Requests:"+ username; 

        long timeleft=jedis.ttl(key);

        boolean keyexists=jedis.exists(key);
        
        if (!keyexists && timeleft<0){
            Pipeline pipeline= jedis.pipelined();
            jedis.del(key);
            jedis.rpush(key, pedido);
            jedis.expire(key,timeslot);
            pipeline.sync();
            System.out.println(username+ "  Requests: "+ pedido +  ".\tRequest accepted!!");
        }else{
            long QuantidaDePedidos= jedis.llen(key);

            if(QuantidaDePedidos < limit && timeleft>0){
                Pipeline pipeline2= jedis.pipelined();
                jedis.rpush(key, pedido);
                pipeline2.sync();
                System.out.println(username+ "  Requests: "+ pedido +  ".\tRequest accepted!!");
            }else{
                System.out.println(username+ "  Requests: "+ pedido +  ".\tRequest not accepted...");
            }
        }

       

    }



    public static void main( String[] args ) 
    {
        int limit=30;
        int timeslot=60*60;
        Jedis jedis = new Jedis("localhost",6379);

        Sistema sistema = new Sistema(jedis,limit,timeslot);
        int n=1;

        while(n<35){
            sistema.Request("Vasco", "CBD");
            n++;
        }
        sistema.Request("Vasco","IES");
        jedis.close();
    }


}
