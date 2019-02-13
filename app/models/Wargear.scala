package models

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio
import slick.dbio.Effect.Read
import slick.jdbc.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.{ DurationInt, Duration }
import scala.concurrent.Future

case class Wargear(id: Int, name: String, description: String)

case class FighterWargear(id: Int, fighterId: Int, wargearId: Int)

class WargearRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
   
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val Wargears = TableQuery[WargearsTable]

  private[models] class WargearsTable(tag: Tag) extends Table[Wargear](tag, "wargear") {
  	def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  	def name = column[String]("name")
  	def description = column[String]("description")
  	def * = (id, name, description) <> (Wargear.tupled, Wargear.unapply)
  }

  def all(): Future[List[Wargear]] = db.run(_all)

  private def _all(): DBIO[List[Wargear]] = Wargears.to[List].result

  def findById(id: Int): Future[Option[Wargear]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[Wargear]] = Wargears.filter(_.id === id).result.headOption

  def create(wargear: Wargear): Future[Int] = db.run(_create(wargear))

  private def _create(wargear: Wargear): DBIO[Int] = Wargears returning Wargears.map(_.id) += wargear

  def deleteById(id: Int): Future[Int] = db.run(_deleteById(id))

  private def _deleteById(id: Int): DBIO[Int] = Wargears.filter(_.id === id).delete

  def update(wargear: Wargear): Future[Int] = db.run(_update(wargear))

  private def _update(wargear: Wargear): DBIO[Int] = Wargears.filter(_.id === wargear.id).update(wargear)

  def getWargear(): List[Tuple2[String, String]] = {
    var list = List[Tuple2[String, String]]()
    Await.result(all, 2. seconds).foreach {wg =>
      list = list ::: List(wg.id.toString -> wg.name)
    }
    list
  }

  def getWargearMap(): Map[Int, String] = {
    var map = Map[Int, String]()
    getWargear.foreach { wg => map = map + (wg._1.toInt -> wg._2) }
    map
  }

}

class FighterWargearRepo @Inject()(wargearRepo: WargearRepo)(protected val dbConfigProvider: DatabaseConfigProvider) {
   
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val FighterWargears = TableQuery[FighterWargearsTable]

  private [models] class FighterWargearsTable(tag: Tag) extends Table[FighterWargear](tag, "fighter_wargear") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def fighterId = column[Int]("fighter_id")
    def wargearId = column[Int]("wargear_id")
    def * = (id, fighterId, wargearId) <> (FighterWargear.tupled, FighterWargear.unapply)
  }

  def all(): Future[List[FighterWargear]] = db.run(_all)

  private def _all(): DBIO[List[FighterWargear]] = FighterWargears.to[List].result

  def create(fighterWargear: FighterWargear): Future[Int] = db.run(_create(fighterWargear))

  private def _create(fighterWargear: FighterWargear): DBIO[Int] = FighterWargears returning FighterWargears.map(_.id) += fighterWargear

  def findById(id: Int): Future[Option[FighterWargear]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[FighterWargear]] = FighterWargears.filter(_.id === id).result.headOption

  def findByFighterId(id: Int): Future[List[FighterWargear]] = db.run(_findByFighterId(id))

  private def _findByFighterId(id: Int): DBIO[List[FighterWargear]] = FighterWargears.filter(_.fighterId === id).to[List].result

  def deleteById(id: Int): Future[Int] = db.run(_deleteById(id))

  private def _deleteById(id: Int): DBIO[Int] = FighterWargears.filter(_.id === id).delete

  def getFighterGearList(fighterId: Int): List[String] = {
    var list = List[String]()

    Await.result(findByFighterId(fighterId), 2.seconds).foreach { fg =>
      list = list ::: List(Await.result(wargearRepo.findById(fg.wargearId), 2.seconds).get.name)
    }

    list
  }

}

case class WargearCost(id: Int, houseId: Int, gearId: Int, credits: Int)

case class WargearCostRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  private[models] val WargearCosts = TableQuery[WargearCostsTable]

  private[models] class WargearCostsTable(tag: Tag) extends Table[WargearCost](tag, "wargear_cost") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def houseId = column[Int]("house_id")
    def wargearId = column[Int]("wargear_id")
    def credits = column[Int]("credits")
    def * = (id, houseId, wargearId, credits) <> (WargearCost.tupled, WargearCost.unapply)
  }  

  def create(wargearCost: WargearCost): Future[Int] = db.run(_create(wargearCost))

  private def _create(wargearCost: WargearCost): DBIO[Int] = WargearCosts returning WargearCosts.map(_.id) += wargearCost

  def findById(id: Int): Future[Option[WargearCost]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[WargearCost]] = WargearCosts.filter(_.id === id).result.headOption

  def deleteById(id: Int): Future[Int] = { db.run(_deleteById(id)) }

  private def _deleteById(id: Int): DBIO[Int] = { WargearCosts.filter(_.id === id).delete }
  
  def findByWargearId(wargearId: Int): Future[List[WargearCost]] = db.run(_findByWargearId(wargearId))

  private def _findByWargearId(wargearId: Int): DBIO[List[WargearCost]] = WargearCosts.filter(_.wargearId === wargearId).to[List].result

  def getCost(wargearId: Int, houseId: Int): Future[Option[Int]] = db.run(_getCost(wargearId, houseId))

  private def _getCost(wargearId: Int, houseId: Int): DBIO[Option[Int]] = {
    WargearCosts.filter(_.wargearId === wargearId).filter(_.houseId === houseId).map(w => (w.credits)).result.headOption
  }

  def findByHouseId(id: Int): Future[List[WargearCost]] = db.run(_findByHouseId(id))

  private def _findByHouseId(id: Int): DBIO[List[WargearCost]] = WargearCosts.filter(_.houseId === id).to[List].result

  def getCostMap(houseId: Int): Map[Int, Int] = {
    var map = Map[Int, Int]()
    Await.result(findByHouseId(houseId), 2.seconds).foreach { cost => 
      map = map + (cost.gearId -> cost.credits)
    }
    map
  }

}