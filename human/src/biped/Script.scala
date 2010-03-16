package biped

import jTrolog.engine._
import jTrolog.terms._

object Script {

    val prolog = new Prolog()
    prolog.setDynamicPredicateIndicator("timepoint/1")
    prolog.setDynamicPredicateIndicator("holds_transiently/1")
    prolog.setDynamicPredicateIndicator("happens_spontaneously/1")

    load(getClass().getResourceAsStream("runtime.pl"))

    implicit private def set[T](seq: Seq[T]): Set[T] = Set[T]() ++ seq

    def load(code: String) {
        prolog addTheory code
    }

    def load(is: java.io.InputStream) {
        load(getContents(is))
    }

    def call0(pred: String): Option[Unit] = {
        val query = pred + "."
        prolog.solve(query) success match {
            case true => Some()
            case false => None
        }
    }

    def call1(pred: String): Option[String] = {
        val query = pred + "(A)."
        val solution = prolog.solve(query)
        solution success match {
            case true => Some(solution.getBinding("A").toString)
            case false => None
        }
    }

    def setof1(pred: String): Set[String] = {
        val query = "setof(X," + pred + "(X),Xs)."
        val solution = prolog.solve(query)
        solution success match {
            case true => untree(solution.getBinding("Xs")) map (_.toString)
            case false => Set()
        }
    }

    def setof2(pred: String): Set[(String,String)] = {
        val query = "setof([X,Y]," + pred + "(X,Y),XYs)."
        val solution = prolog.solve(query)
        solution success match {
            case true => {
                    val xys = solution.getBinding("XYs")
                    for(xy <- untree(xys)) yield untree(xy) match {
                        case Seq(x,y) => (x.toString, y.toString)
                    }
            }
            case false => Set()
        }
    }

    def setof3(pred: String): Set[(String,String,String)] = {
        val query = "setof([X,Y,Z]," + pred + "(X,Y,Z),XYZs)."
        val solution = prolog.solve(query)
        solution success match {
            case true => {
                    val xyzs = solution.getBinding("XYZs")
                    for(xyz<- untree(xyzs)) yield untree(xyz) match {
                        case Seq(x,y,z) => (x.toString, y.toString, z.toString)
                    }
            }
            case false => Set()
        }
    }

    def assertHappens(functor: String, args: String*):Boolean = {
        val eventSource = functor + (args.length match {
            case 0 => ""
            case _ => args.mkString("(",",",")")
        })
        val interestingQuery = "ui_interesting(" + eventSource + ")."
        val isol = prolog.solve(interestingQuery)
        if (isol success) {
            val assertQuery = "assertz(happens_spontaneously(" + eventSource + "))."
            prolog.solve(assertQuery)
            true
        } else false
    }

    def retractallHappens() {
        prolog.solve("retractall(happens_spontaneously(_)).")
    }

    def retractallHolds() {
        prolog.solve("retractall(holds_transiently(_)).")
    }

    def incrementTimepoint() {
        prolog.solve("retract(timepoint(T)), T1 is T+1, assertz(timepoint(T1)).")
    }

    def assertallHolds(items: Set[String]) {
        items.size match {
            case 0 => ()
            case _ => {
                    val fs = items.mkString("[",",","]")
                    val query = "forall(member(F,"+fs+"), assertz(holds_transiently(F)))."
                    prolog.solve(query)
            }
        }
    }

    private def untree(term: Term): Seq[Term] = {
        var head = term
        val buf = new collection.mutable.ListBuffer[Term]
        while(head.isInstanceOf[Struct] && head != Term.emptyList) {
            val cell = head.asInstanceOf[Struct]
            buf append cell.getArg(0)
            head = cell.getArg(1)
        }
        buf.toSeq
    }

    private def getContents(is: java.io.InputStream): String = {
        import java.io._
        val reader = new BufferedReader(new InputStreamReader(is))
        val buf = new StringBuffer

        def until_null[T](maker: ()=>T)(consumer: T=>Unit) {
            val t = maker()
            if(t != null) {
                consumer(t)
                until_null(maker)(consumer)
            }
        }

        try {
            until_null(reader.readLine) { line =>
                buf.append(line+"\n")
            }
            buf.toString
        } finally {
            is.close()
        }
    }

}

