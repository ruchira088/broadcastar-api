package com.ruchij.shared.web.requests

import play.api.http.HeaderNames
import play.api.mvc.RequestHeader

import scala.util.matching.Regex

trait SessionTokenExtractor {
  def token(requestHeader: RequestHeader): Option[String]
}

object SessionTokenExtractor extends SessionTokenExtractor {
  val authorizationCredentials: Regex = "[Bb]earer (\\S+)".r

  override def token(requestHeader: RequestHeader): Option[String] =
    requestHeader.headers.get(HeaderNames.AUTHORIZATION)
      .collect {
        case authorizationCredentials(credentials) => credentials
      }
}
