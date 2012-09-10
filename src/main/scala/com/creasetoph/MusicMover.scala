package main.scala.com.creasetoph

import java.io.File
import java.util.logging.{Logger, Level}
import objects.NewEntityBuilder
import scopt.immutable.OptionParser

object MusicMover {

//  val path = "/Volumes/home_server/Music/"
  val path = "/Volumes/home_server/scripts/music_mover/mp3s/"

  def main(args: Array[String]) {
    Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)
    println("***MusicMover***")
    val parser = new OptionParser[Config]("MusicMover") {
      def options = Seq(
        flag("p", "print", "print track ID3") {
          (c: Config) => c.copy(print = true)
        },
        arg("<filename>", "<filename> is the filename") {
          (v: String, c: Config) => c.copy(filename = path)
        }
      )
    }
    parser.parse(args.toSeq, Config()) map {
      config =>
        val file = new File(config.filename)
        file.exists() match {
          case true => {
            NewEntityBuilder.newBuild(file)
            if (config.print) {

            }
          }
          case false => println("File: " + config.filename + " does not exists")
        }
    }
  }
}
