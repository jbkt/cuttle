package com.criteo.cuttle

import io.circe._
import io.circe.syntax._

import java.time.{Instant}

object JsonApi {
  implicit val projectEncoder = new Encoder[Project] {
    override def apply(project: Project) =
      Json.obj(
        "name" -> project.name.asJson,
        "description" -> project.description.asJson
      )
  }

  implicit val InstantEncoder = new Encoder[Instant] {
    override def apply(date: Instant) =
      date.toString.asJson
  }

  implicit lazy val executionLogEncoder: Encoder[ExecutionLog] = new Encoder[ExecutionLog] {
    override def apply(execution: ExecutionLog) =
      Json.obj(
        "id" -> execution.id.asJson,
        "job" -> execution.job.asJson,
        "startTime" -> execution.startTime.asJson,
        "endTime" -> execution.endTime.asJson,
        "context" -> execution.context,
        "status" -> (execution.status match {
          case ExecutionSuccessful => "successful"
          case ExecutionFailed => "failed"
          case ExecutionRunning => "running"
          case ExecutionPaused => "paused"
          case ExecutionThrottled => "throttled"
        }).asJson,
        "failing" -> execution.failing.map {
          case FailingJob(failedExecutions, nextRetry) =>
            Json.obj(
              "failedExecutions" -> Json.fromValues(failedExecutions.map(_.asJson(executionLogEncoder))),
              "nextRetry" -> nextRetry.asJson
            )
        }.asJson
      )
  }

  implicit val tagEncoder = new Encoder[Tag] {
    override def apply(tag: Tag) =
      Json.obj(
        "name" -> tag.name.asJson,
        "description" -> tag.description.asJson,
        "color" -> tag.color.asJson
      )
  }

  implicit def jobEncoder[S <: Scheduling] = new Encoder[Job[S]] {
    override def apply(job: Job[S]) =
      Json
        .obj(
          "id" -> job.id.asJson,
          "name" -> job.name.getOrElse(job.id).asJson,
          "description" -> job.description.asJson,
          "tags" -> job.tags.map(_.name).asJson
        )
        .asJson
  }

  implicit def graphEncoder[S <: Scheduling] =
    new Encoder[Graph[S]] {
      override def apply(workflow: Graph[S]) = {
        val jobs = workflow.vertices.asJson
        val tags = workflow.vertices.flatMap(_.tags).asJson
        val dependencies = workflow.edges.map {
          case (to, from, _) =>
            Json.obj(
              "from" -> from.id.asJson,
              "to" -> to.id.asJson
            )
        }.asJson
        Json.obj(
          "jobs" -> jobs,
          "dependencies" -> dependencies,
          "tags" -> tags
        )
      }
    }
}
