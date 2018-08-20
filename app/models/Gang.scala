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


case class Gang(id: Int, house: Int, name: String)

class GangRepo @Inject()(
  fighterRepo: FighterRepo)
  (protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val Gangs = TableQuery[GangsTable]

  def all: Future[List[Gang]] = db.run(Gangs.to[List].result)

  def findByHouseId(houseId: Int): Future[List[Gang]] = db.run(_findByHouseId(houseId))

  private def _findByHouseId(houseId: Int): DBIO[List[Gang]] = Gangs.filter(_.house === houseId).to[List].result

  def findById(id: Int): Future[Option[Gang]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[Gang]] = Gangs.filter(_.id === id).result.headOption

  def create(gang: Gang): Future[Int] = { db.run(Gangs returning Gangs.map(_.id) += gang) }

  def deleteById(gangId: Int): Future[Int] = { db.run(_deleteById(gangId)) }

  private def _deleteById(gangId: Int): DBIO[Int] = {
    fighterRepo.deleteByGangId(gangId)
    Gangs.filter(_.id === gangId).delete
  }


  private[models] class GangsTable(tag: Tag) extends Table[Gang](tag, "gangs") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def house = column[Int]("house")
    def name = column[String]("name")
    def * = (id, house, name) <> (Gang.tupled, Gang.unapply)
  }

  def getGangCost(fighters: List[FighterSummary]): Int = {
    var cost = 0;
    fighters.foreach { fighter => 
      cost = cost + fighter.cost
    }
    cost
  }

  def getGangCost(gangId: Int, houseId: Int): Int = {
    val fighters = Await.result(fighterRepo.findByGangId(gangId), 2.seconds) 
    val fighterSummaries = fighterRepo.getFighterSummmaries(fighters, houseId)
    getGangCost(fighterSummaries)
  }

}