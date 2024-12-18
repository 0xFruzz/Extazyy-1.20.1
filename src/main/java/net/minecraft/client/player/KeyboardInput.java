package net.minecraft.client.player;

import net.minecraft.client.Options;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.fruzz.extazyy.misc.event.EventManager;
import ru.fruzz.extazyy.misc.event.events.impl.EventKeyboardInput;

@OnlyIn(Dist.CLIENT)
public class KeyboardInput extends Input {
   private final Options options;

   public KeyboardInput(Options pOptions) {
      this.options = pOptions;
   }

   private static float calculateImpulse(boolean pInput, boolean pOtherInput) {
      if (pInput == pOtherInput) {
         return 0.0F;
      } else {
         return pInput ? 1.0F : -1.0F;
      }
   }

   public void tick(boolean pIsSneaking, float pSneakingSpeedMultiplier) {

      this.up = this.options.keyUp.isDown();
      this.down = this.options.keyDown.isDown();
      this.left = this.options.keyLeft.isDown();
      this.right = this.options.keyRight.isDown();
      this.forwardImpulse = calculateImpulse(this.up, this.down);
      this.leftImpulse = calculateImpulse(this.left, this.right);
      this.jumping = this.options.keyJump.isDown();
      this.shiftKeyDown = this.options.keyShift.isDown();

      EventKeyboardInput moveInputEvent = new EventKeyboardInput(forwardImpulse, leftImpulse, jumping, shiftKeyDown, 0.3D);
      EventManager.call(moveInputEvent);


      final double sneakMultiplier = moveInputEvent.getSneakSlowDownMultiplier();
      this.forwardImpulse = moveInputEvent.getForward();
      this.leftImpulse = moveInputEvent.getStrafe();
      this.jumping = moveInputEvent.isJump();
      this.shiftKeyDown = moveInputEvent.isSneak();

      if (pIsSneaking) {
         this.leftImpulse = (float) (this.leftImpulse * sneakMultiplier);
         this.forwardImpulse = (float)((double)this.forwardImpulse * sneakMultiplier);
      }

   }
}