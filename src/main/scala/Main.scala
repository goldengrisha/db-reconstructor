import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.{Keep, Sink}
import com.github.mjakubowski84.parquet4s.{ParquetReader, ParquetStreams}
import com.typesafe.config.Config
import models.SummaryEvent
import org.apache.hadoop.conf.{Configuration => HadoopConf}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main extends App {
  implicit val actorSystem: ActorSystem = ActorSystem("db-reconstructor")
  implicit val materializer: Materializer = Materializer(actorSystem)
  implicit val session: SlickSession = SlickSession.forConfig("slick-postgres")
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  import session.profile.api._


  val logger = LoggerFactory.getLogger(getClass.getSimpleName)
  val conf: HadoopConf = new HadoopConf()
  // val bucketKey = "agrdata/year=2021/month=9/day=3/hour=1/"
  val bucketKey: String = "agrdata/year=2021/month=9/"
  val aws_config: Config = actorSystem.settings.config.getConfig("aws")
  val bucket: String = aws_config.getString("bucket")
  val region: String = aws_config.getString("region")

  conf.set("fs.s3a.access.key", aws_config.getString("accesskey"))
  conf.set("fs.s3a.secret.key", aws_config.getString("secretkey"))
  conf.set("fs.s3a.connection.maximum", "1000")
  conf.set("fs.s3a.threads.max", "500")
  conf.set("fs.s3a.endpoint", s"s3.$region.amazonaws.com")

  val slickAggFlow = Slick.flowWithPassThrough(10, summaryMapper)
  ParquetStreams
    .fromParquet[SummaryEvent]
    .withOptions(ParquetReader.Options(hadoopConf = conf))
    .read(
      s"s3a://$bucket/$bucketKey/"
    )
    .watchTermination()((prevMatValue, future) =>
      future.onComplete {
        case Failure(exception) => logger.error(exception.getMessage)
        case Success(_)         => logger.info(s"The stream materialized!")
      }
    )
    .async
    .via(slickAggFlow)
    .toMat(Sink.ignore)(Keep.left)
    .run()

  actorSystem.registerOnTermination(session.close())

  def summaryMapper(k: SummaryEvent) =
    sqlu"""INSERT INTO AGRDATA VALUES(${k.connectionId}, ${k.chatbotId},${k.campaignId},
            ${k.interactionId},${k.theTime},${k.secondsToActive},${k.secondsTotalActive},
            ${k.chatbotActions},${k.clickedLink},${k.timeGoalMet},${k.timeGoalSeconds},
            ${k.reachedEnd},${k.reachedGoal},${k.reachedInput},${k.flowsCompleted})"""
      .map(_ => k)
}
