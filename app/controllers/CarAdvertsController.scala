package controllers

import models.{CarAdvert, CarAdvertForm}
import play.api.data.FormError

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.circe.Circe
import io.circe.syntax._
import io.circe.generic.auto._
import services.CarAdvertService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class CarAdvertsController @Inject()(controllerComponents: ControllerComponents, carAdvertService: CarAdvertService) extends AbstractController(controllerComponents) with Circe {

  def read(sortby: String) = Action.async { implicit request: Request[AnyContent] =>
    carAdvertService.listAllItems map { items =>
      Ok(items.sortWith((el1, el2) => sortby.toLowerCase() match {
        case "id" => el1.id < el2.id
        case "title" => el1.title < el2.title
        case "fueltype" => el1.fuelType < el2.fuelType
        case "price" => el1.price < el2.price
        case "isnew" => el1.isNew < el2.isNew
        case "mileage" => el1.mileage < el2.mileage
        case "firstregistration" => el1.firstRegistration < el2.firstRegistration
        case _ => false
      }).asJson)
    }
  }

  def readOne(id: Long): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
     carAdvertService.getItem(id) map { item =>
       if(item.isEmpty)
        NotFound("No car advert with given id was found.")
       else Ok(item.asJson)
     }
  }

  def add() = Action.async { implicit request: Request[AnyContent] =>
    CarAdvertForm.form.bindFromRequest.fold(
      errorForm => {
        errorForm.errors.foreach(println)
        Future.successful(BadRequest("Json is invalid or cannot be parsed!"))
      },
      data => {
        val carAdvertItem = CarAdvert(0, data.title, data.fuelType, data.price, data.isNew, data.mileage, data.firstRegistration)
        carAdvertService.validateCarAdvert(carAdvertItem) flatMap {
          case None => carAdvertService.addItem(carAdvertItem).map(_ => Created(carAdvertItem.asJson))
          case Some(validation_errors) => Future.successful(UnprocessableEntity(Map(("validation_errors", validation_errors)).asJson))
        }

      }
    )
  }

  def update(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    CarAdvertForm.form.bindFromRequest.fold(
      errorForm => {
        errorForm.errors.foreach(println)
        Future.successful(BadRequest("Json is invalid or cannot be parsed!"))
      },
      data => {
        val carAdvertItem = CarAdvert(id, data.title, data.fuelType, data.price, data.isNew, data.mileage,data.firstRegistration)

        carAdvertService.validateCarAdvert(carAdvertItem) flatMap {
          case None => carAdvertService.addItem(carAdvertItem).flatMap(_ =>
            carAdvertService.updateItem(carAdvertItem).map { res =>
              if (res > 0)
                Ok(carAdvertItem.asJson)
              else NotFound("Car advert with given id is not found.")
            })
          case Some(validation_errors) => Future.successful(UnprocessableEntity(Map(("validation_errors", validation_errors)).asJson))
        }
      }
    )
  }

  def delete(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    carAdvertService.deleteItem(id) map {res =>
      if(res > 0)
        NoContent
      else
        NotFound("Car advert with given id is not found.")
    }
  }
}
