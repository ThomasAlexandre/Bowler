package org.bowlerframework.view

import org.scalatest.FunSuite
import java.io.StringWriter
import org.bowlerframework.jvm.{DummyRequest, DummyResponse}
import collection.mutable.MutableList
import org.bowlerframework.exception.ValidationException
import org.bowlerframework.GET
import org.bowlerframework.model.Transient

/**
 * Created by IntelliJ IDEA.
 * User: wfaler
 * Date: 28/12/2010
 * Time: 23:47
 * To change this activeLayout use File | Settings | File Templates.
 */

class JsonViewRendererTest extends FunSuite {
  val renderer = new JsonViewRenderer
  test("renderView single bean") {
    val writer = new StringWriter
    val resp = new DummyResponse(writer)
    renderer.renderView(new DummyRequest(GET, "/", Map(), null, Map("accept" -> "application/json")), resp, toSeq(new Winner(1, List(1, 2, 3, 4, 5, 6))))
    assert(200 == resp.getStatus)
    assert("{\"id\":1,\"numbers\":[1,2,3,4,5,6]}".equals(writer.toString))

  }

  def toSeq(models: Any*): Seq[Any] = models.toSeq

  test("render view singlebean with nested") {
    val group = Group("myGroup", Winner(1, List(1, 2, 3, 4, 5, 6)), List(Winner(1, List(1, 2, 3, 4, 5, 6)), Winner(2, List(3, 4, 5, 6, 7, 8))))

    val writer = new StringWriter
    val resp = new DummyResponse(writer)
    renderer.renderView(new DummyRequest(GET, "/", Map(), null, Map("accept" -> "application/json")), resp, toSeq(group))
    assert(200 == resp.getStatus)
    val result = "{\"name\":\"myGroup\",\"biggestWinner\":{\"id\":1,\"numbers\":[1,2,3,4,5,6]},\"winners\":[{\"id\":1,\"numbers\":[1,2,3,4,5,6]},{\"id\":2,\"numbers\":[3,4,5,6,7,8]}]}"
    assert(result == writer.toString)

  }

  test("renderView single List of items") {
    val list = List(Winner(1, List(1, 2, 3, 4, 5, 6)), Winner(2, List(3, 4, 5, 6, 7, 8)))
    val writer = new StringWriter
    val resp = new DummyResponse(writer)
    renderer.renderView(new DummyRequest(GET, "/", Map(), null, Map("accept" -> "application/json")), resp, toSeq(list))
    assert(200 == resp.getStatus)
    assert("[{\"id\":1,\"numbers\":[1,2,3,4,5,6]},{\"id\":2,\"numbers\":[3,4,5,6,7,8]}]".equals(writer.toString))
  }

  test("renderView none - should be HTTP 204") {
    val writer = new StringWriter
    val resp = new DummyResponse(writer)
    renderer.renderView(new DummyRequest(GET, "/", Map(), null, Map("accept" -> "application/json")), resp)
    assert(204 == resp.getStatus)
  }

  test("renderView single ViewModel") {
    val winner = ViewModel("loser", Winner(1, List(1, 2, 3, 4, 5, 6)))
    val writer = new StringWriter
    val resp = new DummyResponse(writer)
    renderer.renderView(new DummyRequest(GET, "/", Map(), null, Map("accept" -> "application/json")), resp, toSeq(winner))
    val result = "{\"alias\":\"loser\",\"value\":{\"id\":1,\"numbers\":[1,2,3,4,5,6]}"
    assert(200 == resp.getStatus)
  }

  test("ViewModel and object") {
    val list = List(Winner(1, List(1, 2, 3, 4, 5, 6)), Winner(2, List(3, 4, 5, 6, 7, 8)))
    val winner = ViewModel("loser", Winner(1, List(1, 2, 3, 4, 5, 6)))
    val writer = new StringWriter
    val resp = new DummyResponse(writer)
    renderer.renderView(new DummyRequest(GET, "/", Map(), null, Map("accept" -> "application/json")), resp, toSeq(winner, list))
    assert(200 == resp.getStatus)
    val result = "{\"loser\":{\"id\":1,\"numbers\":[1,2,3,4,5,6]},\"winners\":[{\"id\":1,\"numbers\":[1,2,3,4,5,6]},{\"id\":2,\"numbers\":[3,4,5,6,7,8]}]}"
    assert(result == writer.toString)
  }


  test("ViewModel and ListViewModel") {
    val list = ViewModel("losers", List(Winner(1, List(1, 2, 3, 4, 5, 6)), Winner(2, List(3, 4, 5, 6, 7, 8))))
    val winner = ViewModel("loser", Winner(1, List(1, 2, 3, 4, 5, 6)))
    val writer = new StringWriter
    val resp = new DummyResponse(writer)
    renderer.renderView(new DummyRequest(GET, "/", Map(), null, Map("accept" -> "application/json")), resp, toSeq(winner, list))
    assert(200 == resp.getStatus)
    val result = "{\"loser\":{\"id\":1,\"numbers\":[1,2,3,4,5,6]},\"losers\":[{\"id\":1,\"numbers\":[1,2,3,4,5,6]},{\"id\":2,\"numbers\":[3,4,5,6,7,8]}]}"
    assert(result == writer.toString)
  }

  test("renderView multiple items") {
    val list = List(Winner(1, List(1, 2, 3, 4, 5, 6)), Winner(2, List(3, 4, 5, 6, 7, 8)))
    val winner = Winner(1, List(1, 2, 3, 4, 5, 6))
    val writer = new StringWriter
    val resp = new DummyResponse(writer)
    renderer.renderView(new DummyRequest(GET, "/", Map(), null, Map("accept" -> "application/json")), resp, toSeq(winner, list))
    assert(200 == resp.getStatus)
    val result = "{\"winner\":{\"id\":1,\"numbers\":[1,2,3,4,5,6]},\"winners\":[{\"id\":1,\"numbers\":[1,2,3,4,5,6]},{\"id\":2,\"numbers\":[3,4,5,6,7,8]}]}"
    assert(result == writer.toString)
  }

  test("render validationerrors") {
    val list = new MutableList[Tuple2[String, String]]
    list += Tuple2("name", "name is mandatory!")
    list += Tuple2("age", "age must be over 18!")

    val writer = new StringWriter
    val resp = new DummyResponse(writer)
    try {
      renderer.onError(new DummyRequest(GET, "/", Map(), null, Map("accept" -> "application/json")), resp, new ValidationException(list.toList))
    } catch {
      case e: ValidationException => {
        assert(e.errors.size == 2)
      }
    }


  }

  test("render with trait") {
    val writer = new StringWriter
    val resp = new DummyResponse(writer)
    renderer.renderView(new DummyRequest(GET, "/", Map(), null, Map("accept" -> "application/json")), resp, toSeq(new Author))
    println("render with trait: " + resp.toString)
  }

}

case class Group(name: String, biggestWinner: Winner, winners: List[Winner])

case class Winner(id: Long, numbers: List[Int])

case class Author(val id: Long, firstName: String, lastName: String, email: Option[String]) extends Transient {
  def this() = this (0, "John", "Doe", Some("johndoe@gmail.com"))
}