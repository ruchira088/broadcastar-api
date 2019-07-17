package dao

import java.sql.Timestamp

import com.ruchij.enum.{Enum, EnumValues}
import com.ruchij.shared.monads.MonadicUtils
import org.joda.time.{DateTime, DateTimeZone}
import slick.ast.BaseTypedType
import slick.jdbc.{JdbcProfile, JdbcType}

import scala.reflect.ClassTag

object SlickMappedColumns {
  implicit def dateTimeMappedColumn(
    implicit jdbcProfile: JdbcProfile
  ): JdbcType[DateTime] with BaseTypedType[DateTime] = {
    import jdbcProfile.api._
    jdbcProfile.MappedColumnType.base[DateTime, Timestamp](
      dateTime => new Timestamp(dateTime.getMillis),
      timestamp => new DateTime(timestamp.getTime).withZone(DateTimeZone.UTC)
    )
  }

  implicit def enumMappedColumn[A <: Enum: ClassTag](
    implicit jdbcProfile: JdbcProfile,
    enumValues: EnumValues[A]
  ): JdbcType[A] with BaseTypedType[A] = {
    import jdbcProfile.api._
    jdbcProfile.MappedColumnType.base[A, String](_.key, string => MonadicUtils.unsafe(Enum.parse(string)))
  }
}
