package voodoo.data.lock

import com.fasterxml.jackson.annotation.JsonInclude
import voodoo.data.Side

/**
 * Created by nikky on 28/03/18.
 * @author Nikky
 * @version 1.0
 */

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class LockEntry(
        @JsonInclude(JsonInclude.Include.ALWAYS)
        var provider: String = "",
        @JsonInclude(JsonInclude.Include.ALWAYS)
        var name: String = "",
        var folder: String = "mods",
        var fileName: String? = null,
        var side: Side = Side.BOTH,
        // CURSE
        var projectID: Int = -1,
        var fileID: Int = -1,
        // DIRECT
        var url: String = "",
        var useUrlTxt: Boolean = true,
        // JENKINS
        var jenkinsUrl: String = "",
        var job: String = "",
        var buildNumber: Int = -1,
        var fileNameRegex: String = ".*(?<!-sources\\.jar)(?<!-api\\.jar)(?<!-deobf\\.jar)(?<!-lib\\.jar)(?<!-slim\\.jar)$",
        // JSON
        var updateJson: String = "",
        var jsonVersion: String = "",
        // LOCAL
        var fileSrc: String = ""
)