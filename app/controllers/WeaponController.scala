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
class WeaponController @Inject()(
  cc: ControllerComponents,
  houseRepo: HouseRepo,
  gangRepo: GangRepo,
  fighterRepo: FighterRepo,
  profileRepo: FighterProfileRepo, 
  weaponRepo: WeaponRepo,
  fighterWeaponRepo: FighterWeaponRepo,
  traitsRepo: TraitsRepo,
  weaponTraitsRepo: WeaponsTraitsRepo,
  weaponCostRepo: WeaponCostRepo
) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def show(weaponId: Int) =  Action { implicit request: Request[AnyContent] =>
    val weapon = Await.result(weaponRepo.findById(weaponId), 2.seconds).get
    val traits = traitsRepo.getTraitList(Await.result(traitsRepo.all, 2.seconds))
    val weaponTraits = weaponRepo.getWeaponTraits(weaponId)
    val houseList = houseRepo.getHouseList
    val houseMap = houseRepo.getHouseMap(houseList)
    val weaponCosts = Await.result(weaponCostRepo.getCosts(weaponId), 2.seconds)
    Ok(views.html.weapon.show(weapon, traits, weaponTraitForm, weaponTraits, weaponCostForm, houseList, weaponCosts, houseMap))
  } 

  def index() =  Action { implicit request: Request[AnyContent] =>
    var list = List[WeaponWithTraits]()
    Await.result(weaponRepo.all, 2.seconds).foreach { weapon => 
      val weaponTraits = weaponRepo.getWeaponTraits(weapon.id)
      list = list ::: List(WeaponWithTraits(weapon, weaponTraits))
    }
    Ok(views.html.weapon.index(list))
  }

  def addTraitToWeapon() = Action { implicit request: Request[AnyContent] =>
    weaponTraitForm.bindFromRequest.fold (
      formWithErrors => { BadRequest("Bad Request") },
      weaponTrait => {
        Await.result(weaponTraitsRepo.create(weaponTrait), 2.seconds)
        Redirect(routes.WeaponController.show(weaponTrait.weaponId))
      }
    )
  }

  def removeTraitFromWeapon(weaponTraitId: Int) = Action { implicit request: Request[AnyContent] =>
    val weaponTrait = Await.result(weaponTraitsRepo.findById(weaponTraitId), 2.seconds).get
    weaponTraitsRepo.deleteById(weaponTraitId)
    Redirect(routes.WeaponController.show(weaponTrait.weaponId))
  }

  def addWeaponToFighter() = Action { implicit request => 
    weaponForm.bindFromRequest.fold (
      formWithErrors => { BadRequest("Bad Request") },
      fighterWeapon => { 
        Await.result(fighterWeaponRepo.add(fighterWeapon), 2.seconds)
        Redirect(routes.FighterController.show(fighterWeapon.fighterId))
      }
    )
  }

  def removeWeaponFromFighter(id: Int) = Action { implicit request => 
  	val fighterId = Await.result(fighterWeaponRepo.findById(id), 2.seconds).get.fighterId
  	Await.result(fighterWeaponRepo.deleteById(id), 2.seconds) match {
  	  case i: Int => Redirect(routes.FighterController.show(fighterId))
  	}
  }

  def addCostToWeapon() = Action { implicit request =>
    weaponCostForm.bindFromRequest.fold (
      formWithErrors => { Ok(formWithErrors.toString) },
      weaponCost => {
        Await.result(weaponCostRepo.create(weaponCost), 2.seconds)
        Redirect(routes.WeaponController.show(weaponCost.weaponId))
      }
    )
  }

  def removeCostFromWeapon(id: Int, costId: Int) = Action { implicit request => 
    Await.result (weaponCostRepo.deleteById(costId), 2.seconds)
    Redirect(routes.WeaponController.show(id))
  }

}