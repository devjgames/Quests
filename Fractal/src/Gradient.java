import java.util.Arrays;
import java.util.Random;

import org.game.BlendState;
import org.game.ColorFormat;
import org.game.CullState;
import org.game.DepthState;
import org.game.GFX;
import org.game.GeometryType;
import org.game.IOHelper;
import org.game.RenderTarget;
import org.game.Resource;
import org.game.Shader;
import org.game.Vec4;
import org.game.VertexAttribute;

public class Gradient extends Resource {
    
    private ColorEntry[] colors;
    private RenderTarget target;
    private Shader shader;

    public Gradient() throws Exception {
        shader = new Shader(
            new String(IOHelper.readAllBytes(Gradient.class, "/Vertex.glsl")),
            new String(IOHelper.readAllBytes(Gradient.class, "/GradientFragment.glsl")),
            new VertexAttribute("iPosition", 2)
        );
        target = new RenderTarget(512, 8, false, ColorFormat.COLOR);
        target.getTexture(0).bind();
        target.getTexture(0).toLinear(true);
        target.getTexture(0).unBind();

        newRandom(50, 7);
    }

    public int getCount() {
        return colors.length;
    }

    public ColorEntry get(int i) {
        return colors[i];
    }

    public void shift() {
        ColorEntry start = colors[0];
        int count = colors.length;

        for(int i = 0; i != getCount() - 1; i++) {
            colors[i] = colors[i + 1];
        }
        colors[getCount() - 1] = start;

        colors[0].position = 0;
        for(int i = 1; i != count - 1; i++) {
            colors[i].position = i / (float)(count - 1);
        }
        colors[getCount() - 1].position = 1;
    }

    public void sort() {
        Arrays.sort(colors);
    }

    public RenderTarget getTarget() throws Exception {
        return target;
    }

    public void newRandom(int seed, int count) {
        if(count < 3 || count > 16) {
            System.out.println("count < 3 || coubt > 16");
            return ;
        }

        Random random = new Random(seed);

        colors = new ColorEntry[count];
        colors[0] = new ColorEntry(new Vec4(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1), 0);
        for(int i = 1; i != count - 1; i++) {
            colors[i] = new ColorEntry(new Vec4(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1), i / (float)(count - 1));
        }
        colors[count - 1] = new ColorEntry(colors[0].color, 1);
    }

    public void render(Quad quad) throws Exception {
        sort();

        target.begin();
        GFX.clear(1, 1, 1, 1);
        GFX.setDepthState(DepthState.NONE);
        GFX.setBlendState(BlendState.OPAQUE);
        GFX.setCullState(CullState.NONE);
        shader.begin();
        shader.set("uMin", 0, 0);
        shader.set("uMax", 1, 1);
        shader.set("uCount", colors.length);
        for(int i = 0; i != colors.length; i++) {
            shader.set("uColors[" + i + "]", colors[i].color.x, colors[i].color.y, colors[i].color.z, 1);
            shader.set("uPositions[" + i + "]", colors[i].position);
        }
        shader.draw(GeometryType.TRIANGLES, quad.vertexBuffer);
        shader.end();
        target.end();
    }

    public void bind(Shader shader, String name) {
        shader.set(name, target.getTexture(0), 0);
    }

    @Override
    public void destroy() {
        shader.destroy();
        target.destroy();
        
        super.destroy();
    }
}
