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



    public void Request(String username,String pedido){
        String key="Requests:"+ username; 

        long timeleft=jedis.ttl(key);

        boolean keyexists=jedis.exists(key);

        long quantityNow=1;

        //caso utilizador ainda nao tenha feito nenhum pedido 
        //cria uma lista para este com todos os pedidos e com um tempo de vida
        
        if (!keyexists && timeleft<0){
            Pipeline pipeline= jedis.pipelined();
            jedis.del(key);
            jedis.rpush(key, pedido);
            jedis.expire(key,timeslot);
            pipeline.sync();
            System.out.println(username+ "  Requests: "+ pedido +  ".\tRequest accepted!!");
        }else{
                

            //obter pedidos feitos pelos utilizador
            List<String> elements=jedis.lrange(key,0,-1);


            //ver quantos produtos destes ja pediu 
            for(String e:elements){
                    if(e.equals(pedido)){
                        quantityNow++;
                    }
            }

            


            //ver se o utilizador ja pediu mais do que o limite de unidades por produto

                if(quantityNow <= limit && timeleft>0){
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

        Sistemab sistema = new Sistemab(jedis,limit,timeslot);
        int n=0;

        while(n<30){
            sistema.Request("Vasco", "CBD");
            n++;
        }
        sistema.Request("Vasco", "IES");
        sistema.Request("Vasco", "IES");
        sistema.Request("Vasco", "CBD");
        sistema.Request("Vasco", "CBD");
        sistema.Request("Vasco", "IA");
        sistema.Request("Vasco", "IA");

        jedis.close();
    }


}
