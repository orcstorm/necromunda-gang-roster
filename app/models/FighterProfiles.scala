package models 

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio
import slick.dbio.Effect.Read
import slick.jdbc.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.Future

case class FighterProfile(
	id: Int, houseId: Int, fighterClass: String, move: Int, 
	weaponSkill: Int, ballisticSkill: Int, strength: Int, toughness: Int, 
	wounds: Int, initiative: Int, attacks: Int, 
	leadership: Int, cool: Int, willpower: Int, intelligence: Int, cost: Int)

class FighterProfileRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val FighterProfiles = TableQuery[FighterProfilesTable]

  def all: Future[List[FighterProfile]] = db.run(FighterProfiles.to[List].result)

  def findByHouseId(houseId: Int): Future[List[FighterProfile]] = db.run(_findByHouseId(houseId))

  def _findByHouseId(houseId: Int): DBIO[List[FighterProfile]] = FighterProfiles.filter(_.houseId === houseId).to[List].result

  def findById(id: Int): Future[Option[FighterProfile]] = db.run(_findById(id))

  def _findById(id: Int): DBIO[Option[FighterProfile]] = FighterProfiles.filter(_.id === id).result.headOption


  private[models] class FighterProfilesTable(tag: Tag) extends Table[FighterProfile](tag, "fighter_profiles") {
  	def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  	def houseId = column[Int]("house_id")
    def fighterClass = column[String]("fighter_class")
    def move = column[Int]("move")
    def weaponSkill = column[Int]("weapon_skill")
    def ballisticSkill = column[Int]("ballistic_skill")
    def strength = column[Int]("strength")
    def toughness = column[Int]("toughness")
    def wounds = column[Int]("wounds")
    def initiative = column[Int]("initiative")
    def attacks = column[Int]("attacks")
    def leadership = column[Int]("leadership")
    def cool = column[Int]("cool")
    def willpower = column[Int]("willpower")
    def intelligence = column[Int]("intelligence")
    def cost = column[Int]("cost")
    def * = (id, houseId, fighterClass, move, weaponSkill, ballisticSkill, strength, toughness, wounds, initiative, attacks, leadership, cool, willpower, intelligence, cost) <> (FighterProfile.tupled, FighterProfile.unapply)
  }
}
