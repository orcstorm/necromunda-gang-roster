package controllers

import javax.inject.Inject
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

class ApplicationController @Inject()
  (cc: ControllerComponents)
  extends AbstractController(cc) with I18nSupport {

  def index = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def error = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.error())
  }

}
