package apps.analytics

import scala.collection.JavaConversions._
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer

import java.time.LocalDate

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element, TextNode}
import org.jsoup.select.Elements

class UCIMLMeta (val url: String) {

    val doc   = Jsoup.connect(url).get()
    val title = doc.title()
    val name  = title.slice(title.indexOf(":") + 1, title.size).trim

    val elems = doc.select("table + table tr td p")
                   .listIterator()
                   .flatMap(elem => elem.textNodes().toList)
                   .map(node => node.text())
                   .toList

    val about = doc.select("p")
                   .listIterator()
                   .flatMap(elem => elem.textNodes().toList)
                   .map(node => node.text())
                   .toList
                   .filter(s => s.startsWith(":"))
                   .head
                   .substring(1)
                   .trim

    override def toString =
    {
        val summary = Array(
            ("      about", about),
            ("       size", size),
            ("  attrTypes", attrTypes),
            ("    numAttr", numAttr),
            ("       date", date),
            ("      tasks", tasks),
            ("missingVals", missingVals),
            ("    webHits", webHits)
        ) map {
            case (label, value) => "%s = %s".format(label, value)
        }
        "%s: \n\t".format(name) + summary.mkString("\n\t")
    } // toString

    def traits      = elems(0).split(',').toList.map(s => s.trim)
    def size        = elems(1).toInt
    def area        = elems(2)
    def attrTypes   = elems(3).split(',').toList.map(s => s.trim)
    def numAttr     = elems(4).toInt
    def date        = LocalDate.parse(elems(5))
    def tasks       = elems(6).split(',').toList.map(s => s.trim)
    def missingVals = if (elems(7) == "Yes") true else false
    def webHits     = elems(8).toInt

} // UCIMLMeta

object UCIML extends App {
 
    // get all the regression data sets
    val doc   = Jsoup.connect("http://archive.ics.uci.edu/ml/datasets.html?task=reg").get()
    val links = doc.select("a")
        .listIterator()
        .map(elem => elem.attributes().get("href"))
        .filter(s => s.startsWith("datasets/"))

    // given a link, create a UCIMLMeta object
    val allDatasets = links.map { link =>
        Thread.sleep(1000)
        try {
            new UCIMLMeta("http://archive.ics.uci.edu/ml/" + link)
        } catch {
            case ex: Exception => null
        } // try
    } // allDatasets

    // example datasets
    val datasets = Array(
        new UCIMLMeta("http://archive.ics.uci.edu/ml/datasets/Airfoil+Self-Noise"),
        new UCIMLMeta("http://archive.ics.uci.edu/ml/datasets/Auto+MPG"),
        new UCIMLMeta("http://archive.ics.uci.edu/ml/datasets/3D+Road+Network+%28North+Jutland%2C+Denmark%29"),
        new UCIMLMeta("http://archive.ics.uci.edu/ml/datasets/Amazon+Access+Samples")
    ) // datasets

    for (dataset <- datasets) {
        println
        println (dataset)
    } // for

} // UCIML