package services

import com.google.inject.Inject
import models.{CarAdvert, CarAdvertList}

import scala.concurrent.{ExecutionContext, Future}


class CarAdvertService @Inject() (items: CarAdvertList) (implicit executionContext: ExecutionContext){
  def addItem(item: CarAdvert): Future[String] = {
    items.add(item)
  }

  def deleteItem(id: Long): Future[Int] = {
    items.delete(id)
  }

  def getItem(id: Long): Future[Option[CarAdvert]] = {
    items.get(id)
  }

  def listAllItems: Future[Seq[CarAdvert]] = {
    items.listAll
  }

  def updateItem(item: CarAdvert): Future[Int] = {
    items.update(item)
  }

  def validateCarAdvert(carAdvert: CarAdvert): Future[Option[List[String]]] = {
    var validationErrors : List[String] = List()

    if(carAdvert.id < 0){
      validationErrors = "id must be a positive number" :: validationErrors
    }
    if(carAdvert.title.length() == 0){
      validationErrors = "name cant be an empty string" :: validationErrors
    }
    if(carAdvert.fuelType.length() == 0){
      validationErrors = "fuelType cant be an empty string" :: validationErrors
    }
    if(carAdvert.price < 0){
      validationErrors = "price cant be a negative number" :: validationErrors
    }
    if(carAdvert.mileage < 0){
      validationErrors = "mileage cant be a negative number" :: validationErrors
    }

    if(validationErrors.length==0) Future(None)
    else Future(Some(validationErrors.reverse))
  }
}
