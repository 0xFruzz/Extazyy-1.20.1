package net.optifine.shaders;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.optifine.Config;
import net.optifine.shaders.config.HeaderLine;
import net.optifine.shaders.config.HeaderLineFunction;
import net.optifine.shaders.config.HeaderLineText;
import net.optifine.shaders.config.HeaderLineVariable;
import net.optifine.shaders.config.ShaderPackParser;
import net.optifine.shaders.config.ShaderType;
import net.optifine.util.ArrayUtils;
import net.optifine.util.LineBuffer;
import net.optifine.util.StrUtils;

public class ShadersCompatibility {
   public static Pattern PATTERN_UNIFORM = Pattern.compile("(\\s*layout\\s*\\(.*\\)|)\\s*uniform\\s+\\w+\\s+(\\w+).*");
   public static Pattern PATTERN_IN = Pattern.compile("(\\s*layout\\s*\\(.*\\)|)\\s*in\\s+\\w+\\s+(\\w+).*");
   public static Pattern PATTERN_OUT = Pattern.compile("(\\s*layout\\s*\\(.*\\)|)\\s*out\\s+\\w+\\s+(\\w+).*");
   public static Pattern PATTERN_VARYING = Pattern.compile("\\s*varying\\s+\\w+\\s+(\\w+).*");
   public static Pattern PATTERN_CONST = Pattern.compile("\\s*const\\s+\\w+\\s+(\\w+).*");
   public static Pattern PATTERN_FUNCTION = Pattern.compile("\\s*\\w+\\s+(\\w+)\\s*\\(.*\\).*", 32);
   public static HeaderLine MODEL_VIEW_MATRIX = makeHeaderLine("uniform mat4 modelViewMatrix;");
   public static HeaderLine MODEL_VIEW_MATRIX_INVERSE = makeHeaderLine("uniform mat4 modelViewMatrixInverse;");
   public static HeaderLine PROJECTION_MATRIX = makeHeaderLine("uniform mat4 projectionMatrix;");
   public static HeaderLine PROJECTION_MATRIX_INVERSE = makeHeaderLine("uniform mat4 projectionMatrixInverse;");
   public static HeaderLine TEXTURE_MATRIX = makeHeaderLine("uniform mat4 textureMatrix = mat4(1.0);");
   public static HeaderLine NORMAL_MATRIX = makeHeaderLine("uniform mat3 normalMatrix;");
   public static HeaderLine CHUNK_OFFSET = makeHeaderLine("uniform vec3 chunkOffset;");
   public static HeaderLine ALPHA_TEST_REF = makeHeaderLine("uniform float alphaTestRef;");
   public static HeaderLine TEXTURE_MATRIX_2 = makeHeaderLine("const mat4 TEXTURE_MATRIX_2 = mat4(vec4(0.00390625, 0.0, 0.0, 0.0), vec4(0.0, 0.00390625, 0.0, 0.0), vec4(0.0, 0.0, 0.00390625, 0.0), vec4(0.03125, 0.03125, 0.03125, 1.0));");
   public static HeaderLine FTRANSORM_BASIC = makeHeaderLine(makeFtransformBasic());
   public static HeaderLine FOG_DENSITY = makeHeaderLine("uniform float fogDensity;");
   public static HeaderLine FOG_START = makeHeaderLine("uniform float fogStart;");
   public static HeaderLine FOG_END = makeHeaderLine("uniform float fogEnd;");
   public static HeaderLine FOG_COLOR = makeHeaderLine("uniform vec3 fogColor;");
   public static HeaderLine VIEW_WIDTH = makeHeaderLine("uniform float viewWidth;");
   public static HeaderLine VIEW_HEIGHT = makeHeaderLine("uniform float viewHeight;");
   public static HeaderLine RENDER_STAGE = makeHeaderLine("uniform int renderStage;");
   public static HeaderLine FOG_FRAG_COORD_OUT = makeHeaderLine("out float varFogFragCoord;");
   public static HeaderLine FOG_FRAG_COORD_IN = makeHeaderLine("in float varFogFragCoord;");
   public static HeaderLine FRONT_COLOR_OUT = makeHeaderLine("out vec4 varFrontColor;");
   public static HeaderLine FRONT_COLOR_IN = makeHeaderLine("in vec4 varFrontColor;");
   public static HeaderLine POSITION = makeHeaderLine("in vec3 vaPosition;");
   public static HeaderLine COLOR = makeHeaderLine("in vec4 vaColor;");
   public static HeaderLine UV0 = makeHeaderLine("in vec2 vaUV0;");
   public static HeaderLine UV1 = makeHeaderLine("in ivec2 vaUV1;");
   public static HeaderLine UV2 = makeHeaderLine("in ivec2 vaUV2;");
   public static HeaderLine NORMAL = makeHeaderLine("in vec3 vaNormal;");
   private static final Pattern PATTERN_VERSION = ShaderPackParser.PATTERN_VERSION;
   public static final Pattern PATTERN_EXTENSION = Pattern.compile("\\s*#\\s*extension\\s+(\\w+)(.*)");
   public static final Pattern PATTERN_LINE = Pattern.compile("\\s*#\\s*line\\s+(\\d+)\\s+(\\d+)(.*)");
   private static final Pattern PATTERN_TEXTURE2D_TEXCOORD = Pattern.compile("(.*texture(2D)?\\s*\\(\\s*(texture|colortex0)\\s*,\\s*)(\\w+)(\\s*\\).*)");
   private static final Pattern PATTERN_FRAG_DATA_SET = Pattern.compile("(\\s*)gl_FragData\\[(\\d+)\\](\\S*)\\s*=\\s*(.*)");
   private static final Pattern PATTERN_FRAG_DATA_GET = Pattern.compile("gl_FragData\\[(\\d+)\\]([^ ][^=])");
   private static final Pattern PATTERN_FRAG_DATA = Pattern.compile("gl_FragData\\[(\\d+)\\]");
   private static final String COMMENT_COMPATIBILITY = "// Compatibility (auto-generated)";

