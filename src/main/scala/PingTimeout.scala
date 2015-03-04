import akka.actor.ActorSystem
import akka.http.Http
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import scala.concurrent.blocking
import spray.json.DefaultJsonProtocol

case class Ping(timeout: Long)

case object Ping extends DefaultJsonProtocol {
  implicit val pingFormat = jsonFormat1(Ping.apply)
}

object PingTimeout extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorFlowMaterializer()
  val config = ConfigFactory.load()

  val routes =

  Http().bind(interface = config.getString("http.interface"), port = config.getInt("http.port")).startHandlingWith {
    logRequestResult("akka-http-microservice") {
      path("healthcheck") {
        complete(OK)
      } ~
      pathPrefix("ping") {
        entity(as[Ping]) { ping =>
          path("blocking") {
            complete {
              Thread.sleep(ping.timeout)
              OK
            }
          } ~
          path("async") {
            complete {
              blocking(Thread.sleep(ping.timeout))
              OK
            }
          }
        }
      } ~
      path("error") {
        complete(InternalServerError)
      }
    }
  }
}
