package models

import com.google.inject.Inject
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api._

case class CarAdvert(
  id: Long,
  title: String,
  fuelType: String,
  price: Int,
  isNew: Boolean,
  mileage: Int,
  firstRegistration: String
)

case class CarAdvertFormData(
  title: String,
  fuelType: String,
  price: Int,
  isNew: Boolean,
  mileage: Int,
  firstRegistration: String
)

object CarAdvertForm {
  val form = Form(
    mapping(
      "title" -> nonEmptyText,
      "fuelType" -> nonEmptyText,
      "price" -> number,
      "isNew" -> boolean,
      "mileage" -> number,
      "firstRegistration" -> nonEmptyText
    )(CarAdvertFormData.apply)(CarAdvertFormData.unapply)
  )
}

class CarAdvertTableDef(tag: Tag) extends Table[CarAdvert](tag, "caradverts") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def fuelType = column[String]("fuelType")
  def price = column[Int]("price")
  def isNew = column[Boolean]("isNew")
  def mileage = column[Int]("mileage")
  def firstRegistration = column[String]("firstRegistration")

  override def * = (id, title, fuelType, price, isNew, mileage, firstRegistration) <> (CarAdvert.tupled, CarAdvert.unapply)
}

class CarAdvertList @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
) (implicit  executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  var carAdvertList = TableQuery[CarAdvertTableDef]

  def get(id: Long): Future[Option[CarAdvert]] = {
    dbConfig.db.run(carAdvertList.filter(_.id === id).result.headOption)
  }

  def listAll: Future[Seq[CarAdvert]] = {
    dbConfig.db.run(carAdvertList.result)
  }

  def add(item: CarAdvert): Future[String] = {
    dbConfig.db
      .run(carAdvertList += item)
      .map(res => "CarAdvertItem successfully added!")
      .recover {
        case ex: Exception => {
          printf(ex.getMessage())
          ex.getMessage
        }
      }
  }

  def update(carAdvertItem: CarAdvert): Future[Int] = {
    dbConfig.db
      .run(
        carAdvertList.filter(_.id === carAdvertItem.id)
          .map(x => (
              x.title,
              x.fuelType,
              x.price,
              x.isNew,
              x.mileage,
              x.firstRegistration
            ))
          .update(
            carAdvertItem.title,
            carAdvertItem.fuelType,
            carAdvertItem.price,
            carAdvertItem.isNew,
            carAdvertItem.mileage,
            carAdvertItem.firstRegistration
          )
      )
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(carAdvertList.filter(_.id === id).delete)
  }
}