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

  def getCombis(): Future[List[Tuple2[String, String]]] = {
    var list = List[Tuple2[String, String]]()
    Await.result(all, 2. seconds).foreach {combi =>
      list = list ::: List(combi.id.toString -> combi.name)
    }
    Future(list)
  }

  def getCombiMap(): Future[Map[Int, String]] = {
    var map = Map[Int, String]()
    Await.result(getCombis, 2.seconds).foreach { combi => map = map + (combi._1.toInt -> combi._2) }
    Future(map)
  }

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

  private[models] class CombiCostsTable(tag: Tag) extends Table[CombiCost](tag, "combis_cost") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def houseId = column[Int]("house_id")
    def combiId = column[Int]("combi_id")
    def credits = column[Int]("credits")
    def * = (id, houseId, combiId, credits) <> (CombiCost.tupled, CombiCost.unapply)
  }  
  
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

  def findById(id: Int): Future[Option[CombiCost]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[CombiCost]] = CombiCosts.filter(_.id === id).result.headOption
}

case class CombiFighter(id: Int, fighterId: Int, combiId: Int)

case class ArmedCombi(combi: Combi, subweapons: List[ArmedWeapon], cost: Int)

case class CombiFighterRepo @Inject()
(combiWeaponRepo: CombiWeaponRepo, 
  combiRepo: CombiRepo, 
  weaponRepo: WeaponRepo, 
  combiCostRepo: CombiCostRepo, 
  weaponsTraitsRepo: WeaponsTraitsRepo,
  traitsRepo: TraitsRepo)(protected val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  private[models] val CombiFighters = TableQuery[CombiFightersTable]

  private[models] class CombiFightersTable(tag: Tag) extends Table[CombiFighter](tag, "combi_fighters") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def fighterId = column[Int]("fighter_id")
    def combiId = column[Int]("combi_id")
    def * = (id, fighterId, combiId) <> (CombiFighter.tupled, CombiFighter.unapply)
  }

  def create(combiFighter: CombiFighter): Future[Int] = db.run(_create(combiFighter))

  private def _create(combiFighter: CombiFighter): DBIO[Int] = CombiFighters returning CombiFighters.map(_.id) += combiFighter

  def findById(id: Int): Future[Option[CombiFighter]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[CombiFighter]] = CombiFighters.filter(_.id === id).result.headOption

  def deleteById(id: Int): Future[Int] = db.run(_deleteById(id))

  private def _deleteById(id: Int) = CombiFighters.filter(_.id === id).delete

  def findByFighterId(id: Int): Future[List[CombiFighter]] = db.run(_findByFighterId(id))

  private def _findByFighterId(id: Int): DBIO[List[CombiFighter]] = CombiFighters.filter(_.fighterId === id).to[List].result

  def getCombisArmed(combis: List[CombiFighter], houseId: Int): Map[Int, ArmedCombi] = {
    var map = Map[Int, ArmedCombi]()
    combis.foreach { combiFighter =>
      val combi = Await.result(combiRepo.findById(combiFighter.combiId), 2.seconds).get 
      val combiWeapons = Await.result(combiWeaponRepo.findByCombiId(combi.id), 2.seconds)
      var list = List[ArmedWeapon]()
      val cost = Await.result(combiCostRepo.getCost(combi.id, houseId), 2.seconds).getOrElse(0)
      combiWeapons.foreach { combiWeapon =>
        val weapon = Await.result(weaponRepo.findById(combiWeapon.weaponId), 2.seconds).get
        val traits = weaponRepo.getWeaponTraits(weapon.id).values.toList
        list = list ::: List(ArmedWeapon(weapon, 0, traits))
      }
      map = map + (combiFighter.id -> ArmedCombi(combi, list, cost))
    }
    map
  }
}