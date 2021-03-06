package voodoo.pack.sk

import voodoo.data.sk.Launch
import voodoo.data.UserFiles

/**
 * Created by nikky on 30/03/18.
 * @author Nikky
 */
data class SKModpack(
        var name: String,
        var title: String = "",
        var gameVersion: String,
        var features: List<SKFeatureComposite> = emptyList(),
        var userFiles: UserFiles = UserFiles(),
        var launch: Launch = Launch()
)