package biped

import java.awt._
import java.awt.geom._

import javax.swing._
import javax.swing.event._

import com.sun.scenario.scenegraph._
import com.sun.scenario.scenegraph.event._

object Library {

    implicit private def d2f(d:Double):Float = d.asInstanceOf[Float]

    private val font = new Font(Font.SANS_SERIF,Font.BOLD,16)
    private val textColor = Color.BLACK
    private val borderColor = Color.BLACK
    private val borderStroke = new BasicStroke(2)

    private def fadeColor(color: Color) = new Color(
            128 + color.getRed / 2,
            128 + color.getGreen / 2,
            128 + color.getBlue / 2)

    def makePathGlyph(src: SGNode, dst: SGNode): SGNode = {
        def coords(node:SGNode)  =
            (node.getBounds().getCenterX, node.getBounds().getCenterY)

        val w = 32

        val line = new SGShape()
        line setDrawPaint borderColor
        line setDrawStroke
            new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND)
        line setMode SGAbstractShape.Mode.STROKE
        line setAntialiasingHint RenderingHints.VALUE_ANTIALIAS_ON

        def updateEndpoints() {
            val (sx, sy) = coords(src)
            val (dx, dy) = coords(dst)
            line setShape new Line2D.Double(sx, sy, dx, dy)
        }

        updateEndpoints()

        val listener = new SGNodeListener() {
            def boundsChanged(e: SGNodeEvent) { updateEndpoints() }
        }

        src addNodeListener listener
        dst addNodeListener listener

        val composite = new SGComposite()
        composite setOpacity 0.5f
        composite setChild line

        composite
    }

    def makeTokenGlyph(name: String, color: Color): SGNode = {
        val text = new SGText()
        text setText name
        text setFillPaint textColor
        text setFont font
        text setAntialiasingHint RenderingHints.VALUE_TEXT_ANTIALIAS_ON

        val s = 20
        val w = text.getBounds().getWidth + s
        val h = text.getBounds().getHeight + s

        val rect = new SGShape()
        rect setShape new RoundRectangle2D.Double(0,0,w,h,s,s)
        rect setFillPaint new GradientPaint(0,0,Color.WHITE,0,h,fadeColor(color))
        rect setDrawPaint borderColor
        rect setDrawStroke borderStroke
        rect setMode SGAbstractShape.Mode.STROKE_FILL
        rect setAntialiasingHint RenderingHints.VALUE_ANTIALIAS_ON

        centeringTransform(rect, text)
    }

    def makeSpaceGlyph(name: String, color: Color): SGNode = {
        val text = new SGText()
        text setText name
        text setFillPaint textColor
        text setFont font
        text setAntialiasingHint RenderingHints.VALUE_TEXT_ANTIALIAS_ON

        val s = 75
        val d = (text.getBounds().getWidth) max (text.getBounds().getHeight) + s

        val rect = new SGShape()
        rect setShape new Ellipse2D.Double(0,0,d,d)
        rect setFillPaint new GradientPaint(0,0,Color.WHITE,0,d,fadeColor(color))
        rect setDrawPaint borderColor
        rect setDrawStroke borderStroke
        rect setMode SGAbstractShape.Mode.STROKE_FILL
        rect setAntialiasingHint RenderingHints.VALUE_ANTIALIAS_ON

        centeringTransform(rect, text)
    }

    def makeTitleGlyph(title: String, color: Color): SGNode = {
        val s = 150
        val titleFont = new Font(Font.SANS_SERIF, Font.BOLD, s)
        val text = new SGText()
        text setFont titleFont
        text setText title
        text setFillPaint new GradientPaint(0,s/2,fadeColor(color),0,s,Color.BLACK,true)
        text setDrawPaint borderColor
        text setDrawStroke new BasicStroke(2)
        text setMode SGAbstractShape.Mode.STROKE_FILL
        text setAntialiasingHint RenderingHints.VALUE_TEXT_ANTIALIAS_ON

        val node = centeringTransform(text)
        val composite = new SGComposite()
        composite setChild node
        composite setOpacity 0.5f

        composite
    }

    trait InstructionController {
        def setText(s: String)
    }
    def makeInstructionGlyph(prose: String, collect: InstructionController=>Unit): SGNode = {
        val textPane = new JTextPane with InstructionController
        textPane setText prose
        textPane setFont new Font(Font.SANS_SERIF, Font.PLAIN, 12)
        textPane setForeground Color.WHITE
        textPane setBackground(Color.BLACK)
        textPane setEditable false

        collect(textPane)

        val comp = new SGComponent()
        comp setComponent textPane


        val rect = new SGShape()
        rect setFillPaint Color.BLACK
        rect setDrawPaint borderColor
        rect setDrawStroke borderStroke
        rect setMode SGAbstractShape.Mode.STROKE_FILL
        rect setAntialiasingHint RenderingHints.VALUE_ANTIALIAS_ON

        def updateSize() {
            val s = 20
            val w = comp.getBounds().getWidth + s
            val h = comp.getBounds().getHeight + s
            rect setShape new RoundRectangle2D.Double(0,0,w,h,s,s)
        }

        updateSize()

        comp addNodeListener new SGNodeListener() {
            def boundsChanged(e: SGNodeEvent) { updateSize() }
        }

        val node = centeringTransform(rect, comp)
        val composite = new SGComposite()
        composite setChild node
        composite setOpacity 0.75f

        composite
    }

    def centeringTransform(node: SGNode): SGNode = {
        val b = node.getBounds()
        SGTransform.createTranslation(
            -b.getMinX - b.getWidth / 2,
            -b.getMinY - b.getHeight / 2,
            node)
    }

    def centeringTransform(nodes: SGNode*): SGNode = {
        val group = new SGGroup()
        for (node <- nodes) group.add(centeringTransform(node))
        group
    }
}
