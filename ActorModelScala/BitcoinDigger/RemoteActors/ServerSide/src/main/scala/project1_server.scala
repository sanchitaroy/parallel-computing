package server

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.remote._
import com.typesafe.config.ConfigFactory

import akka.actor.{ActorSystem, ActorLogging, Actor, Props, ActorRef}

import java.security.MessageDigest
import scala.util.Random
import akka.routing.RoundRobinPool
import akka.routing.RoundRobinRouter

import common._

case class Start(value: Integer)
case class bitcoinCount(value1: String, value2: Integer, value3:Array[String], value4: Array[String])
case class CalcSHA(value1: String, value2: Integer)

//case class Signal_from_Client(k : Integer)

class masterActor() extends Actor with ActorLogging{
 
  var totalCount: Integer = 0
  var numProcs: Integer = Runtime.getRuntime().availableProcessors() - 1;
  var numResults: Integer = 0
  var highestZero: Int = 0
  var highestZeroCoin: String = null
  var allBitcoinStr : StringBuilder = new StringBuilder()
  
 // val t1 = System.currentTimeMillis
     
  def receive = {
    case Start(value) => 
           
    val roundRobinRouter =
    context.actorOf(Props[workerActor].withRouter(RoundRobinRouter(numProcs)), "router")
  
      1 to numProcs foreach {
    i => roundRobinRouter ! CalcSHA(i.toString(), value)
    }
      
    case bitcoinCount(value1, value2, value3, value4) =>
      totalCount = totalCount+value2
      allBitcoinStr.append("\n").append(value4(0))
      
      println("number of bitcoins is in actor " + value1 +" is: "+value2);
      
      var zeroCount: Int = Integer.parseInt(value3(0))
      if(zeroCount>highestZero){
        highestZero = zeroCount
        highestZeroCoin = value3(1)
      }
      else if(zeroCount == highestZero && highestZero != 0){        
        highestZeroCoin = highestZeroCoin+"\n"+value3(1)        
      }
      
      numResults = numResults+1
      if(numResults == numProcs){
        println("*******************   RESULTS FROM SERVER      *******************\n")

        println("Total # of BITCOINS found  : "+totalCount); 
        println("INPUT STRING                        BIT COINS " +allBitcoinStr);
		println ("=============================================================")
        println("Max # of leading Zeroes from mined Bit Coins is :"+highestZero); 
        println("Bitcoins with highest number of zeros is (coin-hashValue pair separated by new line) : "+"\n"+highestZeroCoin); 
		
		
       // println("Total time taken to run in "+(numProcs+1)+" number of cores is : "+ t1+System.currentTimeMillis + " millisecs");
        context.system.shutdown()
      }
    case _ => log.info("invalid start of program")
    	context.system.shutdown()
  } 
}

class workerActor extends Actor with ActorLogging{
  
  final val UFID:String = "sroy" 
  final val HASH_TYPE = "SHA-256" 
  final val hashval = MessageDigest.getInstance(HASH_TYPE) 

  var numBitcoin : Integer = 0
  var actorId : String = null
  var maxZeroBitcoin: Array[String] = new Array[String](2)
  maxZeroBitcoin(0) = "0"
  maxZeroBitcoin(1) = null
  
  var allBitcoin: Array[String] = new Array[String](1)
  allBitcoin(0) = ""
  
  def getRandom(SpclChar:String) : String = {
       
       var randomStr:StringBuilder = new StringBuilder()
       
       randomStr.append(UFID).append(SpclChar).append("s")
       randomStr.append(Random.alphanumeric.take(10).mkString)
       
      return randomStr.toString()
     } 
   
  def checkBitcoin(inputStr:String, randomStr:String, numZero:Int) : Unit = {
    
    var bitcoin : StringBuilder = new StringBuilder()
    
    var bitValue : String = inputStr.substring(0, numZero)
    
    var i : Integer = 0
       
    if(bitValue.matches("^[0-0]+$")){  
      bitcoin.append(randomStr).append("\t").append(inputStr)
      allBitcoin(0) = allBitcoin(0) + "\n" + bitcoin.toString()
      //println(bitcoin.toString())
      numBitcoin = numBitcoin+1;
     }    
  }   
   
