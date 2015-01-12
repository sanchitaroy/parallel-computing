import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.remote._
import com.typesafe.config.ConfigFactory

import akka.actor.{ActorSystem, ActorLogging, Actor, Props, ActorRef}

import akka.io.{ IO, Tcp }
import spray.can.Http
import spray.client.pipelining._


import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.util.Timeout
import akka.pattern.ask
import akka.io.IO

import spray.can.Http
import spray.http._
import HttpMethods._


//import spray.httpx.RequestBuilding._
import spray.http._
import HttpMethods._
import HttpHeaders._
import ContentTypes._




object TwitterSimulator_RestAPI_Client extends App{
  
val system1 = ActorSystem("RestAPIClient")

val starter = system1.actorOf(Props[RestAPIClientActor1], "RestAPIClientActor")

var choice = ""

println("Starting REST API CLIENT ...")

println ("============ REST API MENU =========" )
println (" 1. Greetings from Twitter ")
println (" 2. Get total # of Tweets in Simulation Engine ")
println (" 3. Get total # of  Users in Simulation Engine ")
println (" 4. Get my TWEETS ")
println (" 5. Get my Following List ")
println (" 6. Add new users to Simulation Engine ")
println (" 7. Post a tweet ")
println (" 8. I want to follow another user ")
println (" 9. EXIT")
println ("Enter your choice")
choice = readLine()
println ("You entered : " , choice)
starter ! (choice)
}

class RestAPIClientActor1 extends Actor {
  
//Post("/abc", "foobar") === HttpRequest(method = POST, uri = "/abc", entity = "foobar")  
  
implicit val system: ActorSystem = ActorSystem("hello")
implicit val timeout: Timeout = Timeout(15.seconds)  


import system.dispatcher
val pipeline1 = sendReceive


def receive = {

	case ("1") =>{
	println ("Inside Greetings from follower")
	
  (IO(Http) ? HttpRequest(GET, Uri("http://localhost:8000/hello"))).mapTo[HttpResponse] 
	self ! "done"

	}
		
	case ("2") => {
	println ("Inside Total # Tweets")
	(IO(Http) ? HttpRequest(GET, Uri("http://localhost:8000/getTotalTweets"))).mapTo[HttpResponse]
	self ! "done"
	
	}
	
	case ("3") => {
	println ("Inside Total # Users")
	(IO(Http) ? HttpRequest(GET, Uri("http://localhost:8000/getTotalUsers"))).mapTo[HttpResponse]
	
	self ! "done"
	}
	
	case ("4") => {
	println ("Inside GET MY TWEETS")
	println ("Enter User ID")
	var id = readLine()
	(IO(Http) ? HttpRequest(GET, Uri("http://localhost:8000/getMyTweets?id="+id))).mapTo[HttpResponse]
	self ! "done"
	}
	
	case ("5") => {
	println ("Inside GET my following list")
	println ("Enter User ID")
	var id = readLine()
	(IO(Http) ? HttpRequest(GET, Uri("http://localhost:8000/getWhomIFollow?id="+id))).mapTo[HttpResponse]
	
	self ! "done"
	
	}
	
	case ("6") => {
	println ("Inside ADD new users to Simulation engine")
	println ("Enter # of users to add : ")
	var num = readLine()
	(IO(Http) ? HttpRequest(GET, Uri("http://localhost:8000/addUsers?id="+num))).mapTo[HttpResponse]
	self ! "done"
	
	}
	
	case ("7") => {
	println ("Inside POST new tweet to your account")
	println ("Enter User ID")
	var id = readLine()
	println ("Enter your tweet")
	var tweet = readLine()
	tweet.replaceAll(" ", "%20")
	(IO(Http) ? HttpRequest(GET, Uri("http://localhost:8000/postTweet?id="+id+"&tweet="+tweet))).mapTo[HttpResponse]
	
	
	self ! "done"
	
	}
	
	case ("8") => {
	println ("Inside I want to follow anothet user")
	println ("Enter User ID")
	var myId = readLine()
	println ("Enter ID of user you want to follow")
	var UserId = readLine()
	(IO(Http) ? HttpRequest(GET, Uri("http://localhost:8000/IFollowU?myId="+myId+"&UserId="+UserId))).mapTo[HttpResponse]
	
	
	self ! "done"
	
	}
	
	case ("9") =>
	  println ("Thanks for trying out our Twitter API Simulation")
	  context.system.shutdown()

	
	case "done"=>
	  println ("============ REST API MENU =========" )
println (" 1. Greetings from Twitter ")
println (" 2. Get total # of Tweets in Simulation Engine ")
println (" 3. Get total # of  Users in Simulation Engine ")
println (" 4. Get my TWEETS ")
println (" 5. Get my Following List ")
println (" 6. Add new users to Simulation Engine ")
println (" 7. Post a tweet ")
println (" 8. I want to follow another user ")
println (" 9. EXIT")
println ("Enter your choice")
var choice = readLine()
println ("You entered : " , choice)

  self ! (choice)
}
}

