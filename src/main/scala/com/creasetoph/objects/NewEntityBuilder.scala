package main.scala.com.creasetoph.objects

import java.io.File
import collection.mutable.ArrayBuffer
import collection.mutable
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.id3.ID3v24Frames

/**
 *
 */
object NewEntityBuilder{

  def build(file: File) {
    val files = getFileTree(file).filter( f => (!f.isHidden && f.getName.endsWith(".mp3")) )
    val tracks = files.filter(_.isFile).map(track => new Track(track))
    val library = new Library
    tracks.foreach(track => {
      val artist = Artist(track.getArtist)
      val album = Album(track.getAlbum)
      if(library.getArtists.contains(artist)) {
        library.getArtists.findEntry(artist) match {
          case Some(a) => {
            if(a.getAlbums.contains(album)) {
              a.getAlbums.findEntry(album) match {
                case Some(b) => b.addTrack(track)
                case None =>
              }
            }else {
              album.addTrack(track)
              a.addAlbum(album)
            }
          }
          case None =>
        }
      }else {
        album.addTrack(track)
        artist.addAlbum(album)
        library.addArtist(artist)
      }
    })
    println(library.getArtists.map(_.toString).mkString("\n"))
  }

  def newBuild(file: File) {
    val files = getFileTree(file).filter( f => (!f.isHidden && f.getName.endsWith(".mp3")) )
    files.filter(_.isFile).foreach(track => {
      println("Validating: " + Console.BLUE + track.getAbsolutePath + Console.RESET)
      val valid = Track.validate(track)
      if (!valid._1) {
        println(Console.RED + valid._2.mkString("\n") + Console.RESET)
      }
    })
  }

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree) else Stream.empty)
}

case class Track(file: File) {
  def getAlbum = file.getParentFile
  def getArtist = file.getParentFile.getParentFile
  def getName = Console.YELLOW + file.getName + Console.RESET
  override def toString = "        Track: " + getName
}

object Track {
  def validate(file: File) = {
    var valid = true
    var errors = mutable.ListBuffer[String]()
    if (getArtistByDir(file) != getArtistById3(file)) {
      errors += ("Arist by dir [" + getArtistByDir(file) + "] not equal to artist by id3[" + getArtistById3(file) + "]")
      valid = false
    }
    if (getAlbumByDir(file) != getAlbumById3(file)) {
      errors += ("Album by dir [" + getAlbumByDir(file) + "] not equal to album by id3[" + getAlbumById3(file) + "]")
      valid = false
    }
    if (getTrackNameByDir(file) != getTrackNameById3(file)) {
      errors += ("Track dir [" + getTrackNameByDir(file) + "] not equal to track by id3[" + getTrackNameById3(file) + "]")
      valid = false
      fixTrackFileName(file)
    }
    (valid,errors)
  }
  def fixTrackFileName(file: File) {
    val title = getTrackNameById3(file)
    val track = getTrackNumById3(file)
    try {
      val trackNum = String.format("%02d", int2Integer(track.toInt))
      val trackName = trackNum + "_" + title.trim.toLowerCase.replace(" ","_") + ".mp3"
      println(Console.YELLOW + "Setting new track name [" + trackName + "]" + Console.RESET)
    }catch {
      case _ => println(Console.RED + "Cant get track name from id3" + Console.RESET)
    }
  }
  def getAlbumByDir(file: File) = file.getParentFile.getName
  def getArtistByDir(file: File) = file.getParentFile.getParentFile.getName
  def getTrackNameByDir(file: File) = file.getName.replaceAll("^\\d*","").split("_").map(s => if (s.size > 0) capitalize(s) else "").mkString(" ").trim.replace(".mp3","")
  def getArtistById3(file: File) = getID3v2Tag(file).getFirst(ID3v24Frames.FRAME_ID_ARTIST)
  def getAlbumById3(file: File) = getID3v2Tag(file).getFirst(ID3v24Frames.FRAME_ID_ALBUM)
  def getTrackNameById3(file: File) = getID3v2Tag(file).getFirst(ID3v24Frames.FRAME_ID_TITLE)
  def getTrackNumById3(file: File) = getID3v2Tag(file).getFirst(ID3v24Frames.FRAME_ID_TRACK)
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
  def capitalize(s: String) = { s(0).toUpper + s.substring(1, s.length).toLowerCase }
}

case class Album(file: File) {
  private val tracks = mutable.ListBuffer[Track]()
  def addTrack(track: Track) {tracks += track}
  def getName = Console.CYAN + file.getName + Console.RESET
  override def toString = "    Album: " + getName + "\n" + tracks.mkString("\n")
}

case class Artist(file: File) {
  private val albums = mutable.HashSet[Album]()
  def addAlbum(album: Album) {albums += album}
  def getName = Console.BLUE + file.getName + Console.RESET
  def getAlbums = albums
  override def toString = "Artist: " + getName + "\n" + albums.mkString("\n") + "\n"
}

class Library() {
  private val artists = mutable.HashSet[Artist]()
  def addArtist(artist: Artist) {artists += artist}
  def getArtists = artists
  override def toString = artists.mkString("\n")
}
