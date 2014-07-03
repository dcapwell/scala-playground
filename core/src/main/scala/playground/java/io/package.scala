package playground.java

import java.io.File

package object io {

  def workingDir: File = new File(".").getAbsoluteFile

  implicit class FileOpts(val self: File) extends AnyVal {
    def /(child: String) = new File(self, child)

    def children: List[File] = self.listFiles() match {
      case null => List()
      case e => e.toList
    }
    
    def ls: List[File] = {
      def walk(file: File): List[File] = {
        if(! file.isDirectory) List(file)
        else file :: file.children.flatMap(walk)
      }
      walk(self)
    }
  }
}