   public static LineBuffer remap(Program program, ShaderType shaderType, LineBuffer lines) {
      if (program == null) {
         return lines;
      } else {
         int i = 120;
         LineBuffer linebuffer = new LineBuffer();
         Set<HeaderLine> set = new LinkedHashSet<>();

         for(String s : lines) {
            if (s.equals("// Compatibility (auto-generated)")) {
               return lines;
            }

            if (s.trim().startsWith("//")) {
               linebuffer.add(s);
            } else {
               if (matches(s, PATTERN_VERSION)) {
                  i = Math.max(i, getVersion(s, i));
                  s = replace(s, "#version 110", "#version 150", set);
                  s = replace(s, "#version 120", "#version 150", set);
                  s = replace(s, "#version 130", "#version 150", set);
                  s = replace(s, "#version 140", "#version 150", set);
                  s = replace(s, "compatibility", "", set);
               }

               if (shaderType == ShaderType.VERTEX) {
                  if (program == Shaders.ProgramBasic) {
                     s = replace(s, Pattern.compile("(\\W)gl_ProjectionMatrix\\s*\\*\\s*gl_ModelViewMatrix\\s*\\*\\s*gl_Vertex(\\W)"), "$1ftransform()$2", set);
                     s = replace(s, Pattern.compile("(\\W)gl_ModelViewProjectionMatrix\\s*\\*\\s*gl_Vertex(\\W)"), "$1ftransform()$2", set);
                     s = replace(s, "ftransform()", "ftransformBasic()", set, RENDER_STAGE, VIEW_WIDTH, VIEW_HEIGHT, PROJECTION_MATRIX, MODEL_VIEW_MATRIX, POSITION, NORMAL, FTRANSORM_BASIC);
                  }

                  if (program.getProgramStage().isAnyComposite()) {
                     s = replace(s, "ftransform()", "(projectionMatrix * modelViewMatrix * vec4(vaPosition, 1.0))", set, PROJECTION_MATRIX, MODEL_VIEW_MATRIX, POSITION);
                     s = replace(s, "gl_Vertex", "vec4(vaPosition, 1.0)", set, POSITION);
                  } else {
                     s = replace(s, "ftransform()", "(projectionMatrix * modelViewMatrix * vec4(vaPosition + chunkOffset, 1.0))", set, PROJECTION_MATRIX, MODEL_VIEW_MATRIX, POSITION, CHUNK_OFFSET);
                     s = replace(s, "gl_Vertex", "vec4(vaPosition + chunkOffset, 1.0)", set, POSITION, CHUNK_OFFSET);
                  }

                  s = replace(s, "gl_Color", "vaColor", set, COLOR);
                  s = replace(s, "gl_Normal", "vaNormal", set, NORMAL);
                  s = replace(s, "gl_MultiTexCoord0", "vec4(vaUV0, 0.0, 1.0)", set, UV0);
                  s = replace(s, "gl_MultiTexCoord1", "vec4(vaUV1, 0.0, 1.0)", set, UV1);
                  s = replace(s, "gl_MultiTexCoord2", "vec4(vaUV2, 0.0, 1.0)", set, UV2);
                  s = replace(s, "gl_MultiTexCoord3", "vec4(0.0, 0.0, 0.0, 1.0)", set);
               }

               s = replace(s, "gl_ProjectionMatrix", "projectionMatrix", set, PROJECTION_MATRIX);
               s = replace(s, "gl_ProjectionMatrixInverse", "projectionMatrixInverse", set, PROJECTION_MATRIX_INVERSE);
               s = replace(s, "gl_ModelViewMatrix", "modelViewMatrix", set, MODEL_VIEW_MATRIX);
               s = replace(s, "gl_ModelViewMatrixInverse", "modelViewMatrixInverse", set, MODEL_VIEW_MATRIX_INVERSE);
               s = replace(s, "gl_ModelViewProjectionMatrix", "(projectionMatrix * modelViewMatrix)", set, PROJECTION_MATRIX, MODEL_VIEW_MATRIX);
               s = replace(s, "gl_NormalMatrix", "normalMatrix", set, NORMAL_MATRIX);
               if (shaderType == ShaderType.VERTEX) {
                  s = replace(s, "attribute", "in", set);
                  s = replace(s, "varying", "out", set);
                  s = replace(s, "gl_FogFragCoord", "varFogFragCoord", set, FOG_FRAG_COORD_OUT);
                  s = replace(s, "gl_FrontColor", "varFrontColor", set, FRONT_COLOR_OUT);
               }

               if (shaderType == ShaderType.GEOMETRY) {
                  s = replace(s, "varying in", "in", set);
                  s = replace(s, "varying out", "out", set);
               }

               if (shaderType == ShaderType.FRAGMENT) {
                  s = replace(s, "varying", "in", set);
                  s = replace(s, "gl_FogFragCoord", "varFogFragCoord", set, FOG_FRAG_COORD_IN);
                  s = replace(s, "gl_FrontColor", "varFrontColor", set, FRONT_COLOR_IN);
               }

               s = replace(s, "gl_TextureMatrix[0]", "textureMatrix", set, TEXTURE_MATRIX);
               s = replace(s, "gl_TextureMatrix[1]", "mat4(1.0)", set);
               s = replace(s, "gl_TextureMatrix[2]", "TEXTURE_MATRIX_2", set, TEXTURE_MATRIX_2);
               s = replace(s, "gl_Fog.density", "fogDensity", set, FOG_DENSITY);
               s = replace(s, "gl_Fog.start", "fogStart", set, FOG_START);
               s = replace(s, "gl_Fog.end", "fogEnd", set, FOG_END);
               s = replace(s, "gl_Fog.scale", "(1.0 / (fogEnd - fogStart))", set, FOG_START, FOG_END);
               s = replace(s, "gl_Fog.color", "vec4(fogColor, 1.0)", set, FOG_COLOR);
               if (program.getName().contains("entities")) {
                  s = replace(s, PATTERN_TEXTURE2D_TEXCOORD, "$1clamp($4, 0.0, 1.0)$5", set);
               }

               if (shaderType == ShaderType.FRAGMENT) {
                  s = replace(s, "gl_FragColor", "gl_FragData[0]", set);
                  s = addAlphaTest(program, s, set);
               }

               if (s.contains("texture")) {
                  s = replace(s, Pattern.compile("(sampler2D\\s+)texture(\\W)"), "$1gtexture$2", set);
                  s = replace(s, Pattern.compile("(\\(\\s*)texture(\\s*,)"), "$1gtexture$2", set);
               }

               s = replace(s, "texture2D", "texture", set);
               s = replace(s, "texture2DLod", "textureLod", set);
               s = replace(s, "texture2DGrad", "textureGrad", set);
               s = replace(s, "texture2DGradARB", "textureGrad", set);
               s = replace(s, "texture3D", "texture", set);
               s = replace(s, "texture3DLod", "textureLod", set);
               s = replaceShadow2D(s, "shadow2D", "texture", set);
               s = replaceShadow2D(s, "shadow2DLod", "textureLod", set);
               s = replace(s, "texelFetch2D", "texelFetch", set);
               s = replace(s, "texelFetch3D", "texelFetch", set);
               s = replaceFragData(s, set);
               if (i <= 120) {
                  s = replace(s, "common", "commonX", set);
                  s = replace(s, "smooth", "smoothX", set);
               }

               s = replace(s, "gl_ModelViewProjectionMatrixInverse", "gl_ModelViewProjectionMatrixInverse_TODO", set);
               s = replace(s, "gl_TextureMatrixInverse", "gl_TextureMatrixInverse_TODO", set);
               s = replace(s, "gl_ModelViewMatrixTranspose", "gl_ModelViewMatrixTranspose_TODO", set);
               s = replace(s, "gl_ProjectionMatrixTranspose", "gl_ProjectionMatrixTranspose_TODO", set);
               s = replace(s, "gl_ModelViewProjectionMatrixTranspose", "gl_ModelViewProjectionMatrixTranspose_TODO", set);
               s = replace(s, "gl_TextureMatrixTranspose", "gl_TextureMatrixTranspose_TODO", set);
               s = replace(s, "gl_ModelViewMatrixInverseTranspose", "gl_ModelViewMatrixInverseTranspose_TODO", set);
               s = replace(s, "gl_ProjectionMatrixInverseTranspose", "gl_ProjectionMatrixInverseTranspose_TODO", set);
               s = replace(s, "gl_ModelViewProjectionMatrixInverseTranspose", "gl_ModelViewProjectionMatrixInverseTranspose_TODO", set);
               s = replace(s, "gl_TextureMatrixInverseTranspose", "gl_TextureMatrixInverseTranspose_TODO", set);
               if (s.contains("\n")) {
                  String[] astring = Config.tokenize(s, "\n\r");
                  linebuffer.add(astring);
               } else {
                  linebuffer.add(s);
               }
            }
         }

         if (set.isEmpty()) {
            return linebuffer;
         } else {
            linebuffer = removeExisting(linebuffer, set);
            linebuffer = moveExtensionsToHeader(linebuffer, set);
            String[] astring1 = set.stream().map((x) -> {
               return x.getText();
            }).toArray((x$0) -> {
               return new String[x$0];
            });
            Arrays.sort(astring1, getComparatorHeaderLines());
            astring1 = (String[])ArrayUtils.addObjectToArray(astring1, "// Compatibility (auto-generated)", 0);
            int j = getIndexInsertHeader(linebuffer, i);
            if (j >= 0) {
               linebuffer.insert(j, astring1);
            }

            return linebuffer;
         }
      }
   }

