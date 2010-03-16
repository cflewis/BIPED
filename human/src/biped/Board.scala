package biped

import java.awt._
import java.awt.event._
import java.awt.geom._

import com.sun.scenario.animation._
import com.sun.scenario.scenegraph._
import com.sun.scenario.scenegraph.event._


class Board(width: Int, height: Int) extends Visual { board =>

    var tokenSpaces = Map[Token,Space]()

    val titleLayer,
        instructionlayer,
        instructionLayer,
        pathLayer,
        spaceLayer,
        tokenLayer = new Layer()

    val root = node.asInstanceOf[SGGroup]
    root add titleLayer
    root add pathLayer
    root add spaceLayer
    root add tokenLayer
    root add instructionLayer

    class Layer extends SGGroup {
      def placeRandomly(node: SGNode): SGTransform.Translate = {
          val x = Math.abs(node.hashCode % width-200)+100
          val y = Math.abs((node.hashCode / width) % height-200)+100
          placeHere(node, x, y)
      }

      def placeCentered(node: SGNode): SGTransform.Translate = {
          placeHere(node, width/2, height/2)
      }

      def placeHere(node: SGNode, x: Double, y: Double): SGTransform.Translate = {
          val parent = SGTransform.createTranslation(x,y,node)
          add(parent)
          parent
      }

      def place(node: SGNode): SGNode = {
          add(node)
          node
      }

      def arrange() {
          var children = getChildren
          val n = children.size
          val rx = width/2-150
          val ry = height/2-100
          for (i <- 0 until n) {
              children.get(i) match {
                  case ch: SGTransform.Translate => {
                          ch.setTranslateX(width/2+rx*Math.cos(2*Math.Pi*i/n))
                          ch.setTranslateY(height/2+ry*Math.sin(2*Math.Pi*i/n))
                  }
              }
          }
      }

    }

    object Path {
        var paths = Set[Path]()
        def apply(src: Space, dst: Space): Path = {
            val op = paths.find(p => Set(p.src,p.dst) == Set(src,dst))
            op match {
                case Some(p) => p
                case None => {
                    val p = new Path(src, dst)
                    paths += p
                    p
                }
            }
        }
    }

    object Space {
        var lut = Map[String,Space]()
        def apply(ident: String): Space = lut.get(ident) match {
            case Some(s) => s
            case None => {
                val s = new Space(ident)
                lut += ident -> s
                s
            }
        }
    }

    object Token {
        var lut = Map[String,Token]()
        def apply(ident: String): Token = lut.get(ident) match {
            case Some(t) => t
            case None => {
                val t = new Token(ident)
                lut += ident -> t
                t
            }
        }
    }

    object Title {
        var title: Title = null
        def apply(name: String): Title = {
            require(title == null)
            title = new Title(name)
            title
        }
    }

    object Instruction {
        var instruction: Instruction = null
        def apply(prose: String): Instruction = {
            require(instruction == null)
            instruction = new Instruction(prose)
            instruction
        }
    }

    object Details {
        def apply(prose: String) {
            if (Instruction.instruction == null) {
                Instruction.instruction = new Instruction("")
            }
            Instruction.instruction.setDetails(prose)
        }
    }
    
    object Soundtrack {
        def apply(name: String) {
            import javazoom.jl.player.Player
            import java.io._
            new Thread {
                override def run() {
                    try {
                        while(true) {
                            var is:InputStream = null
                            try { if(is == null) is = new java.net.URL(name).openStream } catch { case e => ()}
                            try { if(is == null) is = getClass().getResourceAsStream(name) } catch { case e => ()}
                            try { if(is == null) is = new FileInputStream(name) } catch { case e => ()}
                            val bis = new BufferedInputStream(is)
                            new Player(bis).play()
                            bis.close()
                        }
                    } catch {
                        case e => println("problem with soundtrack: "+e.getMessage)
                    }
                }
            }.start()
        }
    }

    class Path(val src: Space, val dst: Space) extends Visual {
        override def makeNode =
            pathLayer.place(Library.makePathGlyph(src.node, dst.node))
    }

    class Space(val ident: String) extends Visual { space =>

        override def makeNode =
            spaceLayer.placeRandomly(Library.makeSpaceGlyph(ident, Color.GREEN))

        val mouseListener = new MouseAdapter with Draggable {
            override def pressed(e: MouseEvent, n: SGNode) {
                if (e.getButton == MouseEvent.BUTTON1)
                    clickedSpace(space)
            }
        }

        node.addMouseListener(mouseListener)
        node.addNodeListener(new SGNodeListener() {
            def boundsChanged(e: SGNodeEvent) {
                for((t, s) <- tokenSpaces if s == space)
                            t.goHome()
            }
        })
    }

    class Token(val ident: String) extends Visual { token =>
        tokenSpaces += token -> outOfPlay

        override def makeNode =
            tokenLayer.placeRandomly(Library.makeTokenGlyph(ident, Color.YELLOW))

