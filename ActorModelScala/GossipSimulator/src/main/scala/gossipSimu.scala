package gossip

import akka.actor.{ActorSystem, ActorLogging, Actor, Props, ActorRef}

import java.security.MessageDigest
import scala.util.Random
import akka.routing.RoundRobinPool
import akka.routing.RoundRobinRouter
import Array._

import scala.math._
import scala.concurrent.duration._

import scala.collection.mutable.ArrayBuffer

//case class gossipFull(topology : String, allWorkers:List[ActorRef], name : String, position : Integer)
//case class gossip(allWorkers:List[ActorRef], name : String, neighList : Array[Array[Int]], countArr : Array[Array[Int]])
case class gossip(allWorkers:List[ActorRef], name : String, neighList : Array[Array[Int]], countArr : Array[Array[Int]],time : Double)
//case class pushSum(allWorkers:List[ActorRef],position : Integer,global_mat: Array[Array[Int]],sumArr: Array[Array[Double]],sum : Double , weight :Double)
case class pushSum(allWorkers:List[ActorRef],position : Integer,global_mat: Array[Array[Int]],sumArr: Array[Array[Double]],sum : Double , weight :Double, time : Double)

case class time(t1 : Double , t2:Double)
//case class time(t1 : Double)

object gossipTrial {
  
