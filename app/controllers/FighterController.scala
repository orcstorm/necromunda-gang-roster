package controllers

import javax.inject._
import play.api.mvc._
import models._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import forms.Forms._

case class FD( fighter: Fighter, fighterProfile: FighterProfile, gang: Gang, house: House,
               weapons: List[Tuple2[String, String]], weaponsArmed: Map[Int, ArmedWeapon],
               skills: List[Tuple2[String, String]], skillMap: Map[Int, String],
               fighterSkills: List[FighterSkill], wargear: List[Tuple2[String, String]],
               wargearMap: Map[Int, String], fighterWargear: List[FighterWargear],
               gearCostMap: Map[Int, Int], combis: List[Tuple2[String, String]],
               combiFighters: List[CombiFighter], combisArmed: Map[Int, ArmedCombi],
               cost: Int, gangList: List[Tuple2[String, String]], edit: Boolean)

@Singleton
class FighterController @Inject()(
  cc: ControllerComponents,
  houseRepo: HouseRepo,
  gangRepo: GangRepo,
  fighterRepo: FighterRepo,
  profileRepo: FighterProfileRepo, 
  weaponRepo: WeaponRepo,
  fighterWeaponRepo: FighterWeaponRepo, 
  skillRepo: SkillRepo,
  fighterSkillRepo: FighterSkillRepo,
  wargearRepo : WargearRepo,
  fighterWargearRepo: FighterWargearRepo,
  wargearCostRepo: WargearCostRepo,
  combiRepo: CombiRepo,
  combiCostRepo: CombiCostRepo,
  combiFighterRepo: CombiFighterRepo
) extends AbstractController(cc) with play.api.i18n.I18nSupport {



  def index() = Action { implicit request: Request[AnyContent] => 
    Ok("hi")
  }

  def show(fighterId: Int) = Action { implicit request: Request[AnyContent] =>
    val fd: FD = new FighterDisplay(fighterId, false).getFD
    Ok(views.html.fighter.show(fd))
  }

  def create(gangId: Int) = Action { implicit request: Request[AnyContent] =>  
    Await.result(gangRepo.findById(gangId), 2.seconds) match {
      case Some(gang) => {
        Await.result(houseRepo.findById(gang.house), 2.seconds) match {
          case Some(house) => {
            val profiles = Await.result(profileRepo.findByHouseId(house.id), 2.seconds)
            val profs = fighterRepo.getProfileMap(profiles)
            Ok(views.html.fighter.create(fighterForm, house, gang, profs))             
          }
          case None => InternalServerError("internal server error")
        } 
      }
      case None => InternalServerError("internal server error")
    }
  }

  def submit() = Action { implicit request =>
    fighterForm.bindFromRequest.fold (
      formWithErrors => { BadRequest("Bad Request") },
      fighter => {
        val fighterId = Await.result(fighterRepo.create(fighter), 2.seconds)
        Redirect(routes.FighterController.show(fighterId))
      })
  } 

  def delete(id: Int) = Action { implicit request: Request[AnyContent] =>
    val fighter = Await.result(fighterRepo.findById(id), 2.seconds).get
    val gang = Await.result(gangRepo.findById(fighter.gangId), 2.seconds).get
    val delete = Await.result(fighterRepo.deleteById(id), 2.seconds)
    Redirect(routes.GangController.show(gang.id))
  }

  class FighterDisplay(fighterId: Int, isEdit: Boolean) {
    val fighter: Fighter = Await.result(fighterRepo.findById(fighterId), 2.seconds).get
    val fighterProfile: FighterProfile = Await.result(profileRepo.findById(fighter.fighterType), 2.seconds).get
    val gang: Gang = Await.result(gangRepo.findById(fighter.gangId), 2.seconds).get
    val house: House = Await.result(houseRepo.findById(gang.house), 2.seconds).get
    val weapons: List[Tuple2[String, String]] = weaponRepo.getWeapons(Await.result(weaponRepo.all, 2.seconds))
    val weaponsArmed: Map[Int, ArmedWeapon] = fighterRepo.getFighterWeapons(fighterId, house.id)
    val skills: List[Tuple2[String, String]] = skillRepo.getSkills
    val skillMap: Map[Int, String] = skillRepo.getSkillsMap
    val fighterSkills: List[FighterSkill] = Await.result(fighterSkillRepo.getFighterSkills(fighter.id), 2.seconds)
    val wargear: List[Tuple2[String, String]] = wargearRepo.getWargear
    val wargearMap: Map[Int, String] = wargearRepo.getWargearMap
    val fighterWargear: List[FighterWargear] = Await.result(fighterWargearRepo.findByFighterId(fighter.id), 2.seconds)
    val gearCostMap: Map[Int, Int] = wargearCostRepo.getCostMap(house.id)
    val combis: List[Tuple2[String, String]] = Await.result(combiRepo.getCombis, 2.seconds)
    val combiFighters: List[CombiFighter] = Await.result(combiFighterRepo.findByFighterId(fighter.id), 2.seconds)
    val combisArmed: Map[Int, ArmedCombi] = combiFighterRepo.getCombisArmed(combiFighters, house.id)
    val cost: Int = fighterRepo.getCost(fighterProfile.cost, weaponsArmed, house.id, fighterWargear, combisArmed)
    val gangList: List[Tuple2[String, String]] = fighterRepo.getGangList(gang.id)
    val edit: Boolean = isEdit

    def getFD: FD = {
      FD(fighter, fighterProfile, gang, house, weapons, weaponsArmed, skills, skillMap, fighterSkills,
        wargear, wargearMap, fighterWargear, gearCostMap, combis, combiFighters, combisArmed,
        cost, gangList, edit)
    }
  }
}

