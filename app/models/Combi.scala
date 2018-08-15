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

case class Combi(id: Int, name: String)

case class CombiWeapon(id: Int, combiId: Int, weaponId: Int)

class CombiRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val Combis = TableQuery[CombisTable]

  private[models] class CombisTable(tag: Tag) extends Table[Combi](tag, "combis") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def * = (id, name) <> (Combi.tupled, Combi.unapply)
  }

  def all(): Future[List[Combi]] = db.run(_all)

  private def _all = Combis.to[List].result

  def create(combi: Combi): Future[Int] = db.run(_create(combi))

  private def _create(combi: Combi): DBIO[Int] = Combis returning Combis.map(_.id) += combi

  def deleteById(id: Int): Future[Int] = db.run(_deleteById(id))

  private def _deleteById(id: Int): DBIO[Int] = Combis.filter(_.id === id).delete

  def findById(id: Int): Future[Option[Combi]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[Combi]] = Combis.filter(_.id === id).result.headOption

}

class CombiWeaponRepo @Inject()(weaponRepo: WeaponRepo)(protected val dbConfigProvider: DatabaseConfigProvider) {
  
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  private[models] val CombiWeapons = TableQuery[CombiWeaponsTable]

  private[models] class CombiWeaponsTable(tag: Tag) extends Table[CombiWeapon](tag, "combi_weapons") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def combiId = column[Int]("combi_id")
    def weaponId = column[Int]("weapon_id")
    def * = (id, combiId, weaponId) <> (CombiWeapon.tupled, CombiWeapon.unapply)
  }

  def create(combiWeapon: CombiWeapon): Future[Int] = db.run(_create(combiWeapon))

  private def _create(combiWeapon: CombiWeapon): DBIO[Int] = CombiWeapons returning CombiWeapons.map(_.id) += combiWeapon

  def findByCombiId(id: Int): Future[List[CombiWeapon]] = db.run(_findByCombiId(id))

  private def _findByCombiId(id: Int): DBIO[List[CombiWeapon]] = CombiWeapons.to[List].filter(_.combiId === id).result

  def findById(id: Int): Option[CombiWeapon] = Await.result(db.run(_findById(id)), 2.seconds)

  private def _findById(id: Int) = CombiWeapons.filter(_.id === id).result.headOption

  def deleteById(id: Int): Int = Await.result(db.run(_deleteById(id)), 2.seconds)

  private def _deleteById(id: Int) = CombiWeapons.filter(_.id === id).delete

  def getWeaponsForCombi(id: Int): Map[Int, Weapon] = {
    var map = Map[Int, Weapon]()
    Await.result(findByCombiId(id), 2.seconds).foreach { combiWeapon =>
      val weapon = Await.result(weaponRepo.findById(combiWeapon.weaponId), 2.seconds).get
      map = map + (combiWeapon.id -> weapon)
    }
    map
  }
}

case class CombiCost(id: Int, houseId: Int, combiId: Int, credits: Int)

case class CombiCostRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  private[models] val CombiCosts = TableQuery[CombiCostsTable]
  
  def create(combiCost: CombiCost): Future[Int] = db.run(_create(combiCost))

  private def _create(combiCost: CombiCost): DBIO[Int] = CombiCosts returning CombiCosts.map(_.id) += combiCost

  def deleteById(id: Int): Future[Int] = { db.run(_deleteById(id)) }

  private def _deleteById(id: Int): DBIO[Int] = { CombiCosts.filter(_.id === id).delete }

  def getCosts(combiId: Int): Future[List[CombiCost]] = db.run(_getCosts(combiId))

  private def _getCosts(combiId: Int): DBIO[List[CombiCost]] = CombiCosts.filter(_.combiId === combiId).to[List].result

  def getCost(combiId: Int, houseId: Int): Future[Option[Int]] = db.run(_getCost(combiId, houseId))

  private def _getCost(combiId: Int, houseId: Int): DBIO[Option[Int]] = {
    CombiCosts.filter(_.combiId === combiId).filter(_.houseId === houseId).map(c => (c.credits)).result.headOption
  }

  private[models] class CombiCostsTable(tag: Tag) extends Table[CombiCost](tag, "combis_cost") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def houseId = column[Int]("house_id")
    def combiId = column[Int]("combi_id")
    def credits = column[Int]("credits")
    def * = (id, houseId, combiId, credits) <> (CombiCost.tupled, CombiCost.unapply)
  }   
}











