package com.maxrave.kotlinyoutubeextractor

data class Format(
    val itag: Int,
    val ext: String? = null,
    val height: Int = -1,
    val fps: Int = 30,
    val videoCodec: VCodec? = VCodec.NONE,
    val audioCodec: ACodec? = ACodec.NONE,
    val audioBitrate: Int = -1,
    val isDashContainer: Boolean = false,
    val isHlsContent: Boolean = false
) {
    enum class VCodec { H263, H264, MPEG4, VP8, VP9, NONE }
    enum class ACodec { MP3, AAC, VORBIS, OPUS, NONE }
        // Apenas o itag (o resto usa o padrão)
    val f1 = Format(itag = 22)

// Itag e altura
     val f2 = Format(itag = 18, height = 360)

// Especificando quase tudo, mas mantendo a ordem ou usando nomes
     val f3 = Format(
         itag = 137, 
         ext = "mp4", 
         height = 1080, 
         videoCodec = Format.VCodec.H264, 
         isDashContainer = true
     )

}
 

