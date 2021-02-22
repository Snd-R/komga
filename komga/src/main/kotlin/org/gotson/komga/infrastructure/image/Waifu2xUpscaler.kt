package org.gotson.komga.infrastructure.image

import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

class Waifu2xUpscaler(
  private val properties: KomgaProperties
) : ImageUpscaler {

  override fun upscale(imageData: ByteArray): ByteArray {
    val inputPath = Files.createTempFile(null, "input.png")
    inputPath.toFile().deleteOnExit()

    val outputPath = Files.createTempFile(null, "output.png")
    outputPath.toFile().deleteOnExit()

    FileUtils.writeByteArrayToFile(inputPath.toFile(), imageData)

    val process = ProcessBuilder(
      properties.waifu2xNcnnVulkanPath, "-i", inputPath.toAbsolutePath().toString(), "-o", outputPath.toAbsolutePath().toString(), "-s", "2", "-n", "1"
    ).start()
    process.errorStream.bufferedReader()
      .forEachLine { logger.debug { it } }

    if (process.waitFor() != 0) {
      Files.delete(inputPath)
      Files.delete(outputPath)
      logger.error { "encountered error during upscale operation. Returning original image" }
      return imageData
    }

    val upscaled = FileUtils.readFileToByteArray(outputPath.toFile())
    Files.delete(inputPath)
    Files.delete(outputPath)
    return upscaled
  }
}
