import org.game.BlendState;
import org.game.ColorFormat;
import org.game.CullState;
import org.game.DepthState;
import org.game.GFX;
import org.game.GeometryType;
import org.game.IOHelper;
import org.game.Log;
import org.game.RenderTarget;
import org.game.Resource;
import org.game.Shader;
import org.game.VertexAttribute;

public class Fractal extends Resource {
    
    public float x = 0;
    public float y = 0;
    public float d = 3;
    public boolean julia = false;
    public float juliaX = 0.5f;
    public float juliaY = 0;
    public int maxCount = 500;
    public float bailout = 4.0f;
    public float centerX = 0;
    public float centerY = 0;
    public float scaleX = 1;
    public float scaleY = 1;
    public float frequency = 2.0f;
    public final Gradient gradient;
    public final Lines lines = new Lines();

    private Shader shader = null;
    private RenderTarget target = null;

    public Fractal() throws Exception {
        Log.getInstance().put(0, "creating fractal ...");
        gradient = new Gradient();
        shader = new Shader(
            new String(IOHelper.readAllBytes(Gradient.class, "/Vertex.glsl")),
            new String(IOHelper.readAllBytes(Gradient.class, "/FractalFragment.glsl")),
            new VertexAttribute("iPosition", 2)
        );
        initDimensions();
    }

    public void initDimensions() {
        x = y = 0;
        d = 3;
    }

    public RenderTarget getTarget() {
        return target;
    }

    public void plot(Quad quad, int w, int h) throws Exception {
        boolean create = target == null;

        if(!create) {
            create = w > 10 && h > 10 && (target.getWidth() != w || target.getHeight() != h);
        }
        if(create) {
            if(target != null) {
                target.destroy();
            }
            target = new RenderTarget(w, h, false, ColorFormat.COLOR);
            target.getTexture(0).bind();
            target.getTexture(0).toLinear(true);
            target.getTexture(0).unBind();
        }
        plot(target, quad, w, h);
    }

    public void plot(RenderTarget target, Quad quad, int w, int h) throws Exception {
        float a = w / (float)h;

        Log.getInstance().put(0, "rendering ...");

        gradient.render(quad);

        panZoom(w / 2, h / 2, w, h, 1);

        target.begin();
        GFX.clear(1, 1, 1, 1);
        GFX.setDepthState(DepthState.NONE);
        GFX.setBlendState(BlendState.OPAQUE);
        GFX.setCullState(CullState.NONE);
        shader.begin();
        shader.set("uJulia", julia);
        shader.set("uJuliaXY", juliaX, juliaY);
        shader.set("uMaxCount", maxCount);
        shader.set("uBailout", bailout);
        shader.set("uCenter", centerX, centerY);
        shader.set("uScale", scaleX, scaleY);
        shader.set("uFrequency", frequency);
        shader.set("uMin", x - d / 2 * a, y - d / 2);
        shader.set("uMax", x + d / 2 * a, y + d / 2);
        gradient.bind(shader, "uGradient");
        shader.set("uLineCount", lines.getCount());
        for(int i = 0; i != lines.getCount(); i++) {
            shader.set("uLines[" + i + "]", lines.getX(i), lines.getY(i));
        }
        shader.draw(GeometryType.TRIANGLES, quad.vertexBuffer);
        shader.end();
        target.end();
    }

    public final void panZoom(int x, int y, int w, int h, float amount) {
        y = h - y;

        float cx = this.x;
        float cy = this.y;
        float a = w / (float)h;
        float x1 = cx - d / 2 * a;
        float y1 = cy - d / 2;

        this.x = x1 + x / (float)w * d * a;
        this.y = y1 + y / (float)h * d;

        d *= amount;
    }

    @Override
    public void destroy() {
        shader.destroy();
        gradient.destroy();
        if(target != null) {
            target.destroy();
        }
        super.destroy();
    }
}
