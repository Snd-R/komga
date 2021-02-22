package org.gotson.komga.infrastructure.configuration

import org.gotson.komga.infrastructure.image.ImageUpscaler
import org.gotson.komga.infrastructure.image.NoopUpscaler
import org.gotson.komga.infrastructure.image.Waifu2xUpscaler
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UpscalerConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = "komga", value = ["waifu2x-ncnn-vulkan-path"])
  fun waifuUpscaler(properties: KomgaProperties): ImageUpscaler {
    return Waifu2xUpscaler(properties)
  }

  @Bean
  @ConditionalOnMissingBean(ImageUpscaler::class)
  fun noopUpscaler(): ImageUpscaler {
    return NoopUpscaler()
  }
}
