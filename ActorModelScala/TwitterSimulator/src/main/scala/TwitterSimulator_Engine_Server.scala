import akka.actor.Actor

import akka.actor.ActorSystem
import akka.actor.Props
import akka.remote._
import com.typesafe.config.ConfigFactory
import akka.actor.{ActorSystem, Actor, Props, ActorRef}
import scala.collection.mutable.ListBuffer
import akka.actor.ActorSelection.toScala
import akka.actor.actorRef2Scala

class twitter_server_simulator (num_servers : Int) extends Actor {

   val All_Twitter_Clients = new ListBuffer[ActorRef]
    var All_Twitter_Servers = new ListBuffer[ActorRef]
   
   var totalNumTweetsSpray : Int = 0
   
   var totalNumTweets : Int = 0
   var counter : Int = 0
   
   val sprayClient = context.actorSelection("akka.tcp://sprayTrial@127.0.0.1:5555/user/master")

    def receive = {
    
     case "Start" => 
      val actorName = self.path.name
      println(" Actor Name : " + actorName + ".... UP & Ready  " + self.path)    
      
      /*******************************************************************************************/
       val system1 = ActorSystem("TwitterServer1",ConfigFactory.parseString("""
        akka {
  //loglevel = "DEBUG"
  actor {
    provider = "akka.remote.RemoteActorRefProvider"
   }
   remote {
     //transport = "akka.remote.netty.NettyRemoteTransport"
      enabled-transports = ["akka.remote.netty.tcp"]
     //log-sent-messages = on
     //log-received-messages = on
     //netty {
       netty.tcp {
       hostname = "127.0.0.1"
       port = 8899
     }
   }
}
      """))
      for(i<-0 to num_servers-1){
            val name = "twitter_server_node" + i.toString
      		//All_Twitter_Servers ::= system1.actorOf(Props(new twitter_server_simulator1(size)), name = name)
      		All_Twitter_Servers += system1.actorOf(Props[twitter_server_simulator1], name = name)
      	
      	}
   for(i<-0 to All_Twitter_Servers.length - 1){
     All_Twitter_Servers(i) ! "Start"   
    
      }
   
 //  Thread.sleep(600000)
    
   //spray
     case "sprayGetTotalServers" =>
       println("server side "+num_servers);
       sender ! ("totalServersResp", num_servers)
   
     case "getTotalUsersSpray" =>
       sender ! ("totalUsersResp",All_Twitter_Clients.length)
     
     case "getTotalTweetsSpray" =>
       println("*******************************just called by spray server**********************************")
       for(i<-0 to All_Twitter_Servers.length - 1){
    	   All_Twitter_Servers(i) ! "getNumTweetsSpray"      
      }
      // Thread.sleep(5000)      
      //sender ! ("totaltweetsResp", totalNumTweetsSpray)       
              
     case ("takeNumTweetsSpray", numTweetsSpray : Int) =>
        totalNumTweetsSpray = totalNumTweetsSpray + numTweetsSpray
        counter = counter + 1
       if(counter == All_Twitter_Servers.length){
         println("*******************************Sending to spray server**********************************")
         //val sprayClient = context.actorSelection("akka.tcp://sprayTrial@127.0.0.1:5555/user/master")
         counter = 0
         var totNum = totalNumTweetsSpray
         totalNumTweetsSpray = 0
         sprayClient ! ("totaltweetsResp", totNum)
       }
     
     case "stopActionFromMain" =>
       println("********************************************* sending stop action");
       for(i<-0 to All_Twitter_Servers.length - 1){
     All_Twitter_Servers(i) ! "getNumTweets"
    
      }
         
     case ("takeNumTweets", numTweets : Int) =>
       println("*******************************************sizeOfServer "+All_Twitter_Servers.length+" ********************************");
       totalNumTweets = totalNumTweets + numTweets
       counter = counter + 1
       if(counter == All_Twitter_Servers.length){
         println("************************************* counter "+counter)
         println("=========================================================================================== ");
         println("=========================================================================================== ");
         println("=========================================================================================== ");
         println("=========================================================================================== ");

         println("====================== Total number of tweets handled in 2 minutes ======================== " +totalNumTweets);
         //considering approx time taken in setup and ignoring approx time taken in follower building, etc.
         println("====================== Rate of tweets handled per second ======================== " +totalNumTweets/80);

         println("=========================================================================================== ");
         println("=========================================================================================== ");
         println("=========================================================================================== ");
         println("=========================================================================================== ");
         counter = 0
         context.system.shutdown()
       }
     
     case "hello" =>
       println("REMOTING WORKS !!")
      
     case ("GetLastId") =>  
       println ("inside GetLastID")
       //sender ! ("buildTempUserList",All_Twitter_Clients.length)
       sender ! ("buildTempUserList",All_Twitter_Clients.length,num_servers)
       
       
     case ("GetLastIdSpray", id : Int) =>  
       println ("SSSSSSSSSSSSSSPPPPPPPPPPPPPPPPPPPRRRRRRRRRRRRRRAAAAAAAAAAAAYYYYYYYYYYYYYYYYYYYYY inside GetLastIDSpray "+id)
       sender ! ("buildTempUserListSpray",All_Twitter_Clients.length,num_servers, id)
       
       
     case ("AddNewUsertoList",twitterclientlist:ListBuffer[ActorRef])=> 
       println ("Inside AddNewUsertoList")
      // var last_id = .length
       for(i<-0 to twitterclientlist.length-1){
         All_Twitter_Clients += twitterclientlist(i)
       
        // printer1 ! print(All_Twitter_Clients)
       } // EOF for
       
       for(i<-0 to All_Twitter_Clients.length-1){
         
         println("ID :"+ i +"   "+ All_Twitter_Clients(i)) 
       }
       
       
       
      /****************************************/ 
       var size = All_Twitter_Clients.length     
       
       sender ! "makeClientsReady"
     
       
       for(i<-0 to All_Twitter_Servers.length - 1){
         println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& "+size)
     All_Twitter_Servers(i) ! ("SizeInformation",size)   
     //send all client list to servers
     All_Twitter_Servers(i) ! ("getAllClients", All_Twitter_Clients)
      }   
 
} // EOF def recieve

} // EOF class twitter_server_simulator

