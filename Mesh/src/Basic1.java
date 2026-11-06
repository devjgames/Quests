import java.io.File;

import org.game.BasicRenderer;
import org.game.CameraHelper;
import org.game.Game;
import org.game.Mat4;
import org.game.ResourceManager;
import org.game.SpriteRenderer;
import org.game.Texture;
import org.game.UI;
import org.game.Vec3;
import org.game.VertexArrayBuffer;
import org.game.VertexElementBuffer;

public class Basic1 extends Demo {

    private Mat4 projection = new Mat4();
    private Mat4 view = new Mat4();
    private Vec3 eye = new Vec3();
    private Vec3 target = new Vec3();
    private Vec3 up = new Vec3();
    private VertexArrayBuffer vertexBuffer;
    private VertexElementBuffer vertexElementBuffer;
    private Texture ledge;
    private BasicRenderer renderer;
    private boolean down;
    private boolean drag;

    @Override
    public void init(Game game, ResourceManager resources) throws Exception {
        eye.set(150, 150, 150);
        target.zero();
        up.set(0, 1, 0);
        projection.identity();
        view.lookAt(eye, target, up);

        HEMesh mesh = new HEMesh();

        mesh.addVertex(-50, -50, -50);
        mesh.addVertex(-50, +50, -50);
        mesh.addVertex(+50, +50, -50);
        mesh.addVertex(+50, -50, -50);
        mesh.addVertex(-50, -50, +50);
        mesh.addVertex(-50, +50, +50);
        mesh.addVertex(+50, +50, +50);
        mesh.addVertex(+50, -50, +50);
        mesh.addFace(0, 1, 2, 3);
        mesh.addFace(7, 6, 5, 4);
        mesh.addFace(0, 4, 5, 1);
        mesh.addFace(3, 2, 6, 7);
        mesh.addFace(0, 3, 7, 4);
        mesh.addFace(1, 5, 6, 2);
        mesh.calcNormals(false);
        mesh.translate(50, 50, 50);
        mesh.calcTextureCoordinate(100);
        mesh.translate(-50, -50, -50);

        vertexBuffer = resources.manage(new VertexArrayBuffer(0));
        vertexElementBuffer = resources.manage(new VertexElementBuffer(0));

        mesh.build(vertexBuffer, vertexElementBuffer);

        ledge = resources.load(new File("ledge.png"));
        renderer = resources.manage(new BasicRenderer());

        down = drag = false;
    }

    @Override
    public void render(Game game, ResourceManager resources, int w, int h) throws Exception {
        projection.identity().perspective((float)Math.PI / 3, w / (float)h, 1, 25000);
        view.identity().lookAt(eye, target, up);
        renderer.begin(w, h, 0.15f, 0.15f, 0.15f, 1);
        renderer.setLightCount(1);
        renderer.setLightColor(0, 1, 1, 1, 1);
        renderer.setLightRadius(0, 800);
        renderer.setLightPosition(0, eye);
        renderer.setTexture(ledge);
        renderer.setProjection(projection);
        renderer.setView(view);
        renderer.draw(vertexBuffer, vertexElementBuffer);
        renderer.end();
    }

    @Override
    public void renderTargets(Game game, ResourceManager resources, int x, int y, int w, int h, SpriteRenderer spriteRenderer) throws Exception {
        if(renderer.getRenderTarget() != null) {
            spriteRenderer.begin(renderer.getRenderTarget().getTexture(0));
            spriteRenderer.push(0, 0, w, h, x, y, w, h, 1, 1, 1, 1, 1, 1, 1, 1, true);
            spriteRenderer.end();
        }
    }

    @Override
    public void render(Game game, ResourceManager resources, Texture font, SpriteRenderer spriteRenderer) throws Exception {
    }

    @Override
    public void update(Game game, ResourceManager resources, int x, int y, int w, int h) throws Exception {
        if(game.isMouseButtonDown(0)) {
            if(!down) {
                int mx = game.getMouseX() * game.getScale();
                int my = game.getMouseY() * game.getScale();

                down = true;
                if(mx >= x && mx < x + w && my >= y && my < y + h) {
                    drag = true;
                }
            }
        } else {
            drag = false;
            down = false;
        }
        if(drag) {
            CameraHelper.rotateAroundTarget(eye, target, up, game.getDeltaX() * 0.025f, -game.getDeltaY() * 0.025f);
        }
    }

    @Override
    public void ui(Game game, ResourceManager resources, UI ui) throws Exception {
    }
    
}
