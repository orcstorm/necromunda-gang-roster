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
  weaponRepo: WeaponRepo,
  combiRepo: CombiRepo,
  combiWeaponRepo: CombiWeaponRepo
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
    Ok(views.html.combi.show(combi, combiWeaponForm, weaponList, weaponsCombined))
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
    val combi = combiWeaponRepo.findById(id)
    combiWeaponRepo.deleteById(id)
    Redirect(routes.CombiController.show(combi.get.combiId))
  }

}