//class twitter_server_simulator1 (size : Integer) extends Actor {
class twitter_server_simulator1 extends Actor {  
  var followersMap : scala.collection.mutable.Map[ActorRef,ListBuffer[Int]] = scala.collection.mutable.Map()
  var s = 0
  var tweetMap : scala.collection.mutable.Map[ActorRef,scala.collection.mutable.Queue[String]] = scala.collection.mutable.Map()
  var AllClients = new ListBuffer[ActorRef]
  
  var run_time : Long = 0
  
  def receive = {
    
    case "getNumTweetsSpray" =>
      //println("*******************************************in getting num tweets SPRAY method ");
      var numTweetsSpray : Int = 0
      tweetMap.foreach {keyVal => 
        numTweetsSpray = numTweetsSpray + (keyVal._2.length)
        }
      println("*******************************sending collection**********************************")
      sender ! ("takeNumTweetsSpray", numTweetsSpray)
    
    case "getNumTweets" =>
      println("*******************************************in getting num tweets shutdown method ");

      var numTweets : Int = 0
      tweetMap.foreach {keyVal => 
        numTweets = numTweets + (keyVal._2.length)
        }
      
    /*  println("Ba ba ba ba ba ba ba ba ba @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
      var total : Int = 0
      tweetMap.foreach {keyVal => 
        total = total + keyVal._2.length}
       println("######################################"+ total)*/
      
      sender ! ("takeNumTweets", numTweets)
      context.system.shutdown()
    
    case "Start" =>
      println ("Inside NEW Server")
      val actorName = self.path.name
      println(" Actor Name : " + actorName + ".... UP & Ready  " + self.path) 
    
    case ("SizeInformation",size : Int) => 
      s = size
      println("Size Information :" + size)
      
    case ("getAllClients", clientList :  ListBuffer[ActorRef]) =>
      AllClients = clientList
      
    case "test"=>
      println ("Inside Test" + s)
      sender ! ("BecomeFollowerOf",s)
      
    case ("SprayBuildFollowerMap",followerList : ListBuffer[Int], me : Int) =>
      println("In SprayBuildFollowerMap")      
      var myActorRef : ActorRef = AllClients(me)      
      var existingFollowers = followersMap.getOrElse(myActorRef, null)
      if(existingFollowers == null){
        followersMap += myActorRef -> followerList
      }
      else{
        for(i<-0 to followerList.length-1){
          if(!existingFollowers.contains(followerList(i))){
        	  existingFollowers += followerList(i)
          }
        }      
        followersMap.update(myActorRef, existingFollowers)
      }      
      
    case ("BuildFollowerMap",followerList : ListBuffer[Int])=>
      println("In BuildFollowerMap")
      var existingFollowers = followersMap .getOrElse(sender, null)
      if(existingFollowers == null){
        followersMap += sender -> followerList
      }
      else{
        for(i<-0 to followerList.length-1){
          if(!existingFollowers.contains(followerList(i))){
        	  existingFollowers += followerList(i)
          }
        }      
        followersMap.update(sender, existingFollowers)
      }      
      
      
    case ("sprayWhomIFollow", id : Int) =>
      var myActorRef : ActorRef = AllClients(id)
      var followerListSpray = followersMap .getOrElse(myActorRef, null)
      sender ! ("IFollowResp", id, followerListSpray)
      
    case ("SprayIHaveTweeted", tweet : String, id : Int) =>
      println("In SprayIHaveTweeted")
      
      var myActorRef : ActorRef = AllClients(id)
   
      var myQ = tweetMap.getOrElse(myActorRef, null)
      
      if(myQ == null){
        myQ = scala.collection.mutable.Queue[String]()
        myQ.enqueue(tweet)
        tweetMap += myActorRef -> myQ
      }
      else{
        if(myQ.length == 1000){
        		  println("Queue full. Can not handle more messages. Dequeuing old tweets to insert new")
        		  myQ.dequeue
        		  myQ.enqueue(tweet)
        	  }
          else{
        	  myQ.enqueue(tweet)
          }
         tweetMap.update(myActorRef, myQ)  
      }      
      //sender ! ("BecomeFollowerOf",s) 
      
    case ("sprayTweetsISee", id : Int) =>
     /* for (i<-0 to AllClients.length-1){
        println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&unsorted list "+AllClients(i))
      }      
      println("my id "+id)
      println("self "+self.path.name)*/
      
      var myActorRef : ActorRef = AllClients(id)
      
      println(id+" SPRAAAAAAAAAAAAAAAAAAAAAAYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY my info " + myActorRef)
      
      var IFollow = followersMap.getOrElse(myActorRef, null)
      
      var tweetMapISee : scala.collection.mutable.Map[String,scala.collection.mutable.Queue[String]] = scala.collection.mutable.Map()
      
      if(IFollow == null){
        //println(myActorRef.path.name+" am my only follower :(")
        
        var myQ = tweetMap.getOrElse(myActorRef, null)
        if(myQ == null){
          println("SPRAAAAAAAAAAAAAAAAAAAAAAYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY INSIDE I GOT NULL" + myActorRef)
        }
        else{
          myQ = tweetMap.getOrElse(myActorRef, null) 
          tweetMapISee += myActorRef.path.name -> myQ
        }
      }
      else{
        var myQ = tweetMap.getOrElse(myActorRef, null)
        if(myQ == null){
          //println(myActorRef.path.name+" have followed people but never tweeted. Be patient for my valueable tweets baby! :)")
        }
        else{
          tweetMapISee += myActorRef.path.name -> myQ
          var IndivFollow : ActorRef = null
          for(i<-0 to IFollow.length-1){
            //println("&&&&&&&&&&&&&&&&&"+IFollow(i)+" &&&&&&&&&&&&&&&&&&&&&& "+AllClients.length)
            if(IFollow(i) == 0){
              IndivFollow = AllClients(IFollow(i))
            } else{
              IndivFollow = AllClients(IFollow(i)-1)
            }
            var followTweets = tweetMap.getOrElse(IndivFollow, null)
            if(followTweets == null){
              //println("follower"+IndivFollow.path.name+" never tweeted! bad guy! ")
            }
            else{
              tweetMapISee += IndivFollow.path.name -> followTweets
            }
            
          }
        }

      }
      
      tweetMapISee.foreach { kv =>
        println("SPRAAAAAAAAAAAAAAAAAAAAAAYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY"+ kv._1 +"valueee "+kv._2)}
       
      sender ! ("SpraytheseAreMyTweets", id, tweetMapISee)
        
      
      
    case ("SendTweetsISee") =>
      var IFollow = followersMap.getOrElse(sender, null)
      var tweetMapISee : scala.collection.mutable.Map[String,scala.collection.mutable.Queue[String]] = scala.collection.mutable.Map()
      
      if(IFollow == null){
        println(sender.path.name+" am my only follower :(")
        
        var myQ = tweetMap.getOrElse(sender, null)
        if(myQ == null){
          println(sender.path.name+" do not follow anyone and I did not tweet too! I am an useless user! :(")
        }
        else{
          myQ = tweetMap.getOrElse(sender, null) 
          tweetMapISee += sender.path.name -> myQ
        }
      }
      else{
        var myQ = tweetMap.getOrElse(sender, null)
        if(myQ == null){
          println(sender.path.name+" have followed people but never tweeted. Be patient for my valueable tweets baby! :)")
        }
        else{
          tweetMapISee += sender.path.name -> myQ
          var IndivFollow : ActorRef = null
          for(i<-0 to IFollow.length-1){
            //println("&&&&&&&&&&&&&&&&&"+IFollow(i)+" &&&&&&&&&&&&&&&&&&&&&& "+AllClients.length)
            if(IFollow(i) == 0){
              IndivFollow = AllClients(IFollow(i))
            } else{
              IndivFollow = AllClients(IFollow(i)-1)
            }
            var followTweets = tweetMap.getOrElse(IndivFollow, null)
            if(followTweets == null){
              //println("follower"+IndivFollow.path.name+" never tweeted! bad guy! ")
            }
            else{
              tweetMapISee += IndivFollow.path.name -> followTweets
            }
            
          }
        }

      }
      
      sender ! ("theseAreMyTweets", tweetMapISee)
      sender ! ("BecomeFollowerOf",s)
      
            
    case ("IHaveTweeted", tweet : String) =>
      //if("twitter_client_node60".equalsIgnoreCase(sender.path.name))
      println("In IHaveTweeted = "+sender.path.name)
   
      var myQ = tweetMap.getOrElse(sender, null)
      
      if(myQ == null){
        myQ = scala.collection.mutable.Queue[String]()
        myQ.enqueue(tweet)
        tweetMap += sender -> myQ
      }
      else{
        if(myQ.length == 1000){
        		  println("Queue full. Can not handle more messages. Dequeuing old tweets to insert new")
        		  myQ.dequeue
        		  myQ.enqueue(tweet)
        	  }
          else{
        	  myQ.enqueue(tweet)
          }
         tweetMap.update(sender, myQ)  
      }      
      sender ! ("BecomeFollowerOf",s) 
      
  }
}

object TwitterSimulator_Engine_Server extends App {
  
  val system = ActorSystem("TwitterServer",ConfigFactory.parseString("""
  akka {
  //loglevel = "DEBUG"
  actor {
    provider = "akka.remote.RemoteActorRefProvider"
   }
   remote {
     //transport = "akka.remote.netty.NettyRemoteTransport"
      enabled-transports = ["akka.remote.netty.tcp"]
     //log-sent-messages = on
     //log-received-messages = on
     //netty {
       netty.tcp {
       hostname = "127.0.0.1"
       port = 8888
     }
   }
}
      """))
  
   //var num_Servers = Integer.parseInt(args(0))
  var num_Servers:Integer = Runtime.getRuntime().availableProcessors();
   println("Attempting to start Server ...")
   var master_server = 	system.actorOf(Props(new twitter_server_simulator(num_Servers)), name = "master_server")						
  master_server ! "Start"
   
   Thread.sleep(120000)
   
   println("********************************* initiating stop")
  master_server ! "stopActionFromMain"
   
} // EOF Main class
