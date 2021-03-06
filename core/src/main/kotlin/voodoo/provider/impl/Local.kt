package voodoo.provider.impl

import mu.KLogging
import voodoo.data.flat.Entry
import voodoo.data.flat.ModPack
import voodoo.data.lock.LockEntry
import voodoo.provider.ProviderBase
import java.io.File

/**
 * Created by nikky on 30/12/17.
 * @author Nikky
 */

object LocalProviderThing : ProviderBase, KLogging() {
    override val name = "Local Provider"

    override fun resolve(entry: Entry, modpack: ModPack, addEntry: (Entry) -> Unit): LockEntry {
        return LockEntry(
                provider = entry.provider,
                name = entry.name,
                folder = entry.folder,
                side = entry.side,
                fileSrc = entry.fileSrc
        )
    }

    override fun download(entry: LockEntry, targetFolder: File, cacheDir: File): Pair<String?, File> {
        val fileSrc = File(entry.parent.localDir, entry.fileSrc)
        val targetFile = targetFolder.resolve(fileSrc.name)
        fileSrc.copyTo(targetFile, overwrite = true)
        return Pair(null, targetFile)
    }

    override fun getVersion(entry: LockEntry): String {
        return entry.fileSrc.substringBeforeLast('.').substringAfterLast('/')
    }
}
