package com.maxrave.kotlinyoutubeextractor

class Format {
    enum class VCodec { H263, H264, MPEG4, VP8, VP9, NONE }
    enum class ACodec { MP3, AAC, VORBIS, OPUS, NONE }

    val itag: Int
    val ext: String?
    val height: Int
    val fps: Int
    val videoCodec: VCodec?
    val audioCodec: ACodec?
    val audioBitrate: Int
    val isDashContainer: Boolean
    val isHlsContent: Boolean

    internal constructor(itag: Int, ext: String?, height: Int, vCodec: VCodec?, aCodec: ACodec?, audioBitrate: Int, isDashContainer: Boolean) {
        this.itag = itag
        this.ext = ext
        this.height = height
        this.fps = 30
        this.videoCodec = vCodec
        this.audioCodec = aCodec
        this.audioBitrate = audioBitrate
        this.isDashContainer = isDashContainer
        this.isHlsContent = false
    }

    internal constructor(itag: Int, ext: String?, height: Int, vCodec: VCodec?, aCodec: ACodec?, isDashContainer: Boolean) 
        : this(itag, ext, height, vCodec, aCodec, -1, isDashContainer)

    internal constructor(itag: Int, ext: String?, vCodec: VCodec?, aCodec: ACodec?, audioBitrate: Int, isDashContainer: Boolean) 
        : this(itag, ext, -1, vCodec, aCodec, audioBitrate, isDashContainer)

    internal constructor(itag: Int, ext: String?, height: Int, vCodec: VCodec?, aCodec: ACodec?, audioBitrate: Int, isDashContainer: Boolean, isHlsContent: Boolean) 
        : this(itag, ext, height, vCodec, aCodec, audioBitrate, isDashContainer) {
        // Para HLS setamos o boolean após o construtor primário
    }

    internal constructor(itag: Int, ext: String?, height: Int, vCodec: VCodec?, fps: Int, aCodec: ACodec?, isDashContainer: Boolean) 
        : this(itag, ext, height, vCodec, aCodec, -1, isDashContainer)
}
