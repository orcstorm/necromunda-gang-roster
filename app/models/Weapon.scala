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

case class Weapon(
	id: Int, weaponType: String, name: String, variant: Option[String],
	rangeShort: Option[String], rangeLong: Option[String], accuracyShort: Option[String], accuracyLong: Option[String],
	strength: Option[String], armorPenetration: Option[String], damage: Option[String], ammo: Option[String]
)

case class WeaponWithTraits(weapon: Weapon, traits: Map[Int, String])

class WeaponRepo @Inject()(
  traitsRepo: TraitsRepo,
  weaponsTraitsRepo: WeaponsTraitsRepo)
  (protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val Weapons = TableQuery[WeaponsTable]

  def all: Future[List[Weapon]] = db.run(Weapons.sortBy(_.weaponType.asc).to[List].result)

  def findById(weaponId: Int): Future[Option[Weapon]] = db.run(_findById(weaponId))

  private def _findById(weaponId: Int): DBIO[Option[Weapon]] = Weapons.filter(_.id === weaponId).result.headOption

  private[models] class WeaponsTable(tag: Tag) extends Table[Weapon](tag, "weapons") {

    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    
    def weaponType = column[String]("weapon_type")
    def name = column[String]("name")
    def variant = column[Option[String]]("variant")
    def rangeShort = column[Option[String]]("rng_short")
    def rangeLong = column[Option[String]]("rng_long")
    def accuracyShort = column[Option[String]]("accShort")
    def accuracyLong = column[Option[String]]("accLong")
    def strength = column[Option[String]]("strength")
    def armorPenetration = column[Option[String]]("armor_pen")
    def damage = column[Option[String]]("damage")
    def ammo = column[Option[String]]("ammo")
    def * = (id, weaponType, name, variant, rangeShort, rangeLong, accuracyShort, accuracyLong, strength, armorPenetration, damage, ammo) <> (Weapon.tupled, Weapon.unapply)

  }

  //common functions
  def getWeapons(weapons: List[Weapon]): List[Tuple2[String, String]] = {
    var list = List[Tuple2[String, String]]()
    weapons.foreach { weapon =>
      weapon.variant match {
        case Some(v) => list = list ::: List(weapon.id.toString() -> ("[" + weapon.weaponType + "] " + weapon.name + " --" + v))
        case None => list = list ::: List(weapon.id.toString() -> ("[" + weapon.weaponType + "] " + weapon.name ))
      }
    }
    list
  }

  def getWeapons(): List[Tuple2[String, String]] = {
    getWeapons(Await.result(all, 2.seconds))
  }

  def getWeaponTraits(weaponId: Int): Map[Int, String] = {
    var map = Map[Int, String]()
    Await.result(weaponsTraitsRepo.getTraitIds(weaponId), 2.seconds).foreach { i =>
      val wTrait = Await.result(traitsRepo.findById(i._2), 2.seconds).get
      map = map + (i._1 -> wTrait.name)
    }
    map
  }

} 

case class WeaponTrait(id: Int, name: String, description: String)

class TraitsRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val Traits = TableQuery[TraitsTable]

  private[models] class TraitsTable(tag: Tag) extends Table[WeaponTrait](tag, "weapon_traits") {

    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("weapon_trait")
    def description = column[String]("description")
    def * = (id, name, description) <> (WeaponTrait.tupled, WeaponTrait.unapply)
  }

  def all: Future[List[WeaponTrait]] = db.run(_all)

  private def _all: DBIO[List[WeaponTrait]] = Traits.to[List].result

  def create(weaponTrait: WeaponTrait): Future[Int] = db.run(_create(weaponTrait))

  private def _create(weaponTrait: WeaponTrait): DBIO[Int] = Traits returning Traits.map(_.id) += weaponTrait

  def findById(id: Int): Future[Option[WeaponTrait]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[WeaponTrait]] = Traits.filter(_.id === id).result.headOption

  def deleteById(id: Int): Future[Int] = db.run(_deleteById(id))

  private def _deleteById(id: Int): DBIO[Int] = Traits.filter(_.id === id).delete
  
  def getTraitList(traits: List[WeaponTrait]): List[Tuple2[String, String]] = {
    var list = List[Tuple2[String, String]]()
    traits.foreach { t =>
      list = list ::: List(t.id.toString() -> t.name)
    }
    list
  } 
}

case class WeaponsTraits(id: Int, weaponId: Int, traitId: Int)

case class WeaponsTraitsRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val WeaponsTraitsJoin = TableQuery[WeaponsTraitsTable]

  def getTraitIds(weaponId: Int): Future[List[Tuple2[Int, Int]]] = 
    db.run(WeaponsTraitsJoin.filter(_.weaponId === weaponId).map(u => (u.id -> u.traitId)).to[List].result)

  def create(wt: WeaponsTraits): Future[Int] = { db.run(WeaponsTraitsJoin returning WeaponsTraitsJoin.map(_.id) += wt) }

  def findById(weaponTraitId: Int): Future[Option[WeaponsTraits]] = db.run(_findById(weaponTraitId))

  private def _findById(id: Int): DBIO[Option[WeaponsTraits]] = WeaponsTraitsJoin.filter(_.id === id).result.headOption

  def deleteById(weaponTraitId: Int): Future[Int] = { db.run(_deleteById(weaponTraitId)) }

  private def _deleteById(id: Int): DBIO[Int] = WeaponsTraitsJoin.filter(_.id === id).delete

  private[models] class WeaponsTraitsTable(tag: Tag) extends Table[WeaponsTraits](tag, "weapons_traits") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def weaponId = column[Int]("weapon_id")
    def traitId = column[Int]("trait_id")
    def * = (id, weaponId, traitId) <> (WeaponsTraits.tupled, WeaponsTraits.unapply)
  }
}

case class WeaponCost(id: Int, houseId: Int, weaponId: Int, credits: Int)

case class WeaponCostRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  def create(weaponCost: WeaponCost): Future[Int] = db.run(_create(weaponCost))

  private def _create(weaponCost: WeaponCost): DBIO[Int] = WeaponCosts returning WeaponCosts.map(_.id) += weaponCost

  def deleteById(id: Int): Future[Int] = { db.run(_deleteById(id)) }

  private def _deleteById(id: Int): DBIO[Int] = { WeaponCosts.filter(_.id === id).delete }

  private[models] val WeaponCosts = TableQuery[WeaponCostsTable]

  def getCosts(weaponId: Int): Future[List[WeaponCost]] = db.run(_getCosts(weaponId))

  private def _getCosts(weaponId: Int): DBIO[List[WeaponCost]] = WeaponCosts.filter(_.weaponId === weaponId).to[List].result

  def getCost(weaponId: Int, houseId: Int): Future[Option[Int]] = db.run(_getCost(weaponId, houseId))

  private def _getCost(weaponId: Int, houseId: Int): DBIO[Option[Int]] = {
    WeaponCosts.filter(_.weaponId === weaponId).filter(_.houseId === houseId).map(w => (w.credits)).result.headOption
  }

  private[models] class WeaponCostsTable(tag: Tag) extends Table[WeaponCost](tag, "weapons_cost") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def houseId = column[Int]("house_id")
    def weaponId = column[Int]("weapon_id")
    def credits = column[Int]("credits")
    def * = (id, houseId, weaponId, credits) <> (WeaponCost.tupled, WeaponCost.unapply)
  }   
}

