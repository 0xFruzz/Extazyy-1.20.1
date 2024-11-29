package net.minecraft.network.syncher;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.util.BiomeUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class SynchedEntityData {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Object2IntMap<Class<? extends Entity>> ENTITY_ID_POOL = new Object2IntOpenHashMap<>();
   private static final int MAX_ID_VALUE = 254;
   private final Entity entity;
   private final Int2ObjectMap<SynchedEntityData.DataItem<?>> itemsById = new Int2ObjectOpenHashMap<>();
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private boolean isDirty;
   public Biome spawnBiome = BiomeUtils.PLAINS;
   public BlockPos spawnPosition = BlockPos.ZERO;
   public BlockState blockStateOn = Blocks.AIR.defaultBlockState();
   public long blockStateOnUpdateMs = 0L;
   public Map<String, Object> modelVariables;
   public CompoundTag nbtTag;
   public long nbtTagUpdateMs = 0L;

   public SynchedEntityData(Entity pEntity) {
      this.entity = pEntity;
   }

   public static <T> EntityDataAccessor<T> defineId(Class<? extends Entity> pClazz, EntityDataSerializer<T> pSerializer) {
      if (LOGGER.isDebugEnabled()) {
         try {
            Class<?> oclass = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
            if (!oclass.equals(pClazz)) {
               LOGGER.debug("defineId called for: {} from {}", pClazz, oclass, new RuntimeException());
            }
         } catch (ClassNotFoundException classnotfoundexception) {
         }
      }

      int j;
      if (ENTITY_ID_POOL.containsKey(pClazz)) {
         j = ENTITY_ID_POOL.getInt(pClazz) + 1;
      } else {
         int i = 0;
         Class<?> oclass1 = pClazz;

         while(oclass1 != Entity.class) {
            oclass1 = oclass1.getSuperclass();
            if (ENTITY_ID_POOL.containsKey(oclass1)) {
               i = ENTITY_ID_POOL.getInt(oclass1) + 1;
               break;
            }
         }

         j = i;
      }

      if (j > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + j + "! (Max is 254)");
      } else {
         ENTITY_ID_POOL.put(pClazz, j);
         return pSerializer.createAccessor(j);
      }
   }

   public <T> void define(EntityDataAccessor<T> pKey, T pValue) {
      int i = pKey.getId();
      if (i > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
      } else if (this.itemsById.containsKey(i)) {
         throw new IllegalArgumentException("Duplicate id value for " + i + "!");
      } else if (EntityDataSerializers.getSerializedId(pKey.getSerializer()) < 0) {
         throw new IllegalArgumentException("Unregistered serializer " + pKey.getSerializer() + " for " + i + "!");
      } else {
         this.createDataItem(pKey, pValue);
      }
   }

   private <T> void createDataItem(EntityDataAccessor<T> pKey, T pValue) {
      SynchedEntityData.DataItem<T> dataitem = new SynchedEntityData.DataItem<>(pKey, pValue);
      synchronized(this.lock) {
         this.itemsById.put(pKey.getId(), dataitem);
      }
   }

   public <T> boolean hasItem(EntityDataAccessor<T> pKey) {
      return this.itemsById.containsKey(pKey.getId());
   }

   private <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> pKey) {
      synchronized(this.lock) {
         SynchedEntityData.DataItem<T> dataitem;
         try {
            dataitem = (SynchedEntityData.DataItem<T>) this.itemsById.get(pKey.getId());
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting synched entity data");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Synched entity data");
            crashreportcategory.setDetail("Data ID", pKey);
            throw new ReportedException(crashreport);
         } finally {
            ;
         }

         return dataitem;
      }
   }

   public <T> T get(EntityDataAccessor<T> pKey) {
      return this.getItem(pKey).getValue();
   }

   public <T> void set(EntityDataAccessor<T> pKey, T pValue) {
      this.set(pKey, pValue, false);
   }

   public <T> void set(EntityDataAccessor<T> pKey, T pValue, boolean pForce) {
      SynchedEntityData.DataItem<T> dataitem = this.getItem(pKey);
      if (pForce || ObjectUtils.notEqual(pValue, dataitem.getValue())) {
         dataitem.setValue(pValue);
         this.entity.onSyncedDataUpdated(pKey);
         dataitem.setDirty(true);
         this.isDirty = true;
         this.nbtTag = null;
      }

   }

   public boolean isDirty() {
      return this.isDirty;
   }

   @Nullable
   public List<SynchedEntityData.DataValue<?>> packDirty() {
      List<SynchedEntityData.DataValue<?>> list = null;
      if (this.isDirty) {
         synchronized(this.lock) {
            for(SynchedEntityData.DataItem<?> dataitem : this.itemsById.values()) {
               if (dataitem.isDirty()) {
                  dataitem.setDirty(false);
                  if (list == null) {
                     list = new ArrayList<>();
                  }

                  list.add(dataitem.value());
               }
            }
         }
      }

      this.isDirty = false;
      return list;
   }

   @Nullable
   public List<SynchedEntityData.DataValue<?>> getNonDefaultValues() {
      List<SynchedEntityData.DataValue<?>> list = null;
      synchronized(this.lock) {
         for(SynchedEntityData.DataItem<?> dataitem : this.itemsById.values()) {
            if (!dataitem.isSetToDefault()) {
               if (list == null) {
                  list = new ArrayList<>();
               }

               list.add(dataitem.value());
            }
         }

         return list;
      }
   }

   public void assignValues(List<SynchedEntityData.DataValue<?>> pEntries) {
      synchronized(this.lock) {
         for(SynchedEntityData.DataValue<?> datavalue : pEntries) {
            SynchedEntityData.DataItem<?> dataitem = this.itemsById.get(datavalue.id);
            if (dataitem != null) {
               this.assignValue(dataitem, datavalue);
               this.entity.onSyncedDataUpdated(dataitem.getAccessor());
               this.nbtTag = null;
            }
         }
      }

      this.entity.onSyncedDataUpdated(pEntries);
   }

   private <T> void assignValue(SynchedEntityData.DataItem<T> pTarget, SynchedEntityData.DataValue<?> pEntry) {
      if (!Objects.equals(pEntry.serializer(), pTarget.accessor.getSerializer())) {
         throw new IllegalStateException(String.format(Locale.ROOT, "Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)", pTarget.accessor.getId(), this.entity, pTarget.value, pTarget.value.getClass(), pEntry.value, pEntry.value.getClass()));
      } else {
         pTarget.setValue((T) pEntry.value);
      }
   }

   public boolean isEmpty() {
      return this.itemsById.isEmpty();
   }

   public static class DataItem<T> {
      final EntityDataAccessor<T> accessor;
      T value;
      private final T initialValue;
      private boolean dirty;

      public DataItem(EntityDataAccessor<T> pAccessor, T pValue) {
         this.accessor = pAccessor;
         this.initialValue = pValue;
         this.value = pValue;
      }

      public EntityDataAccessor<T> getAccessor() {
         return this.accessor;
      }

      public void setValue(T pValue) {
         this.value = pValue;
      }

      public T getValue() {
         return this.value;
      }

      public boolean isDirty() {
         return this.dirty;
      }

      public void setDirty(boolean pDirty) {
         this.dirty = pDirty;
      }

      public boolean isSetToDefault() {
         return this.initialValue.equals(this.value);
      }

      public SynchedEntityData.DataValue<T> value() {
         return SynchedEntityData.DataValue.create(this.accessor, this.value);
      }
   }

   public static record DataValue<T>(int id, EntityDataSerializer<T> serializer, T value) {

      public static <T> SynchedEntityData.DataValue<T> create(EntityDataAccessor<T> pDataAccessor, T pValue) {
         EntityDataSerializer<T> entitydataserializer = pDataAccessor.getSerializer();
         return new SynchedEntityData.DataValue<>(pDataAccessor.getId(), entitydataserializer, entitydataserializer.copy(pValue));
      }

      public void write(FriendlyByteBuf pBuffer) {
         int i = EntityDataSerializers.getSerializedId(this.serializer);
         if (i < 0) {
            throw new EncoderException("Unknown serializer type " + this.serializer);
         } else {
            pBuffer.writeByte(this.id);
            pBuffer.writeVarInt(i);
            this.serializer.write(pBuffer, this.value);
         }
      }

      public static SynchedEntityData.DataValue<?> read(FriendlyByteBuf pBuffer, int pId) {
         int i = pBuffer.readVarInt();
         EntityDataSerializer<?> entitydataserializer = EntityDataSerializers.getSerializer(i);
         if (entitydataserializer == null) {
            throw new DecoderException("Unknown serializer type " + i);
         } else {
            return read(pBuffer, pId, entitydataserializer);
         }
      }

      private static <T> SynchedEntityData.DataValue<T> read(FriendlyByteBuf pBuffer, int pId, EntityDataSerializer<T> pSerializer) {
         return new SynchedEntityData.DataValue<>(pId, pSerializer, pSerializer.read(pBuffer));
      }

      public int id() {
         return this.id;
      }

      public EntityDataSerializer<T> serializer() {
         return this.serializer;
      }

      public T value() {
         return this.value;
      }
   }
}