   private static HeaderLine makeHeaderLine(String line) {
      Matcher matcher = PATTERN_UNIFORM.matcher(line);
      if (matcher.matches()) {
         return new HeaderLineVariable("uniform", matcher.group(2), line);
      } else {
         Matcher matcher1 = PATTERN_IN.matcher(line);
         if (matcher1.matches()) {
            return new HeaderLineVariable("in", matcher1.group(2), line);
         } else {
            Matcher matcher2 = PATTERN_OUT.matcher(line);
            if (matcher2.matches()) {
               return new HeaderLineVariable("out", matcher2.group(2), line);
            } else {
               Matcher matcher3 = PATTERN_VARYING.matcher(line);
               if (matcher3.matches()) {
                  return new HeaderLineVariable("varying", matcher3.group(1), line);
               } else {
                  Matcher matcher4 = PATTERN_CONST.matcher(line);
                  if (matcher4.matches()) {
                     return new HeaderLineVariable("const", matcher4.group(1), line);
                  } else {
                     Matcher matcher5 = PATTERN_FUNCTION.matcher(line);
                     if (matcher5.matches()) {
                        return new HeaderLineFunction(matcher5.group(1), line);
                     } else {
                        throw new IllegalArgumentException("Unknown header line: " + line);
                     }
                  }
               }
            }
         }
      }
   }

