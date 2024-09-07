package snakeGamePackage

import scalafx.application.{JFXApp3, Platform}
import scalafx.beans.property.{IntegerProperty, ObjectProperty}
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.Rectangle

import scala.concurrent.Future
import scala.util.Random

object game extends JFXApp3 {

  val initialSnake: List[(Int, Int)] = List(
    (10, 8),
    (9, 8),
    (8, 8)
  )

  val fieldWidth = 24;
  val fieldHeight = 24;

  import scala.concurrent.ExecutionContext.Implicits.global

  def gameLoop(update: () => Unit): Unit =
    Future {
      update()
      Thread.sleep(1000 / 25 * 2)
    }.flatMap(_ => Future(gameLoop(update)))

  case class GameState(snake: List[(Int, Int)], fruit: (Int, Int)) {
    def newState(dir: Int): GameState = {
      val (x, y) = snake.head
      val (newx, newy) = dir match {
        case 1 => (x, y - 1)
        case 2 => (x, y + 1)
        case 3 => (x - 1, y)
        case 4 => (x + 1, y)
        case _ => (x, y)
      }

      val newSnake: List[(Int, Int)] =
        if (newx < 0 || newx >= fieldWidth || newy < 0 || newy >= fieldHeight || snake.tail.contains((newx, newy)))
          initialSnake
        else if (fruit == (newx, newy))
          fruit :: snake
        else
          (newx, newy) :: snake.init

      val newFruit =
        if (fruit == (newx, newy))
          generateFruit()
        else
          fruit

      GameState(newSnake, newFruit)
    }

    def rectangles: List[Rectangle] = square(fruit._1, fruit._2, Orange) :: snake.map {
      case (x, y) => square(x, y, Green)
    }
  }

  def generateFruit(): (Int, Int) =
    (Random.nextInt(fieldWidth), Random.nextInt(fieldHeight))

  def square(xr: Double, yr: Double, color: Color) = new Rectangle {
    x = xr * 25
    y = yr * 25
    width = 25
    height = 25
    fill = color
  }

  override def start(): Unit = {
    val state = ObjectProperty(GameState(initialSnake, generateFruit()))
    val frame = IntegerProperty(0)
    val direction = IntegerProperty(4) // right

    frame.onChange {
      state.update(state.value.newState(direction.value))
    }

    stage = new JFXApp3.PrimaryStage {
      width = 650
      height = 650
      scene = new Scene {
        fill = Black
        content = state.value.rectangles
        onKeyPressed = key => key.getText match {
          case "w" => direction.value = 1
          case "s" => direction.value = 2
          case "a" => direction.value = 3
          case "d" => direction.value = 4
        }

        state.onChange(Platform.runLater {
          content = state.value.rectangles
        })
      }
    }

    gameLoop(() => frame.update(frame.value + 1))
  }

}
