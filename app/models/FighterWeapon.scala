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

case class FighterWeapon(id: Int, fighterId: Int, weaponId: Int)

class FighterWeaponRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] class FighterWeaponTable(tag: Tag) extends Table[FighterWeapon](tag, "fighter_weapon") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def fighterId = column[Int]("fighter_id")
    def weaponId = column[Int]("weapon_id")
    def * = (id, fighterId, weaponId) <> (FighterWeapon.tupled, FighterWeapon.unapply)
  }

  private[models] val FighterWeapons = TableQuery[FighterWeaponTable]

  def all: Future[List[FighterWeapon]] = db.run(FighterWeapons.to[List].result)

  def add(fighterWeapon: FighterWeapon): Future[Int] = { db.run(FighterWeapons returning FighterWeapons.map(_.id) += fighterWeapon) }

  def findByFighterId(fighterId: Int): Future[List[FighterWeapon]] = db.run(_findByFighterId(fighterId))

  def findById(id: Int): Future[Option[FighterWeapon]] = db.run(_findById(id))

  def deleteById(id: Int): Future[Int] = { db.run(FighterWeapons.filter(_.id === id).delete) }

  private def _findByFighterId(fighterId: Int): DBIO[List[FighterWeapon]] = FighterWeapons.filter(_.fighterId === fighterId).to[List].result

  private def _findById(id: Int): DBIO[Option[FighterWeapon]] = FighterWeapons.filter(_.id === id).result.headOption

}