package voodoo.data.curse

data class CurseMinecraft(
        val version: String = "",
        val modLoaders: List<CurseModLoader> = emptyList()
)