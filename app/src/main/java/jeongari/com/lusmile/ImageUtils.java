package jeongari.com.lusmile;

import android.media.Image;

import java.nio.ByteBuffer;

public class ImageUtils {
    public static byte[] YUV_420_888toNV21(Image image) {
        Image.Plane yPlane = image.getPlanes()[0];
        Image.Plane uPlane = image.getPlanes()[1];
        Image.Plane vPlane = image.getPlanes()[2];

        ByteBuffer yBuffer = yPlane.getBuffer();
        ByteBuffer uBuffer = uPlane.getBuffer();
        ByteBuffer vBuffer = vPlane.getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        int position = 0;
        byte[] nv21 = new byte[ySize + (image.getWidth() * image.getHeight() / 2)];

        // Add the full y buffer to the array. If rowStride > 1, some padding may be skipped.
        for (int row = 0; row < image.getHeight(); row++) {
            yBuffer.get(nv21, position, image.getWidth());
            position += image.getWidth();
            yBuffer.position(Math.min(ySize, yBuffer.position() - image.getWidth() + yPlane.getRowStride()));
        }

        int chromaHeight = image.getHeight() / 2;
        int chromaWidth = image.getWidth() / 2;
        int chromaGap = uPlane.getRowStride() - (chromaWidth * uPlane.getPixelStride());

        // Interleave the u and v frames, filling up the rest of the buffer
        for (int row = 0; row < chromaHeight; row++) {
            for (int col = 0; col < chromaWidth; col++) {
                vBuffer.get(nv21, position++, 1);
                uBuffer.get(nv21, position++, 1);
                vBuffer.position(Math.min(vSize, vBuffer.position() - 1 + vPlane.getPixelStride()));
                uBuffer.position(Math.min(uSize, uBuffer.position() - 1 + uPlane.getPixelStride()));
            }
            vBuffer.position(Math.min(vSize, vBuffer.position() + chromaGap));
            uBuffer.position(Math.min(uSize, uBuffer.position() + chromaGap));
        }


        return nv21;
    }
}
