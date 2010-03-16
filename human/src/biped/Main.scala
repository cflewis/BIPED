package biped

import javax.swing.{JFrame,SwingUtilities,Timer}
import java.awt.{Dimension}
import java.awt.event.{ActionListener,ActionEvent}

import com.sun.scenario.scenegraph._

object Main {

  val width = 1024
  val height = 768

  val board = new Board(width, height)
  val scene = new SGGroup

  var scriptName: String = "script.pl"

  implicit def easyBoolean (b: Boolean) = new {
      def onTrue(proc: =>Unit) {if (b) proc else ()}
  }

  implicit def easyOption[T] (op: Option[T]) = new {
      def onSome(proc: T=>Unit) {
          op match {
              case Some(t) => proc(t)
              case None => ()
          }
      }
  }

  def main(args: Array[String]) {
      if(args.length == 1) {
          scriptName = args(0)
      }

      SwingUtilities invokeLater new Runnable() {
                def run = {
                    createAndShowGUI
                    init
                }
      }
  }

  def createAndShowGUI() {
      val panel = new JSGPanel
      panel setScene scene
      panel setPreferredSize new Dimension(width,height)

      val frame = new JFrame
      frame setTitle "BIPED - Human-Playable Prototype"
      frame setDefaultCloseOperation JFrame.EXIT_ON_CLOSE
      frame setResizable false
      frame add panel
      frame pack ()
      frame setVisible true
  }

  def sync() {
      Script setof1 "ui_details" match {case s =>
              board.Details(s.mkString("\n"))
      }
      Script setof2 "ui_location_total" foreach { case (tokenIdent, spaceIdent) =>
        board.Token(tokenIdent) setHome board.Space(spaceIdent)
      }
  }

  def advance() {
      Script call1 "timepoint" onSome (t => println("timepoint: "+t))
      val events = Script setof1 "happens"
      println("events: \n"+ events.map("\t"+_).mkString("\n"))
      val nexts = Script setof1 "holds_next"
      println("next state: \n"+nexts.map("\t"+_).mkString("\n"))
      Script retractallHolds ()
      Script retractallHappens ()
      Script assertallHolds nexts
      Script incrementTimepoint ()
      sync()
  }

  def init() {
      scene.add(board.node)

      Script load new java.io.FileInputStream(scriptName)

      println("Design issues:")
      Script setof1 "design_issue" foreach { issue =>
          println("\t"+issue)
      }
      println()

      Script call1 "ui_timer" onSome { timeoutText =>
          val timer = new Timer(timeoutText.toInt, new ActionListener() {
                  def actionPerformed(e: ActionEvent) {
                      val event = "ui_timeout"
                      Script assertHappens "ui_timeout" onTrue {
                            advance()
                      }
                  }
          })
          timer setRepeats false
          timer start
      }

      Script call1 "ui_ticker" onSome { timeoutText =>
          val ticker = new Timer(timeoutText.toInt, new ActionListener() {
                  var count: Int = 0
                  def actionPerformed(e: ActionEvent) {
                      Script assertHappens ("ui_tick("+count+")") onTrue {
                              count += 1
                              advance()
                      }
                      
                  }
          })
          ticker start
      }

      Script call1 "ui_soundtrack" onSome (board.Soundtrack(_))

      Script call1 "ui_title"  onSome (board.Title(_))

      Script call1 "ui_instruction" onSome { raw =>
          val nice = raw.replace("\\n","\n")
          board.Instruction(nice)
      }

      Script setof1 "ui_space" foreach (board.Space(_))

      Script setof1 "ui_token" foreach (board.Token(_))

      Script setof2 "ui_path" foreach {case (is, id) =>
              board.Path(board.Space(is), board.Space(id))
      }

      Script setof1 "initially" match {case is => Script assertallHolds(is)}
      val nows = Script setof1 "holds"
      println("initially: "+nows)

      Script call1 "ui_autolayout" onSome {
          case "circular" => board.spaceLayer.arrange()
      }

      Script setof3 "ui_layout" foreach { case (is, tx, ty) =>
              board.Space(is).reposition(tx.toDouble*width, ty.toDouble*height)
      }

      sync()

      board.tokenClickListeners += new board.TokenClickListener {
          def clickedToken(token: board.Token) {
              if(board.tokenSpaces(token) != board.outOfPlay) {
                  Script  assertHappens ("ui_click_token", token.ident) onTrue {
                      advance()
                  }
              }
          }
      }

      board.spaceClickListeners += new board.SpaceClickListener {
          def clickedSpace(space: board.Space) {
              if(space != board.outOfPlay) {
                  Script assertHappens ("ui_click_space", space.ident) onTrue {
                      advance()
                  }
              }
          }
      }

  }

}


