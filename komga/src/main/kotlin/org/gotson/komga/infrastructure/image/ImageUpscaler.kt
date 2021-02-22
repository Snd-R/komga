package org.gotson.komga.infrastructure.image

interface ImageUpscaler {

  fun upscale(imageData: ByteArray): ByteArray
}
