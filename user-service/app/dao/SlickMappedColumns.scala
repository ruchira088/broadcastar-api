package dao

import java.sql.Timestamp

import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.jdbc.{JdbcProfile, JdbcType}

object SlickMappedColumns {
  implicit def dateTimeMappedColumn(implicit jdbcProfile: JdbcProfile): JdbcType[DateTime] with BaseTypedType[DateTime] = {
    import jdbcProfile.api._
    jdbcProfile.MappedColumnType.base[DateTime, Timestamp](dateTime => new Timestamp(dateTime.getMillis), timestamp => new DateTime(timestamp.getTime))
  }
}
