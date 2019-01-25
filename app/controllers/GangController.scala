package controllers

import javax.inject._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._
import models._
import forms.Forms._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

@Singleton
class GangController @Inject()(
  cc: ControllerComponents,
  houseRepo: HouseRepo,
  gangRepo: GangRepo, 
  fighterRepo: FighterRepo,
  profileRepo: FighterProfileRepo, 

) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def index() = Action { implicit request: Request[AnyContent] =>
    val gangs = Await.result(gangRepo.all, 2.seconds)
    val houseMap = houseRepo.getHouseMap(houseRepo.getHouseList)
    Ok(views.html.gang.index(gangs, houseMap))
  }

  def show(id: Int) = Action { implicit request: Request[AnyContent] =>
  	val gang = Await.result(gangRepo.findById(id), 2.seconds).get
    val house = Await.result(houseRepo.findById(gang.house), 2.seconds).get
    val fighters = Await.result(fighterRepo.findByGangId(id), 2.seconds)
    val fighterSummaries = fighterRepo.getFighterSummmaries(fighters, house.id)
    val cost = gangRepo.getGangCost(fighterSummaries)
    Ok(views.html.gang.show(house, gang, fighterSummaries, cost))
  }

  def create(houseId: Int) =  Action { implicit request =>
    Await.result(houseRepo.findById(houseId), 2.seconds) match {
      case Some(house) => Ok(views.html.gang.create(gangForm, house))
      case None => InternalServerError("")  
    }
  }

  def submit() = Action { implicit request =>
    gangForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest("Bad Request")
    },
    gang => {
      val gangId = Await.result(gangRepo.create(gang), 2.seconds)
      Redirect(routes.GangController.show(gangId))
    })
  } 

  def delete(gangId: Int) = Action { implicit request =>
    val gang = Await.result(gangRepo.findById(gangId), 2.seconds).get
    gangRepo.deleteById(gangId)
    Redirect(routes.HouseController.show(gang.house))
  }

}