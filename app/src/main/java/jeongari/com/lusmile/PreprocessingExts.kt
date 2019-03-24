package jeongari.com.lusmile

fun encodeYV12(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
    val frameSize = width * height
    var uIndex = frameSize
    var vIndex = frameSize + (frameSize / 4)
    var yIndex = 0
    var index = 0
    for (j in 0 until height) {
        for (i in 0 until width) {
            val a = (argb[index] and -0x1000000) shr 24 // a is not used obviously
            val R = (argb[index] and 0xff0000) shr 16
            val G = (argb[index] and 0xff00) shr 8
            val B = (argb[index] and 0xff) shr 0
            // well known RGB to YUV algorithm
            val Y = ((66 * R + 129 * G + 25 * B + 128) shr 8) + 16
            val U = ((-38 * R - 74 * G + 112 * B + 128) shr 8) + 128
            val V = ((112 * R - 94 * G - 18 * B + 128) shr 8) + 128
            // YV12 has a plane of Y and two chroma plans (U, V) planes each sampled by a factor of 2
            // meaning for every 4 Y pixels there are 1 V and 1 U. Note the sampling is every other
            // pixel AND every other scanline.
            yuv420sp[yIndex++] = (if ((Y < 0)) 0 else (if ((Y > 255)) 255 else Y)).toByte()
            if (j % 2 == 0 && index % 2 == 0) {
                yuv420sp[uIndex++] = (if ((V < 0)) 0 else (if ((V > 255)) 255 else V)).toByte()
                yuv420sp[vIndex++] = (if ((U < 0)) 0 else (if ((U > 255)) 255 else U)).toByte()
            }
            index++
        }
    }

}