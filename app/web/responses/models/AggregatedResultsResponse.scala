package web.responses.models

import play.api.libs.json.{Json, OWrites, Writes}

case class AggregatedResultsResponse[+A](results: List[A])

object AggregatedResultsResponse {
  implicit def aggregatedResultsResponseWrite[A: Writes]: OWrites[AggregatedResultsResponse[A]] =
    Json.writes[AggregatedResultsResponse[A]]
}
