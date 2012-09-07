package main.scala.com.creasetoph.objects

import java.io.File
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.id3.{ID3v24Frames, ID3v23Frames}
import collection.mutable.ListBuffer


object EntityBuilder {

  def build(file: File) {
    println(buildFromArtists(file).toList.mkString("\n"))
  }

  def buildFromTracks(file: File) = {
    val album = new Album(file)
    val artist = new Artist(file.getParentFile)
    processDir(file,{file =>
      val id3 = getID3v2Tag(file)
      val track = new Track(file)
      track.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_TITLE),id3.getFirst(ID3v24Frames.FRAME_ID_TRACK))
      album.addTrack(track)
      album.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ALBUM))
      artist.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ARTIST))
    })
    artist.addAlbum(album)
    artist
  }

  def buildFromAlbums(file: File) = {
    val artist = new Artist(file)
    file.listFiles().foreach(albumFile => {
      val album = new Album(albumFile)
      processDir(albumFile,{trackFile =>
        val id3 = getID3v2Tag(trackFile)
        val track = new Track(trackFile)
        track.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_TITLE),id3.getFirst(ID3v24Frames.FRAME_ID_TRACK))
        album.addTrack(track)
        album.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ALBUM))
        artist.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ARTIST))
      })
      artist.addAlbum(album)
    })
    artist
  }

  def buildFromArtists(file: File) = {
    val artists: ListBuffer[Artist] = ListBuffer()
    file.listFiles().foreach(artistFile => {
      if (artistFile.isDirectory) {
        val artist = new Artist(artistFile)
        artistFile.listFiles().foreach(albumFile => {
          if (albumFile.isDirectory) {
            val album = new Album(albumFile)
            processDir(albumFile,{trackFile => {
              val id3 = getID3v2Tag(trackFile)
              val track = new Track(trackFile)
              track.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_TITLE),id3.getFirst(ID3v24Frames.FRAME_ID_TRACK))
              album.addTrack(track)
              album.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ALBUM))
              artist.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ARTIST))
            }})
            artist.addAlbum(album)
          }
        })
        artists += artist
      }
    })
    artists
  }

  def processDir(file: File,f: File => Unit) {
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

  def getID3v2Tag(file: File) = {
    AudioFileIO.read(file).asInstanceOf[MP3File] match {
      case mp3File: MP3File => {
        mp3File.hasID3v2Tag match {
          case true => {
            mp3File.getID3v2TagAsv24
          }
        }
      }
    }
  }
}

class Entity(val file: File) {

  val name: String = file.getName
  val path: String = file.getAbsolutePath
  var errors: List[String] = List()

  override def toString = {
    "Path: " + file.getAbsolutePath + "\n" + "Name: " + file.getName + "\n"
  }

  def getErrors = {
    errors.mkString("\n")
  }
}

class Artist(file: File) extends Entity(file) {

  var id3Artist: String = ""
  var albums: List[Album] = List()

  def setId3(artist: String) {
    if (artist != id3Artist && id3Artist != "") {
      errors ::= "Arist [" + artist + "] different then set [" + id3Artist + "]"
    }
    id3Artist = artist
  }

  def addAlbum(album: Album) {
    albums ::= album
  }

  override def toString = {
    val out = new StringBuilder
    out.append(Console.WHITE).append("**Artist**\n").append(Console.RESET)
    out.append("Name       : ").append(Console.BLUE).append(name).append(Console.RESET).append("\n")
    out.append("Path       : ").append(Console.BLUE).append(path).append(Console.RESET).append("\n")
    out.append("ID3        : ").append(Console.BLUE).append(id3Artist).append(Console.RESET).append("\n")
    out.append("Album Count: ").append(Console.BLUE).append(albums.size).append(Console.RESET).append("\n")
    out.append("Errors     : ").append(Console.RED).append(errors.mkString("\n")).append(Console.RESET).append("\n")
    out.append("Albums     : \n").append(stringifyAlbums)
    out.toString()
  }

  def stringifyAlbums = {
    val out = new StringBuilder
    albums.foreach { album => out.append(album.toString("    "))}
    out
  }
}

class Album(file: File) extends Entity(file) {

   var id3Album: String = ""
  var tracks: List[Track] = List()

  def setId3(album: String)  {
    if (album != id3Album && id3Album != "") {
      errors ::= "Arist [" + album + "] different then set [" + id3Album + "]"
    }
    id3Album = album
  }

  def addTrack(track: Track) {
    tracks ::= track
  }

  def toString(prefix: String) = {
    val out = new StringBuilder
    out.append(prefix).append("Name       : ").append(Console.CYAN).append(name).append(Console.RESET).append("\n")
    out.append(prefix).append("Path       : ").append(Console.CYAN).append(path).append(Console.RESET).append("\n")
    out.append(prefix).append("ID3        : ").append(Console.CYAN).append(id3Album).append(Console.RESET).append("\n")
    out.append(prefix).append("Track Count: ").append(Console.CYAN).append(tracks.size).append(Console.RESET).append("\n")
    out.append(prefix).append("Errors     : ").append(Console.RED).append(errors.mkString("\n")).append(Console.RESET).append("\n")
    out.append(prefix).append("Tracks     : \n").append(stringifyTracks)
    out.toString()
  }

  def stringifyTracks = {
    val out = new StringBuilder
    tracks.foreach { track => out.append(track.toString("        "))}
    out
  }
}

class Track(file: File) extends Entity(file) {

  var id3Title: String = ""
  var id3Track: String = ""

  def setId3(title: String,track: String) {
    if (title == "") {
      errors ::= "Title empty"
      return
    }
    if (track == "") {
      errors ::= "Track empty"
      return
    }
    id3Title = title
    id3Track = track
    val id3Name = (String.format("%02d",int2Integer(track.toInt)) + " " + title + ".mp3").replace(" ","_").toLowerCase
    if (id3Name != name) {
      errors ::= "Track[" + name + "] doesn match id3 track [" + id3Name + "]"
    }
  }

  def toString(prefix: String) = {
    val out = new StringBuilder
    out.append(prefix).append("Name  : ").append(Console.YELLOW).append(name).append(Console.RESET).append("\n")
    out.append(prefix).append("Path  : ").append(Console.YELLOW).append(path).append(Console.RESET).append("\n")
    out.append(prefix).append("ID3   : ").append(Console.YELLOW).append(id3Title).append(Console.RESET).append("\n")
    out.append(prefix).append("ID3#  : ").append(Console.YELLOW).append(id3Track).append(Console.RESET).append("\n")
    out.append(prefix).append("Errors: ").append(Console.RED).append(errors.mkString("\n")).append(Console.RESET).append("\n")
    out
  }
}
