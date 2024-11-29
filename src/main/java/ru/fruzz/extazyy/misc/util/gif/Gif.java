package ru.fruzz.extazyy.misc.util.gif;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import ru.fruzz.extazyy.misc.util.Mine;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class Gif {
    static int num = 0;
    private final HashMap<Integer, ResourceLocation> frames;
    private int framesCount = 0;
    private final long frameCooldown;
    private ImageReader imageReader;
    private int[] resolution;
    private final long initTime;

    public Gif(InputStream in, long frameCooldown) {
        frames = new HashMap<>();
        this.frameCooldown = frameCooldown;
        resolution = new int[]{1, 1};
        initTime = System.currentTimeMillis();
        try {
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(in);
            if (imageInputStream != null) {
                imageReader = ImageIO.getImageReaders(imageInputStream).next();
                imageReader.setInput(imageInputStream);
                framesCount = imageReader.getNumImages(true);
                for (int i = 0; i < framesCount; i++) {
                    ResourceLocation resource = createTextureFromFrame(i);
                    frames.put(i, resource);
                }
            } else {
                throw new IOException("������ ��� �������� ImageInputStream �� �������� ������");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void init() {
        try {
            PlayerInfo.cape = new Gif(new ResourceLocation("minecraft","extazyy/gif/cape.gif"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Gif(InputStream stream) {
        this(stream, 100);
    }

    public Gif(ResourceLocation location, long frameCooldown) throws IOException {
        this(Mine.mc.getResourceManager().getResource(location).get().open(), frameCooldown);
    }

    public Gif(ResourceLocation location) throws IOException {
        this(location, 100);
    }

    private BufferedImage getFrame(int index) throws IOException {
        BufferedImage bufferedImage = imageReader.read(index);
        if (bufferedImage == null) {
            throw new IOException("������ ��� ������ ����� " + index);
        }
        return bufferedImage;
    }

    private ResourceLocation createTextureFromFrame(int index) throws IOException {
        BufferedImage frame = getFrame(index);
        if (index == 0) {
            resolution = getTextureResolution(frame);
        }
        NativeImage nativeImage = NativeImage.read(Objects.requireNonNull(convertImageToPngInputStream(frame)));
        num++;
        return Minecraft.getInstance().getTextureManager().register(num + "texture", new DynamicTexture(nativeImage));
    }

    public static int[] getTextureResolution(BufferedImage image) {
        try {
            return new int[]{image.getWidth(), image.getHeight()};
        } catch (Exception e) {
            e.printStackTrace();
            return new int[]{1, 1};
        }
    }

    public static BufferedImage img(ResourceLocation resourceLocation) {
        try {
            return ImageIO.read(Mine.mc.getResourceManager().getResource(resourceLocation).get().open());
        } catch (IOException e) {

        }
        return null;
    }


    private InputStream convertImageToPngInputStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public ResourceLocation getResourceByProcess(float process) {
        int count = (int) ((framesCount - 1) * (process / 100f));
        if (frames.containsKey(count)) {
            return frames.get(count);
        } else {
            try {
                frames.put(count, createTextureFromFrame(count));
                return frames.get(count);
            } catch (IOException e) {
                e.printStackTrace();
                return frames.get(0);
            }
        }
    }

    public ResourceLocation getResourceBySpeed(float speed) {
        int id = (int) ((double) (System.currentTimeMillis() - initTime) / (double) this.frames.size() * (double) speed) % this.frames.size();
        return frames.get(id);
    }

    public ResourceLocation getResource() {
        long elapsedTimeMillis = System.currentTimeMillis() - initTime;
        int numFrames = this.frames.size();
        int id = (int) ((elapsedTimeMillis / frameCooldown) % numFrames);
        return frames.get(id);
    }

    public int[] getResolution() {
        return resolution;
    }

    public float getScaledWidth(float scale) {
        return resolution[0] * scale;
    }

    public float getScaledHeight(float scale) {
        return resolution[1] * scale;
    }

    public int getFramesCount() {
        return framesCount;
    }

    public long getFrameCooldown() {
        return frameCooldown;
    }
}
