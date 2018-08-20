package controllers

import javax.inject._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._
import models._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import forms.Forms._

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
    val fighter = Await.result(fighterRepo.findById(fighterId), 2.seconds).get
    val profile = Await.result(profileRepo.findById(fighter.fighterType), 2.seconds).get
    val gang = Await.result(gangRepo.findById(fighter.gangId), 2.seconds).get
    val house = Await.result(houseRepo.findById(gang.house), 2.seconds).get
    val weapons = weaponRepo.getWeapons(Await.result(weaponRepo.all, 2.seconds))
    val weaponsArmed = fighterRepo.getFighterWeapons(fighterId, house.id)
    val skills = skillRepo.getSkills
    val skillMap = skillRepo.getSkillsMap
    val fighterSkills = Await.result(fighterSkillRepo.getFighterSkills(fighter.id), 2.seconds)
    val wargear = wargearRepo.getWargear
    val wargearMap = wargearRepo.getWargearMap
    val fighterWargear = Await.result(fighterWargearRepo.findByFighterId(fighter.id), 2.seconds)
    val gearCostMap = wargearCostRepo.getCostMap(house.id)
    val combis = Await.result(combiRepo.getCombis, 2.seconds)
    val combiFighters = Await.result(combiFighterRepo.findByFighterId(fighter.id), 2.seconds)
    val combisArmed = combiFighterRepo.getCombisArmed(combiFighters, house.id)
    val cost = fighterRepo.getCost(profile.cost, weaponsArmed, house.id, fighterWargear, combisArmed)
    val gangList = fighterRepo.getGangList(gang.id)


    Ok(views.html.fighter
      .show(
        house, 
        gang, 
        fighter, 
        profile, 
        weapons,  
        weaponsArmed, 
        cost, 
        skills, 
        fighterSkills,
        skillMap,
        wargear,
        fighterWargear,
        wargearMap, 
        gearCostMap,
        combis, 
        combisArmed,
        gangList
      )
    )
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

}

