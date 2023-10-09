package ex5b;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Sistemab
{
    private final Jedis jedis;
    private final int limit;
    private final int timeslot;

    public Sistemab(Jedis jedis,int limit, int timeslot) {
        this.limit = limit;
        this.timeslot = timeslot;
        this.jedis=jedis;
    }



    public void Request(String username,String pedido,int quantity){
        String key="Requests:"+ username; 
        String quantityNow=String.valueOf(quantity);
        long timeleft=jedis.ttl(key);

        boolean pedidoexists=jedis.hexists(key,pedido);

        
        if (!pedidoexists || timeleft<0){
            Pipeline pipeline= jedis.pipelined();
            jedis.hdel(key,pedido);
            jedis.hset(key, pedido,quantityNow);
            jedis.expire(key,timeslot);
            pipeline.sync();
            System.out.println(username+ "  Requests: "+ pedido +  ".Unidades Requesitadas ate agora: "+ quantityNow + ".\tRequest accepted!!");
        }else{
                
            String QPjedis = jedis.hget(key,pedido);
            int quantidadeAntes=Integer.parseInt(QPjedis);
            int quantidadeAtual=quantidadeAntes+quantity;
            

                if(quantidadeAtual<= limit && timeleft>0){
                    String quantidadeAtual2=String.valueOf(quantidadeAtual);
                    Pipeline pipeline2= jedis.pipelined();
                    jedis.hset(key, pedido,quantidadeAtual2);
                    pipeline2.sync();
                    System.out.println(username+ "  Requests: "+ pedido + ".Unidades Requesitadas ate agora: " + quantidadeAtual2 + ".\tRequest accepted!!");
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

        Sistemab sistema = new Sistemab(jedis,limit,timeslot);
        int n=0;
        
        while(n<30){
            sistema.Request("Vasco", "CBD",1);
            n++;
        }
        
        sistema.Request("Vasco", "IES",2);
        sistema.Request("Vasco", "IES",3);
        sistema.Request("Vasco", "CBD",1);
        sistema.Request("Vasco", "CBD",1);
        sistema.Request("Vasco", "IA",30);
        sistema.Request("Vasco", "IA",1);

        sistema.Request("Vasco", "Peras", 15);
        jedis.close();
    }


}
