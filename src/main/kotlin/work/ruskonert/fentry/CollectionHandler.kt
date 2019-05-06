package work.ruskonert.fentry

import java.nio.file.Paths
import java.nio.file.Path

interface CollectionHandler
{
    fun getPath() : Path {
        val currentRelativePath = Paths.get("")
        return currentRelativePath.toAbsolutePath()
    }

    fun getHandlerName() : String {
        return "GKingCollectHandler"
    }
}