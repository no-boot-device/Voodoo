// Generated by delombok at Sat Jul 14 04:26:21 CEST 2018
/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher.util

import java.io.PrintWriter
import java.io.StringWriter
import java.util.logging.*

class SimpleLogFormatter : Formatter() {

    override fun format(record: LogRecord): String {
        val sb = StringBuilder()
        sb.append("[").append(record.level.localizedName.toLowerCase()).append("] ").append(formatMessage(record)).append(LINE_SEPARATOR)
        if (record.thrown != null) {
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                record.thrown.printStackTrace(pw)
                pw.close()
                sb.append(sw.toString())
            } catch (e: Exception) {
            }

        }
        return sb.toString()
    }

    companion object {
        private val log = Logger.getLogger(SimpleLogFormatter::class.java.name)
        private val LINE_SEPARATOR = System.getProperty("line.separator")

        fun configureGlobalLogger() {
            val globalLogger = Logger.getLogger("")
            // Set formatter
            for (handler in globalLogger.handlers) {
                handler.formatter = SimpleLogFormatter()
            }
            // Set level
            val logLevel = System.getProperty(SimpleLogFormatter::class.java.canonicalName + ".logLevel", "INFO")
            try {
                val level = Level.parse(logLevel)
                globalLogger.level = level
            } catch (e: IllegalArgumentException) {
                log.log(Level.WARNING, "Invalid log level of $logLevel", e)
            }

        }
    }
}