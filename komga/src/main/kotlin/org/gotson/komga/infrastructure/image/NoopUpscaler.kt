package org.gotson.komga.infrastructure.image

class NoopUpscaler : ImageUpscaler {
  override fun upscale(imageData: ByteArray): ByteArray {
    return imageData
  }
}
