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

case class Skill(id: Int, name: String, skillType: String, description: String)

class SkillRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val Skills = TableQuery[SkillsTable]

  private[models] class SkillsTable(tag: Tag) extends Table[Skill](tag, "skills") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def skillType = column[String]("skill_type")
    def description = column[String]("description")
    def * = (id, name, skillType, description) <> (Skill.tupled, Skill.unapply)
  }

  def getSkills: List[Tuple2[String, String]] = {
    var list = List[Tuple2[String, String]]()
    Await.result(all, 2.seconds).foreach { skill =>
      list = list ::: List(skill.id.toString -> s"[${skill.skillType}] ${skill.name}")
    }
    list
  }

  def getSkillsMap: Map[Int, String] = {
    var map = Map[Int, String]()
    getSkills.foreach { skill => map = map + (skill._1.toInt -> skill._2) }
    map
  }


  def all: Future[List[Skill]] = db.run(_all)

  private def _all: DBIO[List[Skill]] = Skills.sortBy(_.skillType.asc).to[List].result

  def create(skill: Skill): Future[Int] = db.run(_create(skill))

  private def _create(skill: Skill): DBIO[Int] = Skills returning Skills.map(_.id) += skill 

  def deleteById(id: Int): Future[Int] = db.run(_deleteById(id))

  private def _deleteById(id: Int): DBIO[Int] = Skills.filter(_.id === id).delete

  def findById(id: Int): Future[Option[Skill]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[Skill]] = Skills.filter(_.id === id).result.headOption

}

case class FighterSkill(id: Int, fighterId: Int, skillId: Int)

class FighterSkillRepo @Inject()(skillRepo: SkillRepo)(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._ 

  private[models] val FighterSkills = TableQuery[FighterSkillsTable]

  private[models] class FighterSkillsTable(tag: Tag) extends Table[FighterSkill](tag, "fighter_skills") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def fighterId = column[Int]("fighter_id")
    def skillId = column[Int]("skill_id")
    def * = (id, fighterId, skillId) <> (FighterSkill.tupled, FighterSkill.unapply)
  }

  def all: Future[List[FighterSkill]] = db.run(_all)

  private def _all: DBIO[List[FighterSkill]] = FighterSkills.to[List].result

  def create(fighterSkill: FighterSkill): Future[Int] = db.run(_create(fighterSkill))

  private def _create(fighterSkill: FighterSkill): DBIO[Int] = FighterSkills returning FighterSkills.map(_.id) += fighterSkill 

  def deleteById(id: Int): Future[Int] = db.run(_deleteById(id))

  private def _deleteById(id: Int): DBIO[Int] = FighterSkills.filter(_.id === id).delete

  def getFighterSkills(id: Int): Future[List[FighterSkill]] = db.run(findByFighterId(id))

  private def findByFighterId(id: Int): DBIO[List[FighterSkill]] = FighterSkills.filter(_.fighterId === id).to[List].result

  def findById(id: Int): Future[Option[FighterSkill]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[FighterSkill]] = FighterSkills.filter(_.id === id).result.headOption

  def getFighterSkillList(id: Int): List[String] = {
    var list = List[String]()

    Await.result(getFighterSkills(id), 2.seconds).foreach { skill => 
      list = list ::: List(Await.result(skillRepo.findById(skill.id), 2.seconds).get.name)
    }
    list
  } 
}