   private static String makeFtransformBasic() {
      StringBuilder stringbuilder = new StringBuilder();
      addLine(stringbuilder, "vec4 ftransformBasic()                                                                                           ");
      addLine(stringbuilder, "{                                                                                                                ");
      addLine(stringbuilder, "  if(renderStage != MC_RENDER_STAGE_OUTLINE)   // Render stage outline                                           ");
      addLine(stringbuilder, "    return projectionMatrix * modelViewMatrix * vec4(vaPosition, 1.0);                                           ");
      addLine(stringbuilder, "  float lineWidth = 2.5;                                                                                         ");
      addLine(stringbuilder, "  vec2 screenSize = vec2(viewWidth, viewHeight);                                                                 ");
      addLine(stringbuilder, "  const mat4 VIEW_SCALE = mat4(mat3(1.0 - (1.0 / 256.0)));                                                       ");
      addLine(stringbuilder, "  vec4 linePosStart = projectionMatrix * VIEW_SCALE * modelViewMatrix * vec4(vaPosition, 1.0);                   ");
      addLine(stringbuilder, "  vec4 linePosEnd = projectionMatrix * VIEW_SCALE * modelViewMatrix * (vec4(vaPosition + vaNormal, 1.0));        ");
      addLine(stringbuilder, "  vec3 ndc1 = linePosStart.xyz / linePosStart.w;                                                                 ");
      addLine(stringbuilder, "  vec3 ndc2 = linePosEnd.xyz / linePosEnd.w;                                                                     ");
      addLine(stringbuilder, "  vec2 lineScreenDirection = normalize((ndc2.xy - ndc1.xy) * screenSize);                                        ");
      addLine(stringbuilder, "  vec2 lineOffset = vec2(-lineScreenDirection.y, lineScreenDirection.x) * lineWidth / screenSize;                ");
      addLine(stringbuilder, "  if (lineOffset.x < 0.0)                                                                                        ");
      addLine(stringbuilder, "    lineOffset *= -1.0;                                                                                          ");
      addLine(stringbuilder, "  if (gl_VertexID % 2 == 0)                                                                                      ");
      addLine(stringbuilder, "    return vec4((ndc1 + vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);                                ");
      addLine(stringbuilder, "  else                                                                                                           ");
      addLine(stringbuilder, "    return vec4((ndc1 - vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);                                ");
      addLine(stringbuilder, "}                                                                                                                ");
      String s = stringbuilder.toString();
      return s.replace("MC_RENDER_STAGE_OUTLINE", "" + RenderStage.OUTLINE.ordinal());
   }