  def calcHash(s: String): String = { 
       hashval.digest(s.getBytes) 
       .foldLeft("")((s: String, b: Byte) => 
         s + Character.forDigit((b & 0xf0) >> 4, 16) 
         + Character.forDigit(b & 0x0f, 16)) }
  
  def getMaxBitcoin(s: String, coin:String) : Unit ={
	
    val regexMatch = "^0+".r
    
    var appendStr = coin +"\t"+s
    
    var res:String = null
    
    var matchString:String = regexMatch.findAllIn(s).matchData.toList.toString()
    
    res = matchString.substring(5, matchString.length()-1)
    
    var resLen = res.length()
    
    //println("print string " + s);
    //println("print match " + res);
    
    if(resLen != 0){
      if(resLen>Integer.parseInt(maxZeroBitcoin(0))){        
        maxZeroBitcoin(0) = resLen.toString()
        maxZeroBitcoin(1) = appendStr        
      }
      else if(resLen == Integer.parseInt(maxZeroBitcoin(0)) && Integer.parseInt(maxZeroBitcoin(0)) !=0){
          maxZeroBitcoin(1) = maxZeroBitcoin(1) + "\n" + appendStr      
      }
    }
       // println("length " +res.length());
    //println("max " + maxZeroBitcoin(1));
    
   // return maxZeroBitcoin
  }
  
  def receive = {
    case CalcSHA(value1, value2) =>    
      
      actorId = value1
      
      var dig_time : Long = 0
    // while (dig_time < 600 ) {
      //while (dig_time < 300000 ) {
	  //while (dig_time < 60000 ) {
	  while (dig_time < 15000 ) {
	  
    	  	val t1 = System.currentTimeMillis
	        var coin:String = getRandom(value1)
	        var hashVal:String = calcHash(coin)
	        getMaxBitcoin(hashVal, coin)
		  	//var coin:String = "adobra;kjsdfk11"
    		checkBitcoin(hashVal, coin, value2)
    		
    		val t2 = System.currentTimeMillis
    		dig_time = dig_time+(t2-t1)
	  }
      //send to master back
      sender ! bitcoinCount(actorId, numBitcoin, maxZeroBitcoin, allBitcoin)

    case _ => log.info("invalid SHA generator")
    	context.system.shutdown()
  }  
}

class Server_Proxy extends Actor {
  def receive = {
    
    case "Start" => {
      val actorName = self.path.name
      println("Server UP & READY " + "\n Server Running now ...." + " \n Actor Name : " + actorName)
    }
	 case argMessage(k1) => {
    //case Message1(k1,ip) => {
    println("SIGNAL RECEIVED FROM CLIENT")
	println ("Please wait for 1 minute(s)...BIT COIN digging in process........")
    val system = ActorSystem("calculateSHABitcoins")
    val master = system.actorOf(Props[masterActor], name = "master")
 
     master ! Start(k1)
  
    }
    
    //case "hello" => println ("RETURN SIGNAL FROM CLIENT")
    //case result : String => {
	
	case resultFromClient(totalCount,allBitcoinArr,highestZero,highestZeroCoinArr)=> {
      
      println("*******************   RESULTS FROM CLIENT      *******************")
	  
		println("Total # of BITCOINS found  : "+totalCount); 
        println("INPUT STRING                        BIT COINS " +allBitcoinArr(0));
		println ("=============================================================")
        println("Max # of leading Zeroes from mined Bit Coins is :"+highestZero); 
        println("Bitcoins with highest number of zeros is (coin-hashValue pair separated by new line) : "+"\n"+highestZeroCoinArr(0)); 
	  
      context.system.shutdown()
      
    }
    case _ => println("Server Failed to start !!")
  }

}

object Server_Sanchita_Sritapa extends App {
 val system = ActorSystem("BitCoinDigger", ConfigFactory.parseString("""
  akka {
  # loglevel = "DEBUG"
  actor {
     provider = "akka.remote.RemoteActorRefProvider"
  }
  remote {
     enabled-transports = ["akka.remote.netty.tcp"]
     netty.tcp {
         hostname = "lin116-01.cise.ufl.edu"
         port = 1988
     }
     log-sent-messages = off
     log-received-messages = off
  }
}
  
  """))  
  
  println("Attempting to start Server ...")
  
  val Server_Proxy = system.actorOf(Props[Server_Proxy], name = "Server_Proxy")
  Server_Proxy ! "Start"
  //println(Server_Proxy.path)
  
}





