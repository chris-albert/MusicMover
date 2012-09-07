package main.scala.com.creasetoph

import java.io.File
import java.util.logging.{Logger, Level}
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.id3.ID3v24Frames
import scala.com.creasetoph.Config
import scopt.immutable.OptionParser
import scala.collection.JavaConversions._
import main.scala.com.creasetoph.Config
import main.scala.com.creasetoph.Config

object MusicMover {

  def main(args: Array[String]) {
    Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)
    println("***MusicMover***")
    val parser = new OptionParser[Config]("MusicMover") {
      def options = Seq(
        flag("p", "print", "print track ID3") {
          (c: Config) => c.copy(print = true)
        },
        arg("<filename>", "<filename> is the filename") {
          (v: String, c: Config) => c.copy(filename = v)
        }
      )
    }
    parser.parse(args.toSeq, Config()) map {
      config =>
        val file = new File(config.filename)
        file.exists() match {
          case true => {
            if (config.print) {
              //            processAlbumDir(file,stringifyID3v2)
              printAlbumDir(file)
            }
          }
          case false => println("File: " + config.filename + " does not exists")
        }
    }
  }

  def processAlbumDir(file: File, f: (File) => Unit) {
    file.isDirectory match {
      case false => println("File: " + file.getName + " is not a directory")
      case true => {
        val tracks = file.listFiles.filter(_.getName.matches( """.*\.mp3"""))
        tracks.size match {
          case 0 => println("Directory: " + file.getName + " has no mp3's in it")
          case _ => {
            tracks.foreach(f)
          }
        }
      }
    }
  }

  def printAlbumDir(file: File) {
    file.isDirectory match {
      case false => println("File: " + file.getName + " is not a directory")
      case true => {
        val tracks = file.listFiles.filter(_.getName.matches( """.*\.mp3"""))
        tracks.size match {
          case 0 => println("Directory: " + file.getName + " has no mp3's in it")
          case _ => {
            var artist = ""
            var album = ""
            var year = ""
            var trackss: List[String] = List()
            tracks.foreach(file => {
              val tag = getID3v2Tag(file)
              var tmp = tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST)
              if (tmp != "" && artist != tmp) {
                artist = tmp
              }
              tmp = tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM)
              if (tmp != "" && album != tmp) {
                album = tmp
              }
              tmp = tag.getFirst(ID3v24Frames.FRAME_ID_YEAR)
              if (tmp != "" && year != tmp) {
                year = tmp
              }
              trackss ::= "  [" + tag.getFirst(ID3v24Frames.FRAME_ID_TRACK) + "] - " + tag.getFirst(ID3v24Frames.FRAME_ID_TITLE)

            })
            println("size:  " + trackss.size)
            val ts = trackss.mkString("\n")
            val out = "Dir: " + file.getAbsolutePath + "\n" +
              "Artist: " + artist + "\n" +
              "Album : " + album + "\n" +
              "Year  : " + year + "\n" +
              "Tracks:\n" + ts + "\n"
            println(out)
          }
        }
      }
    }
  }

  def getID3v2Tag(file: File) = {
    AudioFileIO.read(file).asInstanceOf[MP3File] match {
      case mp3File: MP3File => mp3File.hasID3v2Tag match {
        case true => {
          mp3File.getID3v2TagAsv24
        }
      }
    }
  }

  def stringifyID3v2(file: File) {
    AudioFileIO.read(file).asInstanceOf[MP3File] match {
      case mp3File: MP3File => mp3File.hasID3v2Tag match {
        case true => {
          val tag = mp3File.getID3v2TagAsv24
          println(file.getName + ": \n" +
            "    Aritist: " + tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST) + "\n" +
            "      Album: " + tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM) + "\n" +
            "      Title: " + tag.getFirst(ID3v24Frames.FRAME_ID_TITLE) + "\n" +
            "       Year: " + tag.getFirst(ID3v24Frames.FRAME_ID_YEAR) + "\n" +
            "  Track Num: " + tag.getFirst(ID3v24Frames.FRAME_ID_TRACK) + "\n" +
            "\n"
          )
        }
      }
    }
  }
}