   private static void addLine(StringBuilder buf, String line) {
      buf.append(StrUtils.trimTrailing(line, " \t") + "\n");
   }

   private static LineBuffer removeExisting(LineBuffer lines, Set<HeaderLine> headerLines) {
      if (headerLines.isEmpty()) {
         return lines;
      } else {
         LineBuffer linebuffer = new LineBuffer(lines.getLines());

         for(HeaderLine headerline : headerLines) {
            for(int i = 0; i < linebuffer.size(); ++i) {
               String s = linebuffer.get(i);
               if (headerline.matches(s)) {
                  String s1 = headerline.removeFrom(s);
                  if (s1 == null) {
                     s1 = "// Moved up";
                  }

                  linebuffer.set(i, s1);
               }
            }
         }

         return linebuffer;
      }
   }

   private static LineBuffer moveExtensionsToHeader(LineBuffer lines, Set<HeaderLine> headerLines) {
      LineBuffer linebuffer = new LineBuffer(lines.getLines());

      for(int i = 0; i < lines.size(); ++i) {
         String s = lines.get(i);
         if (PATTERN_EXTENSION.matcher(s).matches()) {
            String s1 = s.trim();
            s1 = replaceWord(s1, "require", "enable");
            HeaderLine headerline = new HeaderLineText(s1);
            headerLines.add(headerline);
            s = "//" + s;
         }

         linebuffer.set(i, s);
      }

      return linebuffer;
   }

