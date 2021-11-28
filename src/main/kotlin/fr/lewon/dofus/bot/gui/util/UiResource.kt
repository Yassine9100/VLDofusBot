package fr.lewon.dofus.bot.gui.util

enum class UiResource(path: String, filledPath: String) {

    EDIT("/icon/ui/edit.png", "/icon/ui/edit_filled.png"),
    DELETE("/icon/ui/delete.png", "/icon/ui/delete_filled.png"),
    STOP("/icon/ui/stop.png", "/icon/ui/stop_filled.png"),
    PLAY_ARROW("/icon/ui/play_arrow.png", "/icon/ui/play_arrow_filled.png");

    val imageData = javaClass.getResourceAsStream(path)?.readAllBytes()
        ?: error("Couldn't find image [$path]")
    val filledImageData = javaClass.getResourceAsStream(filledPath)?.readAllBytes()
        ?: error("Couldn't find filled image [$filledPath]")

}