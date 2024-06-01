package verify

import dev.zenhao.melon.Melon
import dev.zenhao.melon.utils.math.RandomUtil.nextInt
import verify.HWIDManager.encryptedHWID
import java.io.File
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ClassGenerator {
    private fun getJarFile(): File {
        val fileName = this.javaClass.protectionDomain.codeSource.location.toURI().path
        if (fileName.startsWith("/")) {
            fileName.replaceFirst("/", "")
        }
        return File(fileName)
    }

    fun dumpJar(): Boolean {
        val url = Melon::class.java.protectionDomain.codeSource.location
        val path = Paths.get(url.toURI())
        val file = path.toFile()

        val originJar = ZipFile(path.toFile())
        val tmpJar = File.createTempFile("tempFile", ".tmp")

        // add check file to cancel because it will always rewrite origin jar

        // write to tmp jar cause cannot modify origin jar will error
        ZipOutputStream(tmpJar.outputStream()).use { outputStream ->
            outputStream.copyFrom(file)

            val ze = originJar.getEntry("kotlin/math/MathVKt")
            if (ze != null) {
                originJar.getInputStream(ze).use { inputStream ->
                    inputStream.reader().use {
                        println("Token exist: " + it.readText())
                    }
                }
            } else {
                println("Token not exist generate.")
                val zipEntry = ZipEntry("kotlin/math/MathVKt")
                outputStream.putNextEntry(zipEntry)
                val token = java.lang.String(encryptedHWID().toString()).getBytes("UTF-8")
                outputStream.write(token)
                outputStream.closeEntry()
            }
        }

        tmpJar.inputStream().use { it.copyTo(file.outputStream()) }
        tmpJar.delete()
        return true
    }

    private fun ZipOutputStream.copyFrom(file: File) {
        val jar = ZipFile(file)
        jar.stream().forEach {
            this.putNextEntry(it)
            if (!it.isDirectory) jar.getInputStream(it).copyTo(this)
            this.closeEntry()
        }
    }

    //返回真数是True 非负数则是False
    fun scanHWID(): Int {
        return if (getJarFile().readText().contains(encryptedHWID().toString())) {
            nextInt(1, Int.MAX_VALUE)
        } else {
            nextInt(-1, Int.MIN_VALUE)
        }
    }
}