   private static int getVersion(String line, int def) {
      Matcher matcher = PATTERN_VERSION.matcher(line);
      if (!matcher.matches()) {
         return def;
      } else {
         String s = matcher.group(1);
         int i = Config.parseInt(s, -1);
         return i < def ? def : i;
      }
   }

   private static int getIndexInsertHeader(LineBuffer lines, int version) {
      int i = lines.indexMatch(PATTERN_VERSION);
      int j = lines.indexMatch(PATTERN_LINE, i);
      if (j < 0) {
         Config.warn("Header insert line not found");
      }

      return j;
   }

   private static String addAlphaTest(Program program, String line, Set<HeaderLine> headerLines) {
      if (program.getProgramStage().isAnyComposite()) {
         return line;
      } else {
         Matcher matcher = PATTERN_FRAG_DATA_SET.matcher(line);
         if (matcher.matches()) {
            String s = matcher.group(2);
            if (!s.equals("0")) {
               return line;
            }

            HeaderLine headerline = new HeaderLineText("vec4 temp_FragData" + s + ";");
            headerLines.add(headerline);
            headerLines.add(ALPHA_TEST_REF);
            String s1 = matcher.replaceAll("$1{\n$1  temp_FragData$2$3 = $4\n$1  if(temp_FragData$2.a < alphaTestRef) discard;\n$1  gl_FragData[$2] = temp_FragData$2;\n$1}");
            line = s1;
         }

         Matcher matcher1 = PATTERN_FRAG_DATA_GET.matcher(line);
         if (matcher1.find()) {
            String s3 = matcher1.group(1);
            if (!s3.equals("0")) {
               return line;
            }

            HeaderLine headerline1 = new HeaderLineText("vec4 temp_FragData" + s3 + ";");
            headerLines.add(headerline1);
            String s2 = matcher1.replaceAll("temp_FragData$1$2");
            line = s2;
         }

         return line;
      }
   }

   private static String replaceShadow2D(String line, String name, String nameNew, Set<HeaderLine> headerLines) {
      if (line.indexOf(name) < 0) {
         return line;
      } else {
         String line2 = line.replaceAll(name + "\\((([^()]*+|\\(([^()]*+|\\([^()]*+\\))*\\))*)\\)\\.[xyzrgb]{3}", "vec3(" + nameNew + "($1))");
         line2 = line2.replaceAll(name + "\\((([^()]*+|\\(([^()]*+|\\([^()]*+\\))*\\))*)\\)\\.[xyzrgb]", nameNew + "($1)");
         return line2.replaceAll(name + "\\((([^()]*+|\\(([^()]*+|\\([^()]*+\\))*\\))*)\\)([^.])", "vec4(vec3(" + nameNew + "($1)), 1.0)$4");
      }
   }

   private static String replaceFragData(String line, Set<HeaderLine> headerLines) {
      Matcher matcher = PATTERN_FRAG_DATA.matcher(line);
      if (matcher.find()) {
         String s = matcher.replaceAll("outColor$1");

         for(int i = 0; i < 8; ++i) {
            if (s.contains("outColor" + i)) {
               headerLines.add(new HeaderLineText("out vec4 outColor" + i + ";"));
            }
         }

         return s;
      } else {
         return line;
      }
   }

