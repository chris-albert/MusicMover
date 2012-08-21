import java.io.File
import java.util.logging.{Logger, Level}
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.id3.{ID3v24Frames, ID3v1Tag}

object MusicMover {

  val usage =
    """
      |Usage: MusicMover [-p] <filename>
    """.stripMargin

  def main(args: Array[String]) {
    Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)
    println("***MusicMover***")
    args match {
      case Array() => println(usage)
      case _ => {
        val filename = args(args.size - 1)
        val file = new File(filename)
        file.exists() match {
          case true => {

            processAlbumDir(file,printID3v2)
          }
          case false => println("File: " + filename + " does not exists")
        }
      }
    }
  }

  def processAlbumDir(file: File,f: (File) => Unit) {
    file.isDirectory match {
      case false => println("File: " + file.getName + " is not a directory")
      case true => {
        val tracks = file.listFiles.filter(_.getName.matches(""".*\.mp3"""))
        tracks.size match {
          case 0 => println("Directory: " + file.getName + " has no mp3's in it")
          case _ => {
            tracks.foreach(f)
          }
        }
      }
    }
  }

  def printID3v2(file: File) {
    val mp3File: MP3File = AudioFileIO.read(file).asInstanceOf[MP3File]
    if (mp3File.hasID3v2Tag) {
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