package net.minecraft.client.resources;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.InputStream;

public interface IResource extends Closeable
{
    ResourceLocation getLocation();

    InputStream getInputStream();


    String getPackName();
}