/**
  * Created by saurabhg on 29/05/16.
  */
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

import org.apache.log4j.Logger
import org.apache.log4j.Level



class  VertexProperty()
case class Actor(val id:Long, val name:String) extends VertexProperty
case class Country(val id:Long, val name:String) extends VertexProperty
case class Organization(val id:Long, val name:String) extends VertexProperty

class EdgeProperty()
case class  WorksAt(val idActor:Long, val idOrganization:Long) extends  EdgeProperty
case class  LivesIn(val idActor:Long, val idCountry:Long) extends  EdgeProperty

object FirstObject extends App{

  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  def getData(csv:RDD[String]) : RDD[Array[String]] = {
    val map = csv.map(line => line.split(',').map(_.trim))
    val headers = map.first()
    val data = map.mapPartitionsWithIndex((idx, iter)=> if(idx==0) iter.drop(1) else iter)
    val filteredData = data.map(row => Array(row(0), row(1), row(3)))
    return filteredData
  }

  def createGraph(data:RDD[Array[String]]):Graph[VertexProperty, String] = {

    val country = data.map(_(0)).distinct().zipWithIndex().map(tuple => Country(id = tuple._2, name = tuple._1))
    val person = data.map(_(1)).distinct().zipWithIndex().map(tuple => Actor(id = tuple._2, name = tuple._1))
    val organization = data.map(_(2)).distinct().zipWithIndex().map(tuple => Organization(id = tuple._2, name = tuple._1))

    val countryCount = country.count()
    val personCount = person.count()
    val organizationCount = organization.count()

    val personDictionary = person.map(actor => actor.name -> actor.id).collectAsMap()
    val countryDictionary = country.map(country => country.name -> country.id).collectAsMap()
    val organizationDictionary = organization.map(organization => organization.name -> organization.id).collectAsMap()

    var vertexRDD:RDD[(VertexId, VertexProperty)] = country.map(country => (country.id, country))
    vertexRDD = vertexRDD.union(person.map(person => (person.id+countryCount, person)))
    vertexRDD = vertexRDD.union(organization.map(organization => (organization.id+countryCount+personCount, organization)))

    val edgeRDD = data.flatMap{ array =>
      val aaId = personDictionary(array(1))
      val ccId = countryDictionary(array(0))
      val ooId = organizationDictionary(array(2))
      Edge(aaId+countryCount, ooId+countryCount+personCount, "worksAt")::Edge(aaId+countryCount, ccId, "livesIn")::Nil
    }

    val graph = Graph(vertexRDD, edgeRDD)
    return graph
  }

  def getActorsForOrganization(graph:Graph[VertexProperty, String], ooName:String, graphActorsMap:scala.collection.Map[Long, VertexProperty]):RDD[VertexProperty]={

    val ooId = graph.vertices.filter{
      case (vertexId, Organization(id, name)) => name == ooName
      case _ => false
    }.first()._1

    val actorsId = graph.edges.filter{
      case Edge(acId, oId, relation) => ooId == oId
    }.map(edge => edge.srcId)

    val validActors = actorsId.map(id => graphActorsMap(id))

    return validActors
  }

  val conf = new SparkConf().setMaster("local").setAppName("saurabh first app")
  val sc = new SparkContext(conf)

  println()
  println()

  val timestamp1: Long = System.currentTimeMillis

  val csv = sc.textFile("terrorist_data_all.csv")
  val filteredData = getData(csv)
  val graph = createGraph(filteredData)
  val graphActorsMap = graph.vertices.map{
    case (vertexId, a) => vertexId->a
  }.collectAsMap()
  val timestamp2: Long = System.currentTimeMillis

  val validActors = getActorsForOrganization(graph, "Pashtun", graphActorsMap)
  val timestamp3: Long = System.currentTimeMillis

  println(validActors.count())
  validActors.foreach(println _)
  println()

  println(timestamp2-timestamp1)
  println(timestamp3-timestamp2)
  println()
  println()
  println()
}


/*
object FirstObject extends App{
  val conf = new SparkConf().setMaster("local").setAppName("saurabh first app")
  val sc = new SparkContext(conf)
  val input = sc.textFile("file.py")
  val splitWords = input.flatMap(line => line.split(" "))
  val count = splitWords.count()
  print("hello it works\n")
  print(count)
  print("\n\n\n")
}
*/