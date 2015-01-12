import spray.routing.SimpleRoutingApp
import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSelection.toScala
import akka.actor.actorRef2Scala
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing.Directive.pimpApply
import akka.actor.ActorRef
import scala.collection.mutable.ListBuffer

object TwitterSimulator_RestAPI_Service extends App with SimpleRoutingApp {
 implicit val actorSystem = ActorSystem()

// val system = ActorSystem("sprayTrial")
 
 val system = ActorSystem("sprayTrial", ConfigFactory.parseString("""
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
      port = 5555
    }
  }
}
      """))
 
 
 val Master = system.actorOf(Props[sprayActor], name = "master")
 	Master ! "getServers"    
}

class sprayActor() extends Actor with SimpleRoutingApp { 
  
  val server_proxy = context.actorSelection("akka.tcp://TwitterServer@127.0.0.1:8888/user/master_server")
  
  //val clientMaster = context.actorSelection("akka.tcp://twitter_clients_simulator1@127.0.0.1:8800/user/Twitter_Starter")
  
  
  val systemForCreatingClient = ActorSystem("twitterServerInLocal",ConfigFactory.parseString("""
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
  
  
 // server_proxy ! "getTotalTweets" 
  var tweetsTillNow : Int = 0
  var totUserTillNow : Int = 0
  //var id : Int = 10
  var totalServers : Int = 0
  var Map_Clients_to_Server = new ListBuffer[ActorRef]
  var tweetMapISeeSpray : scala.collection.mutable.Map[String,scala.collection.mutable.Queue[String]] = scala.collection.mutable.Map()
  var IFollowList = new ListBuffer[Int]
  var tweetMapISeeSprayString : String = "Yet to get any tweets"
  var IFollowSprayString : String = "Waiting for someone to follow me"
  
  def receive = {
    
     case "getServers" =>
      	server_proxy ! "sprayGetTotalServers"
    
    case "prepareSystem" =>
	  println("building map"+totalServers)
	         
	   for (i<-0 to totalServers-1){
		    
		    Map_Clients_to_Server += context.actorFor("akka.tcp://TwitterServer1@127.0.0.1:8899/user/twitter_server_node"+i.toString())
    	    println("test abc"+Map_Clients_to_Server(i));
	    
	  }
      	println("++++++++++++"+Map_Clients_to_Server.length)
      	self ! "hey"
    
    case "hey" =>
       implicit val actorSystem = ActorSystem()

       startServer (interface = "localhost", port = 8000) {
	   get {
	     path("hello") {
	       complete {        
	         "Welcome to Twitter API!"
	       }
	     }
	   } ~
	   get {
	     path("getTotalTweets") {	    
	       //Thread.sleep(5000)
	       complete {   	         
	         server_proxy ! "getTotalTweetsSpray"
	         "Total number of tweets till now : \n"+tweetsTillNow
	       }
	     }
	   } ~
	   get {
	     path("getTotalUsers") {
	       complete {
	         server_proxy ! "getTotalUsersSpray"
	         "Total number of users : \n"+totUserTillNow
	       }
	     }
	   } ~
	   get {
	     path("getMyTweets") {
	      parameters("id".as[Int]) { (id) =>
	       complete {
	         println("******************"+Map_Clients_to_Server.length)
	         var mapper_id = (id % Map_Clients_to_Server.length)
	         println(mapper_id+"mapper id map"+Map_Clients_to_Server(mapper_id));
	         Map_Clients_to_Server(mapper_id) ! ("sprayTweetsISee", id)
	         "Total tweets I see along with who tweeted\n"+tweetMapISeeSprayString
	       }
	       }
	     }
	   } ~
	   get {
	     path("getWhomIFollow") {
	      parameters("id".as[Int]) { (id) =>
	       complete {
	         //println("******************"+Map_Clients_to_Server.length)
	         var mapper_id = (id % Map_Clients_to_Server.length)
	         //println(mapper_id+"mapper id map")//+Map_Clients_to_Server(mapper_id));
	         Map_Clients_to_Server(mapper_id) ! ("sprayWhomIFollow", id)
	         "Check whom I follow : "+IFollowSprayString
	       }
	       }
	     }
	   } ~
	   get {
	     path("addUsers") {	    
	       parameters("id".as[Int]) { (id) =>	       
	       complete {   	     
	         println(id+" User(s) Added! Do 'getTotalUsers' to see.")
	         server_proxy ! ("GetLastIdSpray", id)
	         id+" User(s) Added! Do 'getTotalUsers' to see."
	       }
	       }
	     }
	   } ~
	   get {
	     path("postTweet") {	    
	       parameters("id".as[Int], "tweet".as[String]) { (id, tweet) =>	
	       //http://localhost:8000/postTweet?id=7&tweet=abcdef  
	       complete {   	         
	         //println("******************"+Map_Clients_to_Server.length)
	         var mapper_id = (id % Map_Clients_to_Server.length)
	         //println(mapper_id+"mapper id map")//+Map_Clients_to_Server(mapper_id));
	         println(id+" posted tweet "+tweet)
	         Map_Clients_to_Server(mapper_id) ! ("SprayIHaveTweeted", tweet, id)
	         id+" posted tweet "+tweet
	       }
	       }
	     }
	   } ~
	   get {
	     path("IFollowU") {	    
	       parameters("myId".as[Int], "yourId".as[Int]) { (me, you) =>	
	       //http://localhost:8000/postTweet?id=7&tweet=abcdef  
	       complete {   
	         println(me+" is now following "+you)
	         var mapper_id = (me % Map_Clients_to_Server.length)
	         var ifollowuList = new ListBuffer[Int]
	         ifollowuList += you
	         Map_Clients_to_Server(mapper_id) ! ("SprayBuildFollowerMap",ifollowuList, me)
	         me+" is now following "+you
	       }
	       }
	     }
	   }
	   
	 }   
       
       
    case ("buildTempUserListSpray",last_id : Int, num_data_centers : Int, idFromServr : Int ) => 
        var id = last_id
        //println ("Inside buildTempUserListSpray num users id : "+id+" length : "+last_id+ " num servers "+num_data_centers)
        var Twitter_Clients_temp = new ListBuffer[ActorRef]
        for(i<-0 to idFromServr-1){	
        	  var name = "twitter_client_node" + id.toString
      		 // Twitter_Clients_temp ::= system1.actorOf(Props[TempUser],name = name)
        	  Twitter_Clients_temp += systemForCreatingClient.actorOf(Props(new TempUser(num_data_centers)),name = name)
        	 // println("====================================================="+Twitter_Clients_temp(i))
      		id = id + 1
        }
        
         sender ! ("AddNewUsertoList",Twitter_Clients_temp)
       
    case ("IFollowResp", id, followerListSpray : ListBuffer[Int]) =>
      IFollowList = followerListSpray
      var IFollow = new StringBuilder()
      println("********************************************************************************************************")
      println("==================================== I, "+id +", follow ================================================")
      IFollow.append("==================================== I, ").append(id).append(", follow ================================================\n")
      followerListSpray.foreach { k =>
        println(k)        
        IFollow.append(k).append("\t")
      }
      println("********************************************************************************************************")
      IFollowSprayString = IFollow.toString()
      
    case ("IFollowResp", id, null) =>
      println("********************************************************************************************************")
      println("==================================== I, "+id +", follow only myself! #ProudGator! ================================================")
      println("********************************************************************************************************")
       
    case ("SpraytheseAreMyTweets", id : Int, tweetMapISee : scala.collection.mutable.Map[String,scala.collection.mutable.Queue[String]]) =>
      tweetMapISeeSpray = tweetMapISee
      var tweet = new StringBuilder()
      println("********************************************************************************************************")
      println("==================================== Tweets For "+id +"================================================")
      tweet.append("==================================== Tweets For ").append(id).append("================================================")
      
      tweetMapISee.foreach {keyVal => 
        println("\n================================"+keyVal._1 + " has sent "+keyVal._2.length+" Tweets =========================== ")// + keyVal._2)
        tweet.append("\n\n").append("------------------------------").append(keyVal._1).append(" has sent ").append(keyVal._2.length).append(" tweets").append("------------------------------")
        keyVal._2.foreach { k =>
          println(k)          
          tweet.append("\n").append(k)
        }      
      }
      println("********************************************************************************************************")
      tweetMapISeeSprayString = tweet.toString()
       
    case  ("totalServersResp", num_servers : Int) =>
      totalServers = num_servers
      self ! "prepareSystem"
       
    case ("totalUsersResp",userLength : Int) =>
      totUserTillNow = userLength
      println("Total number of users at ")
      println(userLength) 
       
    case ("totaltweetsResp", totalNumTweets : Int) =>
      tweetsTillNow = totalNumTweets
      println("Total number of tweets at ")
      println(totalNumTweets)       
  }
}