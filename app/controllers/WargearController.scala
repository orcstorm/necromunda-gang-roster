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
class WargearController @Inject()(
  cc: ControllerComponents,
  houseRepo: HouseRepo,
  wargearRepo: WargearRepo,
  wargearCostRepo: WargearCostRepo,
  fighterWargearRepo: FighterWargearRepo) extends AbstractController(cc) with play.api.i18n.I18nSupport {
  
  def index() = Action { implicit request: Request[AnyContent] =>
    val wargear = Await.result(wargearRepo.all, 2.seconds).sortBy(_.name)
    Ok(views.html.wargear.index(wargear))
  }

  def show(id: Int) = Action { implicit request: Request[AnyContent] =>
    val gear = Await.result(wargearRepo.findById(id), 2.seconds).get
    val costs = Await.result(wargearCostRepo.findByWargearId(gear.id), 2.seconds)
    val houseList = houseRepo.getHouseList
    val houseMap = houseRepo.getHouseMap(houseList)
    Ok(views.html.wargear.show(gear, costs, wargearCostForm, houseList, houseMap))
  }

  def create() = Action { implicit request: Request[AnyContent] =>  
    Ok(views.html.wargear.create(wargearForm))             
  }

  def submit() = Action { implicit request: Request[AnyContent] =>  
    wargearForm.bindFromRequest.fold (
      formWithErrors => { BadRequest("Bad Request") },
      wargear => {
        val fighterId = Await.result(wargearRepo.create(wargear), 2.seconds)
        Redirect(routes.WargearController.index)
      })
  }

  def delete(id: Int) = Action { implicit request: Request[AnyContent] =>
    Await.result(wargearRepo.deleteById(id), 2.seconds)
    Redirect(routes.WargearController.index)
  }

  def addGearToFighter() = Action { implicit request: Request[AnyContent] =>  
    fighterWargearForm.bindFromRequest.fold(
      formWithErrors => { BadRequest("Bad Request") },
      fwg => {
        Await.result(fighterWargearRepo.create(fwg), 2.seconds)
        Redirect(routes.FighterController.show(fwg.fighterId))
      }
    )
  }

  def removeGearFromFighter(id: Int) = Action { implicit request: Request[AnyContent] =>  
    val fighterId = Await.result(fighterWargearRepo.findById(id), 2.seconds).get.fighterId
    Await.result(fighterWargearRepo.deleteById(id), 2.seconds)
    Redirect(routes.FighterController.show(fighterId))
  }

  def addCostToGear() = Action { implicit request: Request[AnyContent] =>
    wargearCostForm.bindFromRequest.fold (
      formWithErrors => { BadRequest("Bad Request") },
      wargearCost => {
        Await.result(wargearCostRepo.create(wargearCost), 2.seconds)
        Redirect(routes.WargearController.show(wargearCost.gearId))
      }
    )
  }

  def removeCostFromGear(id: Int) = Action { implicit request: Request[AnyContent] =>
    val wargearCost = Await.result(wargearCostRepo.findById(id), 2.seconds).get
    Await.result(wargearCostRepo.deleteById(id), 2.seconds)
    Redirect(routes.WargearController.show(wargearCost.gearId))
  }
}