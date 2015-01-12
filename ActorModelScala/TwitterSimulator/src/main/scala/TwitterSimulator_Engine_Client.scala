import akka.actor.Actor

import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.remote._
import com.typesafe.config.ConfigFactory

import akka.actor.{ActorSystem, ActorLogging, Actor, Props, ActorRef}

import java.security.MessageDigest
//import java.util.Random
import scala.util.Random
import akka.routing.RoundRobinPool
import akka.routing.RoundRobinRouter

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer


class twitter_clients_simulator1(num_clients : Int) extends Actor {
  

  val server_proxy = context.actorFor("akka.tcp://TwitterServer@127.0.0.1:8888/user/master_server") 

    var Twitter_Clients_temp = new ListBuffer[ActorRef]
  
   val system1 = ActorSystem("twitterServerInLocal",ConfigFactory.parseString("""
       akka {
  //loglevel = "DEBUG"
  actor {
    provider = "akka.remote.RemoteActorRefProvider"
  }
  remote {
   // transport = "akka.remote.netty.NettyRemoteTransport"
      enabled-transports = ["akka.remote.netty.tcp"]
    //log-sent-messages = on
    //log-received-messages = on
    //netty {
      netty.tcp {
      hostname = "127.0.0.1"
      port = 0
    }
  }
}
       """))

 
    def receive = {
     
    
      
    case "Start" =>
        server_proxy ! "hello"
        server_proxy ! ("GetLastId")

    case "makeClientsReady" =>
      println("in making clients ready")
      for(i<-0 to num_clients-1){
           Twitter_Clients_temp(i) ! "Ready"
         }
        
      case ("buildTempUserList",last_id : Int, num_data_centers : Int ) => 
        var id = last_id
        println ("Inside buildTempUserList")
        for(i<-0 to num_clients-1){	
        	  var name = "twitter_client_node" + id.toString
      		 // Twitter_Clients_temp ::= system1.actorOf(Props[TempUser],name = name)
        	  Twitter_Clients_temp += system1.actorOf(Props(new TempUser(num_data_centers)),name = name)
        	  println("====================================================="+Twitter_Clients_temp(i))
      		id = id + 1
        }
        
        /*println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        for(i<-0 to Twitter_Clients_temp.length-1){
          println(Twitter_Clients_temp(i));          
        }*/
        
         sender ! ("AddNewUsertoList",Twitter_Clients_temp)
         
        /* Thread.sleep(5000);
         
         for(i<-0 to num_clients-1){
           Twitter_Clients_temp(i) ! "Ready"
         }*/
      
    }
}

class TempUser (num_data_centers : Int) extends Actor {
  
  var proxy_server_act_name = "server_"
  var real_server_act_name = "twitter_server_node"  
  var actor_argument =  "akka.tcp://TwitterServer1@127.0.0.1:8899/user/"
  var Map_Clients_to_Server = new ListBuffer[ActorRef] 
 //println("NO OF DATA CENTERS :" + num_data_centers)
  for (i<-0 to num_data_centers-1){
		    
		    Map_Clients_to_Server += context.actorFor("akka.tcp://TwitterServer1@127.0.0.1:8899/user/twitter_server_node"+i.toString())
    	    //println("test abc"+Map_Clients_to_Server(i));
	    
	  }
 /* 
  val server_1 = context.actorFor("akka.tcp://TwitterServer1@127.0.0.1:2553/user/twitter_server_node0") 
  val server_2 = context.actorFor("akka.tcp://TwitterServer1@127.0.0.1:2553/user/twitter_server_node1") 
  val server_3 = context.actorFor("akka.tcp://TwitterServer1@127.0.0.1:2553/user/twitter_server_node2") 
  val server_4 = context.actorFor("akka.tcp://TwitterServer1@127.0.0.1:2553/user/twitter_server_node3") 
 */ 
  var followerlist = new ListBuffer[Int]
  
  val tweetWordsList = Array("My","main","research","interests","are",
        "approximate","processing","of","database","queries","and","foundations",
        "of","data-mining/machine","learning.","","In","terms","of","approximate",
        "query","processing,","I","have","wide","interests","into","most","approximation",
        "techniques:","histograms,","sampling","and","sketches","with","an","emphasis",
        "of","developing","a","general","theory","of","approximation","for","database",
        "queries.","The","interest","in","foundational","data-mining","is","mostly","focused",
        "on","bridging","the","gap","between","Statistics","and","data-mining","by","developing",
        "semi-analytical","models","to","explain","the","behavior","of","learning","methods.",
        "A","gossip","protocol","is","a","style","of","computer-to-computer","communication",
        "protocol","inspired","by","the","form","of","gossip","seen","in","social","networks.",
        "Modern","distributed","systems","often","use","gossip","protocols","to","solve",
        "problems","that","might","be","difficult","to","solve","in","other","ways,","either",
        "because","the","underlying","network","has","an","inconvenient","structure,","is","extremely",
        "large,","or","because","gossip","solutions","are","the","most","efficient","ones","available.")
        
