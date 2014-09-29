import akka.actor.{ActorSystem, ActorLogging, Actor, Props, ActorRef}

import java.security.MessageDigest
import scala.util.Random
import akka.routing.RoundRobinPool
import akka.routing.RoundRobinRouter

case class Start(value: Integer)
case class bitcoinCount(value1: String, value2: Integer, value3:Array[String], value4: Array[String])
case class CalcSHA(value1: String, value2: Integer)

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
        println("Total number of bitcoins is: "+totalCount); 
        println("Bitcoins found for given parameter are : " +allBitcoinStr);
        println("Highest number of Zeros found in bitcoin is : "+highestZero); 
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
  //var maxZeroBitcoin : Map[Int, String] = Map()
  //val maxZeroBitcoin = scala.collection.mutable.Map[Int,String]()
  var maxZeroBitcoin: Array[String] = new Array[String](2)
  maxZeroBitcoin(0) = "0"
  maxZeroBitcoin(1) = null
  
  var allBitcoin: Array[String] = new Array[String](1)
  allBitcoin(0) = ""
  
  def getRandom(SpclChar:String) : String = {
       
       var randomStr:StringBuilder = new StringBuilder()
       
       randomStr.append(UFID).append(SpclChar)
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
      while (dig_time < 300000 ) {
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

object Project1Local{
  
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("calculateSHABitcoins")
    val master = system.actorOf(Props[masterActor], name = "master")
    master ! Start(Integer.parseInt(args(0)))
  }  
}