        val mouseListener = new MouseAdapter with Draggable {
            override def pressed(e: MouseEvent, n: SGNode) {
                if (e.getButton == MouseEvent.BUTTON1)
                    clickedToken(token)
            }

            override def released(e: MouseEvent, n: SGNode) {
                goHome()
            }
        }

        def setHome(space: Space) {
            tokenSpaces += token -> space
            goHome()
        }

        def goHome() {
            val home = tokenSpaces(token)
            val (sx, sy) = token.position
            val (dx, dy) = home.position
            def dist(x1:Double,y1:Double,x2:Double,y2:Double) =
                Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1))
            val range = dist(sx,sy,dx,dy)
            val radius = home.node.getBounds().getWidth/2
            if(range > radius) {
                var res = 100.0
                val rx = Math.abs(token.hashCode % res)/res-0.5
                val ry = Math.abs((token.hashCode/res)%res)/res-0.5
                val newRange = dist(0,0,rx,ry)
                createSlideTo(250, dx+radius*rx/newRange, dy+radius*ry/newRange).start()
            }
        }

        goHome()

        node.addMouseListener(mouseListener)
    }

    class Title(name: String) extends Visual {
        override def makeNode: SGNode =
            titleLayer.placeHere(Library.makeTitleGlyph(name, Color.YELLOW), width/2, height/8)
    }

    class Instruction(prose: String) extends Visual {
        var controller: Library.InstructionController = null
        val header = if(prose == "") prose else prose+"\n\n"

        override def makeNode: SGNode =
            instructionLayer.placeCentered(Library.makeInstructionGlyph(prose,{controller=_}))

        node.addMouseListener(new MouseAdapter with Draggable)

        def setDetails(s: String) {
            controller.setText(header+s)
        }
    }

    lazy val outOfPlay = Space("__outOfPlay")

    trait SpaceClickListener { def clickedSpace(s: Space) }
    trait TokenClickListener { def clickedToken(s: Token) }

    var spaceClickListeners = Set[SpaceClickListener]()
    var tokenClickListeners = Set[TokenClickListener]()

    def clickedSpace(s: Space) {
        spaceClickListeners foreach (_.clickedSpace(s))
    }

    def clickedToken(t: Token) {
        tokenClickListeners foreach (_.clickedToken(t))
    }
 }

object Visual {
    var dragon: SGNode = null
}

trait Visual {
    def makeNode: SGNode = new SGGroup()
    val node: SGNode = makeNode
    def reposition(x: Double, y: Double) {
        require(node.isInstanceOf[SGTransform.Translate])
        node.asInstanceOf[SGTransform.Translate].setTranslation(x, y)
    }

    def position: (Double,Double) = {
        require(node.isInstanceOf[SGTransform.Translate])
        val t = node.asInstanceOf[SGTransform.Translate]
        (t.getTranslateX, t.getTranslateY)
    }

    val propX = new Property[java.lang.Double] {
        def getValue: java.lang.Double = position._1
        def setValue(x: java.lang.Double) = reposition(x.doubleValue, position._2)
    }

    val propY = new Property[java.lang.Double] {
        def getValue: java.lang.Double = position._2
        def setValue(y: java.lang.Double) = reposition(position._1, y.doubleValue)
    }

    def createSlideTo(duration: Long, dx: Double, dy: Double): Animation = {
        implicit def c(d: Double): java.lang.Double = new java.lang.Double(d)
        var (sx, sy) = position
        var clipX = Clip.create[java.lang.Double](duration, propX, c(sx), c(dx))
        var clipY = Clip.create[java.lang.Double](duration, propY, c(sy), c(dy))
        val interpolator = Interpolators.getEasingInstance(0f,0.45f)
        clipX setInterpolator interpolator
        clipY setInterpolator interpolator
        val tl = new Timeline()
        tl schedule clipX
        tl schedule clipY

        tl
    }

    class MouseAdapter extends SGMouseAdapter {
        var clickOffsetX, clickOffsetY: Double = _
        def pressed(e: MouseEvent, n: SGNode) {}
        def dragged(e: MouseEvent, n: SGNode) {}
        def released(e: MouseEvent, n: SGNode) {}
        override def mousePressed(e: MouseEvent, n: SGNode) {
            if(Visual.dragon == null) {
                Visual.dragon = n
                clickOffsetX = e.getX-n.getBounds().getCenterX
                clickOffsetY = e.getY-n.getBounds().getCenterY
                if (e.getButton == MouseEvent.BUTTON1)
                    pressed(e, n)
            }
        }

        override def mouseDragged(e: MouseEvent, n: SGNode) {
            if (Visual.dragon == n)
                dragged(e, n)
        }

        override def mouseReleased(e: MouseEvent, n: SGNode) {
            if(Visual.dragon == n) {
                Visual.dragon = null
                released(e, n)
            }
        }
    }

    trait Draggable extends MouseAdapter {
        override def dragged(e: MouseEvent, n: SGNode) {
            var bits = InputEvent.META_MASK
            if((e.getModifiers & bits) == bits)
                reposition(e.getPoint().getX-clickOffsetX, e.getPoint().getY-clickOffsetY)
        }
    }

}


