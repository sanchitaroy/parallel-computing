package client

import akka.actor._
import akka.actor.ActorDSL._
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
case class Signal_from_Client(k : Integer)
//case class GMaster(k1 : Integer , ip : String)

case class GMaster(k1 : Integer)
case class send_to_server (result : String)
case class ip_address(ip : String)

class masterActor() extends Actor with ActorLogging{
  
  var totalCount: Integer = 0
  var numProcs: Integer = Runtime.getRuntime().availableProcessors() - 1;
  var numResults: Integer = 0
  var highestZero: Int = 0
  var highestZeroCoin: String = null
  var allBitcoinStr : StringBuilder = new StringBuilder()
  var resultClient : StringBuilder = new StringBuilder()
  var allBitcoinArr: Array[String] = new Array[String](1)
  allBitcoinArr(0) = null
  
  var highestZeroCoinArr: Array[String] = new Array[String](1)
  highestZeroCoinArr(0) = null

/*  
  val conf1 = """akka {
  #log-config-on-start = on
  # stdout-loglevel = "DEBUG"
  # loglevel = "DEBUG"
  actor {
      provider = "akka.remote.RemoteActorRefProvider"
  }
  remote {
    enabled-transports = ["akka.remote.netty.tcp"]
    log-sent-messages = off
    log-received-messages = off
    netty.tcp {
          hostname ="""
    val conf2 = """port = 0
    }
  }  
}  """
*  
*  
*/
  
  implicit val system = ActorSystem("BitCoinDigger-1", ConfigFactory.parseString("""
   akka {
  #log-config-on-start = on
  # stdout-loglevel = "DEBUG"
  # loglevel = "DEBUG"
  actor {
      provider = "akka.remote.RemoteActorRefProvider"
  }
  remote {
    enabled-transports = ["akka.remote.netty.tcp"]
    log-sent-messages = off
    log-received-messages = off
    netty.tcp {
          hostname = "127.0.0.1"
          port = 0
    }
  }  
}  
   """))
   
 val server_proxy = system.actorSelection("akka.tcp://BitCoinDigger@lin116-01.cise.ufl.edu:1988/user/Server_Proxy")  
     
  def receive = {    
       
       //case GMaster(k1,ip)=>{
  
    case GMaster(k1)=>{
   //server_proxy ! Message1(k1,ip)
      
   server_proxy ! argMessage(k1)
   
    val roundRobinRouter =
    context.actorOf(Props[workerActor].withRouter(RoundRobinRouter(numProcs)), "router")
  
      1 to numProcs foreach {
    i => roundRobinRouter ! CalcSHA(i.toString(), k1)
    }

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
        
        allBitcoinArr(0) = allBitcoinStr.toString()
        highestZeroCoinArr(0) = highestZeroCoin
        
       // println("*************procs***************"+numResults)
        
        resultClient.append("Total # of BITCOINS found  : ").append(totalCount).append("\n INPUT STRING                        BIT COINS " + "\n=============================================================")
        	.append(allBitcoinStr).append("\n Max # of leading Zeroes from mined Bit Coins is : ").append(highestZero)
        	.append("\n Bitcoins with highest number of zeros is (coin-hashValue pair separated by new line) : \n")
        	.append(highestZeroCoin)
        var result : String = resultClient.toString()
        /*
        server_proxy ! result*/
        
        
        server_proxy ! resultFromClient(totalCount,allBitcoinArr,highestZero,highestZeroCoinArr)
        
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
       
       randomStr.append(UFID).append(SpclChar).append("c")
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
    
    if(resLen != 0){
      if(resLen>Integer.parseInt(maxZeroBitcoin(0))){        
        maxZeroBitcoin(0) = resLen.toString()
        maxZeroBitcoin(1) = appendStr        
      }
      else if(resLen == Integer.parseInt(maxZeroBitcoin(0)) && Integer.parseInt(maxZeroBitcoin(0)) !=0){
          maxZeroBitcoin(1) = maxZeroBitcoin(1) + "\n" + appendStr      
      }
    }
  }
  
  def receive = {
    case CalcSHA(value1, value2) =>    
      
      actorId = value1
      
      var dig_time : Long = 0
     //while (dig_time < 600 ) {
      //while (dig_time < 300000 ) {
	 // while (dig_time < 60000 ) {
       
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
      
     // println("worker says number of bitcoins is in actor " + actorId +" is: "+numBitcoin);
      sender ! bitcoinCount(actorId, numBitcoin, maxZeroBitcoin, allBitcoin)
      

    case _ => log.info("invalid SHA generator")
    	context.system.shutdown()
  }  
}

object Client extends App {

   println("Starting Client...")
   val system = ActorSystem("StartClient")
   val master = system.actorOf(Props[masterActor], name = "master")
   
   //val ip = args(1)

   //master ! GMaster(Integer.parseInt(args(0)),ip)
   
    master ! GMaster(Integer.parseInt(args(0)))
      
}