  def main(args: Array[String]): Unit = {
    if(args.length == 0 || args.length != 3){
      println("Arguments should be of form : numNodes topology algorithm")
      System.exit(0);
    }   
    val t2 = 0
    
    val numNodes:Integer = Integer.parseInt(args(0))
    val topology:String = args(1)
    val algo:String = args(2)   
    //var position:Array[Integer] = new Array[Integer](numNodes)
    println("============")
    println("Number of Nodes :" + numNodes )
         
    val system = ActorSystem("GossipSimulator")
    var allWorkers:List[ActorRef] = Nil
    
    	1 to numNodes foreach {
    		i => allWorkers ::= system.actorOf(Props[workerActor])
       }   
    
    //val timer = system.actorOf(Props[workerActor1])
    
    
    var sum : Double = 0
    var weight : Double = 0
    
    var global_mat = ofDim[Int](numNodes,numNodes)
    
    var countArr = ofDim[Int](numNodes,2)
    
    var sumArr = ofDim[Double](numNodes,5)
    
    for(i<-0 to numNodes - 1){
        countArr(i)(0) = i
        countArr(i)(1) = 0
        
        sumArr(i)(0) = i
        sumArr(i)(1) = i  // initialised to i as Sum
        sumArr(i)(2) = 1  // weight initialised to 1
        sumArr(i)(3) = 0  // ratio
        sumArr(i)(4) = 0
        
    }
       
    if(args(1).equalsIgnoreCase("full")){
      TopoLine(numNodes)
      println ("=============")
   println("TOPOLOGY : FULL NETWORK")
   println ("=============")
      if(args(2).equalsIgnoreCase("pushsum")){
        val t1 = System.currentTimeMillis
        // timer ! time(t1,0)
        //timer ! time(t1)
         println("ALGO : -> PUSH-SUM ")
         println ("=============")
         //allWorkers(0) ! pushSum(allWorkers,0,global_mat, sumArr,sum,weight)
         
         allWorkers(0) ! pushSum(allWorkers,0,global_mat, sumArr,sum,weight,t1)
       }        
       if(args(2).equalsIgnoreCase("gossip")){
         val t1 = System.currentTimeMillis
          println("ALGO : -> GOSSIP ")
          println ("=============")
         allWorkers(0) ! gossip(allWorkers, null, global_mat, countArr,t1)
       }
    } 
 
    if(args(1).equalsIgnoreCase("line")){      
      TopoFull(numNodes)
      println ("=============")
   println("TOPOLOGY : LINE NETWORK")
   println ("=============")
      if(args(2).equalsIgnoreCase("pushsum")){ 
        val t1 = System.currentTimeMillis
         println("ALGO : -> PUSH-SUM ")
         println ("=============")
         allWorkers(0) ! pushSum(allWorkers,0,global_mat, sumArr ,sum,weight,t1)
       }        
       if(args(2).equalsIgnoreCase("gossip")){
         val t1 = System.currentTimeMillis
          println("ALGO : -> GOSSIP ")
          println ("=============")
         allWorkers(0) ! gossip(allWorkers, null, global_mat, countArr,t1)
       }
      
    }
    
    if(args(1).equalsIgnoreCase("2d")){
      Topo2D(numNodes)
      if(args(2).equalsIgnoreCase("pushsum")){
        val t1 = System.currentTimeMillis
         println("ALGO : -> PUSH-SUM ")
         println ("=============")
         allWorkers(0) ! pushSum(allWorkers,0,global_mat, sumArr , sum , weight,t1)
       }        
       if(args(2).equalsIgnoreCase("gossip")){
         val t1 = System.currentTimeMillis
          println("ALGO : -> GOSSIP ")
          println ("=============")
         allWorkers(0) ! gossip(allWorkers, null, global_mat, countArr,t1)
       }      
    }
    
    if(args(1).equalsIgnoreCase("imp2d")){
      ImpTopo2D(numNodes)
      if(args(2).equalsIgnoreCase("pushsum")){
        val t1 = System.currentTimeMillis
         println("ALGO : -> PUSH-SUM ")
         println ("=============")
         allWorkers(0) ! pushSum(allWorkers,0,global_mat, sumArr , sum , weight ,t1)
       }        
       if(args(2).equalsIgnoreCase("gossip")){
         val t1 = System.currentTimeMillis
          println("ALGO : -> GOSSIP ")
          println ("=============")
         allWorkers(0) ! gossip(allWorkers, null, global_mat, countArr,t1)
       }
      
    }
      
    def Topo2D(numNodes:Integer) : Unit = {
      //TODO      
      
      var n = numNodes
      
   println ("=============")
   println("  TOPOLOGY : PERFECT 2D  ")
   println ("=============")
     
   //var matrix_2D = ofDim[Int](n,n)
   var no_connect : Array[Int] = new Array[Int](n);
                        
                            var flag = 1;
                            var i =0
                            for(i<-0 to n - 1){
                                    no_connect(i) = 0;
                            }
                        
                        var grid_net:Double = math.sqrt(n.toDouble)
                        while(i<n){
                                if((i+1)%grid_net == 0){
                                        if(i+grid_net.toInt < n){
                                                global_mat(i)(i+grid_net.toInt) = 1;
                                                global_mat(i+grid_net.toInt)(i) = 1;
                                        } 
                                        
                                } else {
                                  //println("indexxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"+i)
                                        global_mat(i)(i+1) = 1;
                                        global_mat(i+1)(i) = 1;
                                        if(i+grid_net.toInt < n) {
                                                global_mat(i)(i+grid_net.toInt) = 1;
                                                global_mat(i+grid_net.toInt)(i) = 1;
                                        }
                                }
                                i=i+1
                        }
    }
    
    def ImpTopo2D(numNodes:Integer) : Unit = {
      //TODO      
   println ("=============")   
   println("TOPOLOGY : IMPERFECT 2D")
   println ("=============")
   
   var n = numNodes
   var Skip : Array[Int] = new Array[Int](n);
                        
                            var flag = 1;
                            var i =0
                            for(i<-0 to n - 1){
                                    Skip(i) = 0;
                            }
                        
                        var grid:Double = math.sqrt(n.toDouble)
                        while(i<n){
                                if((i+1)%grid == 0){
                                        if(i+grid.toInt < n){
                                                global_mat(i)(i+grid.toInt) = 1;
                                                global_mat(i+grid.toInt)(i) = 1;
                                        }
                                } else {
                                        global_mat(i)(i+1) = 1;
                                        global_mat(i+1)(i) = 1;
                                        if(i+grid.toInt < n) {
                                                global_mat(i)(i+grid.toInt) = 1;
                                                global_mat(i+grid.toInt)(i) = 1;
                                        }
                                }
                                
                                
                                if(flag == 1) {
                                        var counter:Int = 0
                                        var stop:Int = 0
                                        if (Skip(i) == 0) {
                                                var r = new scala.util.Random
                                                val range = 0 to (n-1);
                                                var init_rand = r.nextInt(range.length);
                                                while((global_mat(i)(init_rand) == 1 || Skip(init_rand) == 1 || i==init_rand) && stop == 0 ) {
                                                        init_rand = r.nextInt(n);
                                                        counter +=1;
                                                        if (counter > n){
                                                                stop = 1;
                                                                init_rand = i;
                                                        }
                                                }
                                       
                                        global_mat(i)(init_rand) = 1;
                                        global_mat(init_rand)(i) = 1;
                                        Skip(init_rand) = 1;
                                        Skip(i) = 1;

                                        }
                                }
                                i = i + 1;
                        }
                     
    }
   
  
  def TopoFull(num_nodes: Int): Unit = {
    
   var n = num_nodes
   
   for (i <- 0 to n-1) {
         for ( j <- 0 to n-1) {
        	 if (i==j)
        	  global_mat(i)(j) = 0
        	 else 
              global_mat(i)(j) = 1
         }
      }
   
 }

    
    def TopoLine(numNodes:Integer) : Unit = { 
   var n = numNodes
   for (i <- 0 to n-1) {
         for ( j <- 0 to n-1) {
            if (j==i+1)
              global_mat(i)(j) = 1
             // print(global_mat(i)(j)+"\t")
         }
      }
   
  
    }
     
  }
    
}

