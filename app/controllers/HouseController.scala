package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HouseController @Inject()(
  cc: ControllerComponents,
  houseRepo: HouseRepo,
  gangRepo: GangRepo
) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    val houses = Await.result(houseRepo.all, 2.seconds)
    Ok(views.html.house.index(houses))
  }

  def show(id: Int) = Action { implicit request: Request[AnyContent] =>
  	val house = Await.result(houseRepo.findById(id), 2.seconds).get
    var gangs = List[Tuple2[Gang, Int]]()
    Await.result(gangRepo.findByHouseId(id), 2.seconds).foreach { gang =>
      gangs = gangs ::: List(gang -> gangRepo.getGangCost(gang.id, house.id))
    }
    Ok(views.html.house.show(house, gangs))
  }
}
