//package main.scala.com.creasetoph.objects
//
//import java.io.File
//import org.jaudiotagger.tag.id3.ID3v24Frames
//import collection.mutable.ListBuffer
//import org.jaudiotagger.audio.AudioFileIO
//import org.jaudiotagger.audio.mp3.MP3File
//
//object EntityBuilder {
//
//  def build(file: File) {
//    println(buildFromArtists(file).toList.mkString("\n"))
//  }
//
//  def buildFromTracks(file: File) = {
//    val album = new Album(file)
//    val artist = new Artist(file.getParentFile)
//    processDir(file,{file =>
//      val id3 = getID3v2Tag(file)
//      val track = new Track(file)
//      track.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_TITLE),id3.getFirst(ID3v24Frames.FRAME_ID_TRACK))
//      album.addTrack(track)
//      album.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ALBUM))
//      artist.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ARTIST))
//    })
//    artist.addAlbum(album)
//    artist
//  }
//
//  def buildFromAlbums(file: File) = {
//    val artist = new Artist(file)
//    file.listFiles().foreach(albumFile => {
//      val album = new Album(albumFile)
//      processDir(albumFile,{trackFile =>
//        val id3 = getID3v2Tag(trackFile)
//        val track = new Track(trackFile)
//        track.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_TITLE),id3.getFirst(ID3v24Frames.FRAME_ID_TRACK))
//        album.addTrack(track)
//        album.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ALBUM))
//        artist.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ARTIST))
//      })
//      artist.addAlbum(album)
//    })
//    artist
//  }
//
//  def buildFromArtists(file: File) = {
//    val artists: ListBuffer[Artist] = ListBuffer()
//    file.listFiles().foreach(artistFile => {
//      if (artistFile.isDirectory) {
//        val artist = new Artist(artistFile)
//        artistFile.listFiles().foreach(albumFile => {
//          if (albumFile.isDirectory) {
//            val album = new Album(albumFile)
//            processDir(albumFile,{trackFile => {
//              val id3 = getID3v2Tag(trackFile)
//              val track = new Track(trackFile)
//              track.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_TITLE),id3.getFirst(ID3v24Frames.FRAME_ID_TRACK))
//              album.addTrack(track)
//              album.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ALBUM))
//              artist.setId3(id3.getFirst(ID3v24Frames.FRAME_ID_ARTIST))
//            }})
//            artist.addAlbum(album)
//          }
//        })
//        artists += artist
//      }
//    })
//    artists
//  }
//
//  def processDir(file: File,f: File => Unit) {
//    file.isDirectory match {
//      case false => println("File: " + file.getName + " is not a directory")
//      case true => {
//        val tracks = file.listFiles.filter(_.getName.matches( """.*\.mp3"""))
//        tracks.size match {
//          case 0 => println("Directory: " + file.getName + " has no mp3's in it")
//          case _ => {
//            tracks.foreach(f)
//          }
//        }
//      }
//    }
//  }
//
//  def getID3v2Tag(file: File) = {
//    AudioFileIO.read(file).asInstanceOf[MP3File] match {
//      case mp3File: MP3File => {
//        mp3File.hasID3v2Tag match {
//          case true => {
//            mp3File.getID3v2TagAsv24
//          }
//        }
//      }
//    }
//  }
//}