class workerActor extends Actor with ActorLogging{
  
	
    def receive = {      
            
       case gossip(allWorkers, name, neighList, countArr,time) => 
         import context.dispatcher      
               
        var neigh =new ArrayBuffer[Int]()
        var position = 0
        
        for (i <- 0 to allWorkers.length-1) {  
        
          if(self == allWorkers(i)){
            position = i
          }          
        }

        for (i <- 0 to allWorkers.length-1) {  
        
          if(neighList(position)(i) == 1){
            neigh+=i
          }          
        }

        var r = new scala.util.Random
        var range = 0 to (allWorkers.length-1);
       
        var neighRange = 0 to (neigh.length-1)
        var randLoop = 0
        
        if(neigh.length == 0){
          randLoop = 1
        }
        else {

         randLoop = r.nextInt(neighRange.length) 
        }        
        

        
        countArr(position)(1)= countArr(position)(1)+1

        if(countArr(position)(1)!=10){        	
         // println("Gossip selected.\n" + self.path.name + " listening to rumour " +countArr(position)(1) +" times\n")

                   
          for(i <- 0 to randLoop){
             var cancellable = context.system.scheduler.schedule(0 milliseconds,10 milliseconds,
    		   allWorkers(if(neigh.length == 0){0}
            else{neigh(r.nextInt(neighRange.length))}), 
    		   gossip(allWorkers, self.path.name, neighList, countArr,time))

             cancellable.cancel()
          }          
        }     
        else{
        
          var flag:Int = 0
          
          for(i <-0 to countArr.length-1){
            

            if(countArr(i)(1) < 10){
                  flag = 1
            }
          }

          if(flag == 0){    
            val t2 = System.currentTimeMillis
            println("Mission Accomplished! shutting down systems")
            //log.info("Mission Accomplished! shutting down systems")
            println("TIME to converge  " + (t2-time)/1000 + "s")
            context.system.shutdown()
          }     
          else{
            for(i <- 0 to randLoop){
            var cancellable = context.system.scheduler.schedule(0 milliseconds,50 milliseconds,
    		   allWorkers(if(neigh.length == 0){0}
            else{neigh(r.nextInt(neighRange.length))}), 
    		   gossip(allWorkers, self.path.name, neighList, countArr,time))
 
             //This cancels further Ticks to be sent
             cancellable.cancel()
            }
            //println("actor " + position + " listened "+countArr(position)(1)+" times\nstopping actor "+self.path.name);
            println("Actor  " + position + "  listened  " + countArr(position)(1)+ "times..shutting down"  )
            context.stop(self)
          }      
        }  
        
      //case pushSum(allWorkers,position,global_mat,sumArr,sum,weight) => 
        case pushSum(allWorkers,position,global_mat,sumArr,sum,weight,time) => 

        var prev_ratio :Double = 0.0
        var new_ratio :Double = 0.0
        var half_sum :Double = 0.0
        var half_weight :Double = 0.0
        
      // var threshhold = scala.math.pow(10,-10)
       var threshhold = scala.math.pow(10,-2)
        var rand_neighbour = 0
        
        var conn_list =new ArrayBuffer[Int]()
        for (i <- 0 to allWorkers.length-1) {  
         
          if(global_mat(position)(i) == 1){
            conn_list+=i
          
          }          
        }        
                
        var r = new scala.util.Random

        var neighRange = 0 to (conn_list.length-1)
        var randLoop = 0
        
       if (sumArr(position)(4)!=3){
        
        prev_ratio =  sumArr(position)(3)
         
        sumArr(position)(1) = sumArr(position)(1)+ sum/2
        sumArr(position)(2) = sumArr(position)(2)+ weight/2
        sumArr(position)(3) = sumArr(position)(1)/sumArr(position)(2)
        

        
        new_ratio = sumArr(position)(3)

        
        if (abs((prev_ratio-new_ratio))<threshhold) {
          sumArr(position)(4) = sumArr(position)(4)+ 1          
        }
                
        half_sum = sumArr(position)(1) /2
        half_weight = sumArr(position)(2)/2
 
        if(conn_list.length == 0){
            randLoop = 1
          } 
        else        
        	randLoop = r.nextInt(neighRange.length)          
        
        for(i <-0 to randLoop){  
          
        if(conn_list.length == 0){
            rand_neighbour = 0
          } 
        else
        	rand_neighbour = conn_list(r.nextInt(neighRange.length))
        
        allWorkers(rand_neighbour) ! pushSum(allWorkers,rand_neighbour,global_mat, sumArr,half_sum,half_weight,time)

        }  
         // EOF if loop threshold
        
       } // EOF master if
       else{
        
       context.stop(self)
       var flag = 0
       for(i<-0 to sumArr.length-1){
         if(sumArr(position)(4) != 3){
           flag = 1
         }
       }
          if(flag ==0){
            context.system.shutdown()
            val t2 = System.currentTimeMillis
            var time1 = t2-time
              //to delete
            for(i<-0 to sumArr.length-1){
            	println("Node :"+i+" ; Convergence Ratio  -> "+sumArr(i)(3))
            }            

            println("======= PUSH-SUM converged! Mission accomplished. shutting down system =============") 
            println ("TIME to converge :" + time1/1000 + "s")
      
          }    
       
       }       
      
        
        
      case _ => log.info("invalid gossip!")
    	context.system.shutdown()

    }  
}


