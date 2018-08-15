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
class SkillController @Inject()(
  cc: ControllerComponents,
  houseRepo: HouseRepo,
  gangRepo: GangRepo,
  weaponRepo: WeaponRepo,
  skillRepo: SkillRepo, 
  fighterSkillRepo: FighterSkillRepo) 
  extends AbstractController(cc) with play.api.i18n.I18nSupport {
  
  def index() = Action { implicit request: Request[AnyContent] =>
  	val skills = Await.result(skillRepo.all, 2.seconds)
  	Ok(views.html.skill.index(skills))
  }

  def create() = Action { implicit request: Request[AnyContent] =>  
    Ok(views.html.skill.create(skillForm))             
  }

  def submit() = Action { implicit request =>
    skillForm.bindFromRequest.fold (
      formWithErrors => { BadRequest("Bad Request") },
      skill => {
        val fighterId = Await.result(skillRepo.create(skill), 2.seconds)
        Redirect(routes.SkillController.index)
      })
  }

  def delete(id: Int) = Action { implicit request: Request[AnyContent] =>
  	val delete = Await.result(skillRepo.deleteById(id), 2.seconds)
    Redirect(routes.SkillController.index)
  }

  def addSkillToFighter() = Action { implicit request: Request[AnyContent] =>
    fighterSkillForm.bindFromRequest.fold (
      formWithErrors => { BadRequest("Bad Request") },
      fighterSkill => {
        Await.result(fighterSkillRepo.create(fighterSkill), 2.seconds)
        Redirect(routes.FighterController.show(fighterSkill.fighterId))
      })
  }

  def removeSkillFromFighter(id: Int) = Action { implicit request: Request[AnyContent] =>
    val skill = Await.result(fighterSkillRepo.findById(id), 2.seconds)
    Await.result(fighterSkillRepo.deleteById(id), 2.seconds)
    Redirect(routes.FighterController.show(skill.get.fighterId))

  }
}