   private static Comparator<String> getComparatorHeaderLines() {
      return new Comparator<String>() {
         private static final int UNKNOWN = Integer.MAX_VALUE;

         public int compare(String o1, String o2) {
            if (o1.startsWith("in ") && o2.startsWith("in ")) {
               int i = this.getAttributeIndex(o1);
               int j = this.getAttributeIndex(o2);
               if (i != Integer.MAX_VALUE || j != Integer.MAX_VALUE) {
                  return i - j;
               }
            }

            if (o1.startsWith("uniform ") && o2.startsWith("uniform ")) {
               int k = this.getUniformIndex(o1);
               int l = this.getUniformIndex(o2);
               if (k != Integer.MAX_VALUE || l != Integer.MAX_VALUE) {
                  return k - l;
               }
            }

            return o1.compareTo(o2);
         }

         private int getAttributeIndex(String line) {
            if (line.equals(ShadersCompatibility.POSITION.getText())) {
               return 0;
            } else if (line.equals(ShadersCompatibility.COLOR.getText())) {
               return 1;
            } else if (line.equals(ShadersCompatibility.UV0.getText())) {
               return 2;
            } else if (line.equals(ShadersCompatibility.UV1.getText())) {
               return 3;
            } else if (line.equals(ShadersCompatibility.UV2.getText())) {
               return 4;
            } else {
               return line.equals(ShadersCompatibility.NORMAL.getText()) ? 5 : Integer.MAX_VALUE;
            }
         }

         private int getUniformIndex(String line) {
            if (line.equals(ShadersCompatibility.MODEL_VIEW_MATRIX.getText())) {
               return 0;
            } else if (line.equals(ShadersCompatibility.MODEL_VIEW_MATRIX_INVERSE.getText())) {
               return 1;
            } else if (line.equals(ShadersCompatibility.PROJECTION_MATRIX.getText())) {
               return 2;
            } else if (line.equals(ShadersCompatibility.PROJECTION_MATRIX_INVERSE.getText())) {
               return 3;
            } else if (line.equals(ShadersCompatibility.TEXTURE_MATRIX.getText())) {
               return 4;
            } else if (line.equals(ShadersCompatibility.NORMAL_MATRIX.getText())) {
               return 5;
            } else if (line.equals(ShadersCompatibility.CHUNK_OFFSET.getText())) {
               return 6;
            } else {
               return line.equals(ShadersCompatibility.ALPHA_TEST_REF.getText()) ? 7 : Integer.MAX_VALUE;
            }
         }
      };
   }

   private static String replace(String line, String find, String replace, Set<HeaderLine> newLines, HeaderLine... headerLines) {
      String s = replaceWord(line, find, replace);
      if (!s.equals(line) && headerLines.length > 0) {
         newLines.addAll(Arrays.asList(headerLines));
      }

      return s;
   }

   private static String replaceWord(String line, String find, String replace) {
      String s = line;
      int i = line.length();

      while(i > 0) {
         i = s.lastIndexOf(find, i - 1);
         if (i >= 0) {
            int j = i + find.length();
            if (i - 1 >= 0) {
               char c0 = s.charAt(i - 1);
               if (Character.isLetter(c0) || Character.isDigit(c0) || c0 == '_') {
                  continue;
               }
            }

            if (j < s.length()) {
               char c1 = s.charAt(j);
               if (Character.isLetter(c1) || Character.isDigit(c1) || c1 == '_') {
                  continue;
               }
            }

            s = s.substring(0, i) + replace + s.substring(j);
         }
      }

      return s;
   }

   private static String replace(String line, Pattern pattern, String replace, Set<HeaderLine> newLines, HeaderLine... headerLines) {
      Matcher matcher = pattern.matcher(line);
      if (!matcher.find()) {
         return line;
      } else {
         String s = matcher.replaceAll(replace);
         if (!s.equals(line) && headerLines.length > 0) {
            newLines.addAll(Arrays.asList(headerLines));
         }

         return s;
      }
   }

   private static boolean matches(String line, Pattern pattern) {
      Matcher matcher = pattern.matcher(line);
      return matcher.matches();
   }
}