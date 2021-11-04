

class RenderSurface(var width:Int, var height:Int) {
    fun setSize(w:Int, h:Int) {
        width = w
        height = h
    }
}

interface RenderCommands {
    fun prerenderInit(surface: RenderSurface)
    fun invoke(surface: RenderSurface, progression:Float)

    companion object {
        fun getByClassName(name: String): RenderCommands? {
            return RenderCommands::class.java.classLoader.loadClass(name).constructors[0].newInstance() as? RenderCommands
        }
    }
}

fun getExampleRenderCommands(className:String): RenderCommands =
    RenderCommands.getByClassName(className) ?: throw RuntimeException("Unknown example render name")
