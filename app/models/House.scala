package models

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio
import slick.dbio.Effect.Read
import slick.jdbc.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, DurationInt }
import scala.concurrent.Future

case class House(id: Int, name: String)

class HouseRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
  
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val Houses = TableQuery[HousesTable]

  def all: Future[List[House]] = db.run(Houses.to[List].result)

  def getHouseList: List[Tuple2[String, String]] = {
    val houses = Await.result(all, 2.seconds)
    var list = List[Tuple2[String, String]]()
      houses.foreach { t =>
        list = list ::: List(t.id.toString() -> t.name)
      }
    list
  } 
  

  def getHouseMap(list: List[Tuple2[String, String]]): Map[Int, String] = {
    var map = Map[Int, String]()
    list.foreach { h =>
      map = map + (h._1.toInt -> h._2)
    }
    map
  }

  private def _findById(id: Int): DBIO[Option[House]] = Houses.filter(_.id === id).result.headOption

  def findById(id: Int): Future[Option[House]] = db.run(_findById(id))

  private[models] class HousesTable(tag: Tag) extends Table[House](tag, "houses") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def * = (id, name) <> (House.tupled, House.unapply)
  }

}