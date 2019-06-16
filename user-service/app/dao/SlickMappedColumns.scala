package dao

import java.sql.Timestamp

import com.ruchij.enum.{Enum, EnumValues}
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.jdbc.{JdbcProfile, JdbcType}

import scala.reflect.ClassTag
import scala.util.Try

object SlickMappedColumns {
  implicit def dateTimeMappedColumn(implicit jdbcProfile: JdbcProfile): JdbcType[DateTime] with BaseTypedType[DateTime] = {
    import jdbcProfile.api._
    jdbcProfile.MappedColumnType.base[DateTime, Timestamp](dateTime => new Timestamp(dateTime.getMillis), timestamp => new DateTime(timestamp.getTime))
  }

  implicit def enumMappedColumn[A <: Enum: ClassTag](implicit jdbcProfile: JdbcProfile, enumValues: EnumValues[A]): JdbcType[A] with BaseTypedType[A] = {
    import jdbcProfile.api._
    jdbcProfile.MappedColumnType.base[A, String](_.key, string => unsafe(Enum.parse(string)))
  }

  private def unsafe[A](value: => Try[A]): A = value.get
}
