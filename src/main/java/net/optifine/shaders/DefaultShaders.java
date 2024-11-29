package net.optifine.shaders;

import com.google.common.base.Charsets;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultShaders {
   public static InputStream getResourceAsStream(String filename) {
      if (filename.endsWith("/final.vsh")) {
         return getFinalVsh();
      } else {
         return filename.endsWith("/final.fsh") ? getFinalFsh() : null;
      }
   }

   private static InputStream getFinalVsh() {
      List<String> list = new ArrayList<>();
      list.add("#version 150");
      list.add("uniform mat4 modelViewMatrix;");
      list.add("uniform mat4 projectionMatrix;");
      list.add("in vec2 vaUV0;");
      list.add("in vec3 vaPosition;");
      list.add("out vec2 texcoord;");
      list.add("void main()");
      list.add("{");
      list.add("  gl_Position = (projectionMatrix * modelViewMatrix * vec4(vaPosition, 1.0));");
      list.add("  texcoord = vec4(vaUV0, 0.0, 1.0).st;");
      list.add("}");
      String s = list.stream().collect(Collectors.joining("\n"));
      return getInputStream(s);
   }

   private static InputStream getFinalFsh() {
      List<String> list = new ArrayList<>();
      list.add("#version 150");
      list.add("uniform sampler2D colortex0;");
      list.add("in vec2 texcoord;");
      list.add("/* DRAWBUFFERS:0 */");
      list.add("out vec4 outColor0;");
      list.add("void main()");
      list.add("{");
      list.add("  outColor0 = texture(colortex0, texcoord);");
      list.add("}");
      String s = list.stream().collect(Collectors.joining("\n"));
      return getInputStream(s);
   }

   private static InputStream getInputStream(String str) {
      byte[] abyte = str.getBytes(Charsets.US_ASCII);
      return new ByteArrayInputStream(abyte);
   }
}