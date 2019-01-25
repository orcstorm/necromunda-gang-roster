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

case class Fighter(id: Int, gangId: Int, name: String, fighterType: Int)

case class FighterSummary(id: Int, name: String, fighterType: String, weaponsArmed: Map[Int, ArmedWeapon], skills: List[String], wargear: List[String], combis: Map[Int, ArmedCombi], cost: Int)

case class ArmedWeapon(weapon: Weapon, cost: Int, traits: List[String])

class FighterRepo @Inject()(
    profileRepo: FighterProfileRepo,
    fighterWeaponRepo: FighterWeaponRepo,
    fighterWargearRepo: FighterWargearRepo,
    weaponRepo: WeaponRepo,
    weaponCostRepo: WeaponCostRepo, 
    wargearCostRepo: WargearCostRepo,
    fighterSkillRepo: FighterSkillRepo,
    combiFighterRepo: CombiFighterRepo,
    weaponsTrairsRepo: WeaponsTraitsRepo, 
    traitsRepo: TraitsRepo
  )(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val Fighters = TableQuery[FightersTable]

  def all: Future[List[Fighter]] = db.run(Fighters.to[List].result)

  def create(fighter: Fighter): Future[Int] = { db.run(Fighters returning Fighters.map(_.id) += fighter) }

  def findById(id: Int): Future[Option[Fighter]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[Fighter]] = Fighters.filter(_.id === id).result.headOption

  def findByGangId(gangId: Int): Future[List[Fighter]] = db.run(_findByGangID(gangId))

  private def _findByGangID(gangId: Int): DBIO[List[Fighter]] = {
    Fighters.to[List].filter(_.gangId === gangId).sortBy( _.fighterType).result
  }

  def deleteById(id: Int): Future[Int] = { db.run(_deleteById(id)) }

  private def _deleteById(id: Int): DBIO[Int] = Fighters.filter(_.id === id).delete

  private[models] class FightersTable(tag: Tag) extends Table[Fighter](tag, "fighters") {

    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def gangId = column[Int]("gang_id")
    def name = column[String]("name")
    def fighterType = column[Int]("fighter_type")

  	def * = (id, gangId, name, fighterType) <> (Fighter.tupled, Fighter.unapply)
  }

  //common functions

  def getFighterWeapons(fighterId: Int, houseId: Int): Map[Int, ArmedWeapon] = {
    var map = Map[Int, ArmedWeapon]()
    val fighterWeapons = Await.result(fighterWeaponRepo.findByFighterId(fighterId), 2.seconds)
    fighterWeapons.foreach { fighterWeapon => 
      Await.result(weaponRepo.findById(fighterWeapon.weaponId), 2.seconds) match {
        case Some(weapon) => {
          val cost = Await.result(weaponCostRepo.getCost(weapon.id, houseId), 2.seconds).getOrElse(0)
          val traits = weaponRepo.getWeaponTraits(weapon.id).values.toList
          map = map + (fighterWeapon.id -> ArmedWeapon(weapon, cost, traits))
        }
        case None => // log this I guess?
      }
    }
    map
  }

  def getProfileMap(profiles: List[FighterProfile]): List[Tuple2[String, String]] = {
    var list = List[Tuple2[String, String]]()
    profiles.foreach { profile => 
      list = list ::: List(profile.id.toString() -> profile.fighterClass) 
    }
    list
  }

  def getCost(baseCost: Int, weapons: Map[Int, ArmedWeapon], houseId: Int, wargear: List[FighterWargear], combisArmed: Map[Int, ArmedCombi]): Int = {

    var cost = baseCost
    weapons.foreach { w => 
      cost = cost + Await.result(weaponCostRepo.getCost(w._2.weapon.id, houseId), 2.seconds).getOrElse(0)
    }

    wargear.foreach { g =>
      cost = cost + Await.result(wargearCostRepo.getCost(g.wargearId, houseId), 2.seconds).getOrElse(0)
    }

    combisArmed.foreach { c => 
      cost = cost + c._2.cost
    }

    cost
  }

  def getFighterSummmaries(fighters: List[Fighter], houseId: Int): List[FighterSummary] = {
    var list = List[FighterSummary]()
    fighters.foreach { fighter => list = list ::: List(getFighterSummary(fighter.id, houseId)) }
    list
  }

  def getFighterSummary(fighterId: Int, houseId: Int): FighterSummary = {
    val fighter = Await.result(findById(fighterId), 2.seconds).get
    val profile = Await.result(profileRepo.findById(fighter.fighterType), 2.seconds).get
    val weaponsArmed = getFighterWeapons(fighterId, houseId)
    val fighterWeapons = Await.result(fighterWargearRepo.findByFighterId(fighterId), 2.seconds)
    val gearCostMap = wargearCostRepo.getCostMap(houseId)
    val skills = fighterSkillRepo.getFighterSkillList(fighter.id)
    val wargear = fighterWargearRepo.getFighterGearList(fighter.id)
    val combiFighters = Await.result(combiFighterRepo.findByFighterId(fighter.id), 2.seconds)
    val combisArmed = combiFighterRepo.getCombisArmed(combiFighters, houseId)
    val cost = getCost(profile.cost, weaponsArmed, houseId, fighterWeapons, combisArmed)
    new FighterSummary(fighter.id, fighter.name, profile.fighterClass, weaponsArmed, skills, wargear, combisArmed, cost)  
  }

  def deleteByGangId(gangId: Int): Unit = {
    Await.result(findByGangId(gangId), 2.seconds).foreach { fighter => 
      Await.result(deleteById(fighter.id), 1.seconds)
    }
  }

  def getGangList(gangId: Int): List[Tuple2[String, String]] = {
    var list = List[Tuple2[String, String]]()
    val fighters = Await.result(findByGangId(gangId), 2.seconds)
    fighters.foreach{ fighter => 
      list = list ::: List(Tuple2(fighter.id.toString, fighter.name))
    }
    list
  }
}




