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
class CombiController @Inject()(
  cc: ControllerComponents,
  houseRepo: HouseRepo,
  weaponRepo: WeaponRepo,
  combiRepo: CombiRepo,
  combiWeaponRepo: CombiWeaponRepo,
  combiCostRepo: CombiCostRepo,
  combiFighterRepo: CombiFighterRepo
) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def index = Action { implicit request: Request[AnyContent] => 
    val combis = Await.result(combiRepo.all, 2.seconds)
    Ok(views.html.combi.index(combis, combiForm))
  }

  def submit = Action { implicit request: Request[AnyContent] => 
    combiForm.bindFromRequest.fold (
      formWithErrors => { BadRequest("Bad Request") },
      combi => {
        Await.result(combiRepo.create(combi), 2.seconds)
        Redirect(routes.CombiController.index)
      })
  }

  def delete(id: Int) = Action { implicit request: Request[AnyContent] => 
    Await.result(combiRepo.deleteById(id), 2.seconds)
    Redirect(routes.CombiController.index)
  }

  def show(id: Int) = Action { implicit request: Request[AnyContent] =>
    val combi = Await.result(combiRepo.findById(id), 2.seconds).get
    val weaponList = weaponRepo.getWeapons
    val weaponsCombined: Map[Int, Weapon] = combiWeaponRepo.getWeaponsForCombi(id)
    val houseList = houseRepo.getHouseList
    val houseMap = houseRepo.getHouseMap(houseList)
    val combiCosts = Await.result(combiCostRepo.getCosts(combi.id), 2.seconds)
    Ok(views.html.combi.show(combi, combiWeaponForm, combiCostForm, weaponList, weaponsCombined, houseList, combiCosts, houseMap))
  }

  def addWeaponToCombi() = Action { implicit request: Request[AnyContent] =>
    combiWeaponForm.bindFromRequest.fold (
      formWithErrors => { Ok(formWithErrors.toString) },
      combiWeapon => { 
        Await.result(combiWeaponRepo.create(combiWeapon), 2.seconds)
        Redirect(routes.CombiController.show(combiWeapon.combiId))
      }
    )
  }

  def removeWeaponFromCombi(id: Int) = Action { implicit request: Request[AnyContent] =>
    val combi = combiWeaponRepo.findById(id).get
    combiWeaponRepo.deleteById(id)
    Redirect(routes.CombiController.show(combi.combiId))
  }

  def addCostToCombi() = Action { implicit request: Request[AnyContent] =>
    combiCostForm.bindFromRequest.fold (
      formWithErrors => { BadRequest(formWithErrors.toString) },
      combiCost => {
        Await.result(combiCostRepo.create(combiCost), 2.seconds)
        Redirect(routes.CombiController.show(combiCost.combiId))
      }
    )
  }

  def removeCostFromCombi(id: Int) = Action { implicit request =>
    val combi = Await.result(combiCostRepo.findById(id), 2.seconds).get
    combiCostRepo.deleteById(id)
    Redirect(routes.CombiController.show(combi.combiId))
  }

  def addCombiToFighter() = Action { implicit request =>
    combiFighterForm.bindFromRequest.fold (
      formWithErrors => { BadRequest( "Bad Request " + formWithErrors.toString) },
      combiFighter => {
        Await.result(combiFighterRepo.create(combiFighter), 2.seconds)
        Redirect(routes.FighterController.show(combiFighter.fighterId))
      }
    )
  }

  def removeCombiFromFighter(id: Int) = Action { implicit request => 
    val fighterId = Await.result(combiFighterRepo.findById(id), 2.seconds).get.fighterId
    Await.result(combiFighterRepo.deleteById(id), 2.seconds)
    Redirect(routes.FighterController.show(fighterId))
  }

}