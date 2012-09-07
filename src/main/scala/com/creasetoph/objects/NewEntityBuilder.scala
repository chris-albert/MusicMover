package main.scala.com.creasetoph.objects

import java.io.File
import collection.mutable.ArrayBuffer
import collection.mutable

/**
 *
 */
object NewEntityBuilder{

  def build(file: File) {
    val files = getFileTree(file).filter(!_.isHidden)
    val tracks = files.filter(_.isFile).map(track => new Track(track))
//    println(tracks.mkString("\n"))
    val library = new Library
    tracks.foreach(track => {
      val artist = Artist(track.getArtist)
      val album = Album(track.getAlbum)
      album.addTrack(track)
      artist.addAlbum(album)
      library.addArtist(artist)
    })
    println(library.toString)
  }

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree) else Stream.empty)
}

case class Track(file: File) {
  override def toString = file.getName
  def getAlbum = file.getParentFile
  def getArtist = file.getParentFile.getParentFile
}

case class Album(file: File) {
  private val tracks = mutable.Set[Track]()
  def addTrack(track: Track) {tracks += track}
  override def toString = tracks.mkString("\n")
}

case class Artist(file: File) {
  private val albums = mutable.Set[Album]()
  def addAlbum(album: Album) {albums += album}
  override def toString = albums.mkString("\n")
}

class Library() {
  private val artists = mutable.Set[Artist]()
  def addArtist(artist: Artist) {artists += artist}
  override def toString = artists.mkString("\n")
}