  def GenerateTweet : String = {
    var tweet : StringBuilder = new StringBuilder()
     var randomNumber = new scala.util.Random
     var range = 2 to (tweetWordsList.length-1);
     var randTweetLength = randomNumber.nextInt(7)
     var randIndexTweetList : Integer = 0
        
     for(i <- 0 to randTweetLength){
    	 randIndexTweetList = randomNumber.nextInt(range.length)
         tweet.append(tweetWordsList(randIndexTweetList)).append(" ")       
     }
     
     return tweet.toString()
  }
  
  def receive = {
    case "Start" =>
      
      
    case "Ready" =>
      println("Inside Ready......NumDataCenters:" + num_data_centers)
     var name = self.path.name 
     var identifier = Integer.parseInt(name.substring(19))
   
     var mapper_id = (identifier % Map_Clients_to_Server.length)
     println("Extracted Id :" + identifier + "   Being divided by :" + (Map_Clients_to_Server.length) + "  Result :" + mapper_id)
     
     Map_Clients_to_Server(mapper_id) ! "test"
     /*for (i<-0 to Map_Clients_to_Server.length-1){
       if (mapper_id == i){
         println ("TRUE")
         Map_Clients_to_Server(i) ! "test"
       }
     }*/
       
     
  
       
    case ("theseAreMyTweets", tweetMapISee : scala.collection.mutable.Map[String,scala.collection.mutable.Queue[String]]) =>
      println("********************************************************************************************************")
      println("I am "+self.path.name)
      
      tweetMapISee.foreach {keyVal => 
        println(keyVal._1 + "=" + keyVal._2.length)}
      
      println("********************************************************************************************************")
     
       
     case ("BecomeFollowerOf",x : Int)=> 
            println ("Inside BecomeFollowerOf" + "Value of X :" + x)
            var r = new scala.util.Random
            var rand_num_followers = r.nextInt((x - 0)+1) + 0
            println("Random Value" + rand_num_followers)
            for(i<-0 to rand_num_followers-1){ 
              followerlist += r.nextInt((x - 0)+1) + 0
            }
     sender ! ("BuildFollowerMap",followerlist)
   /*  var tweet : StringBuilder = new StringBuilder()
     var randomNumber = new scala.util.Random
     var range = 2 to (tweetWordsList.length-1);
     var randTweetLength = randomNumber.nextInt(range.length)
     var randIndexTweetList : Integer = 0
        
     for(i <- 0 to randTweetLength){
    	 randIndexTweetList = randomNumber.nextInt(range.length)
         tweet.append(tweetWordsList(randIndexTweetList)).append(" ")       
     }*/
     
     
     Thread.sleep(250)
     var tweet : String = GenerateTweet
     sender ! ("IHaveTweeted", tweet)
     
     Thread.sleep(250)
     tweet = GenerateTweet
     sender ! ("IHaveTweeted", tweet)
     
     Thread.sleep(250)
     tweet = GenerateTweet
     sender ! ("IHaveTweeted", tweet)
     
    /* tweet  = GenerateTweet
     sender ! ("IHaveTweeted", tweet)
     tweet = GenerateTweet
     sender ! ("IHaveTweeted", tweet)
     
     tweet = GenerateTweet
     sender ! ("IHaveTweeted", tweet)
     tweet  = GenerateTweet
     sender ! ("IHaveTweeted", tweet)
     tweet = GenerateTweet
     sender ! ("IHaveTweeted", tweet)
     
     tweet = GenerateTweet
     sender ! ("IHaveTweeted", tweet)
     tweet  = GenerateTweet
     sender ! ("IHaveTweeted", tweet)
     tweet = GenerateTweet
     sender ! ("IHaveTweeted", tweet)*/
     
    // Thread.sleep(1000)
     
    // sender ! ("SendTweetsISee")
  }
}

object TwitterSimulator_Engine_Client extends App {
println("Starting Client...")
var numProcs = Runtime.getRuntime().availableProcessors();
var num_clients = 20*numProcs
//var num_clients = Integer.parseInt(args(0))
implicit val system = ActorSystem("TwitterClient", ConfigFactory.parseString("""
  akka {
  //loglevel = "DEBUG"
  actor {
    provider = "akka.remote.RemoteActorRefProvider"
  }
  remote {
   // transport = "akka.remote.netty.NettyRemoteTransport"
      enabled-transports = ["akka.remote.netty.tcp"]
    //log-sent-messages = on
    //log-received-messages = on
    //netty {
      netty.tcp {
      hostname = "127.0.0.1"
      port = 0
    }
  }
}
      """))
var Twitter_Starter= system.actorOf(Props(new twitter_clients_simulator1(num_clients)),name = "Twitter_Starter")
//println (Twitter_Starter.path.name)
//println (Twitter_Starter.path)
Twitter_Starter ! "Start"

}
/*class SpraySys extends Actor {
  
  def receive = {
    case ("Start", id : Int) =>
      
      
  }
  
}*/
