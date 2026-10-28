import java.io.File;
import java.util.Random;

import org.game.BasicRenderer;
import org.game.GameComponent;
import org.game.GameLight;
import org.game.GameNode;
import org.game.GameSystem;
import org.game.Mesh;
import org.game.MeshLoader;
import org.game.MeshPart;
import org.game.RenderPass;
import org.game.Resource;
import org.game.SpriteRenderer;
import org.game.Texture;
import org.game.Vec3;

public class Worm extends GameComponent {

    private Random random = new Random(1000);
    
    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        KeyFrameMesh mesh = new KeyFrameMesh(gs.game.getResources().load(new File("worm")));

        gs.game.getResources().manage(mesh);

        mesh.setSequence(0, mesh.getCount() - 1, (int)(8 + random.nextFloat() * 6), true);

        node.data.put("mesh", mesh);
        node.data.put("light", new GameLight());

        gs.backgroundColor.set(0, 0, 0, 0);
    }

    @Override
    public GameLight getLight(GameSystem gs, GameNode node, int i) throws Exception {
        if(i == 0) {
            GameLight light = (GameLight)node.data.get("light");

            light.position.set(node.position).add(50, 100, 50);
            light.radius = 200;

            return light;
        }
        return null;
    }

    @Override
    public boolean pick(GameSystem gs, GameNode node, Vec3 origin, Vec3 direction, float[] time) throws Exception {
        KeyFrameMesh mesh = (KeyFrameMesh)node.data.get("mesh");

        mesh.getMesh().calcBoundsAndTransform(node);

        return mesh.getMesh().pick(origin, direction, time);
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        KeyFrameMesh mesh = (KeyFrameMesh)node.data.get("mesh");

        mesh.update();
    }

    @Override
    public void render(GameSystem gs, GameNode node, BasicRenderer renderer, RenderPass renderPass) throws Exception {

        if(renderPass != RenderPass.SOLID) {
            return;
        }

        KeyFrameMesh keyFrameMesh = (KeyFrameMesh)node.data.get("mesh");
        Mesh mesh = keyFrameMesh.getMesh();

        mesh.calcBoundsAndTransform(node);

        if(gs.frustum.testAab(mesh.bounds)) {
            for(int i = 0; i != mesh.getPartCount(); i++) {
                MeshPart part = mesh.getPart(i);

                if(gs.frustum.testAab(part.bounds)) {
                    renderer.setModel(mesh.model);
                    renderer.setModelIT(mesh.modelIT);
                    renderer.setLightingEnabled(true);
                    renderer.setTexture(part.get(MeshLoader.TEXTURE));
                    renderer.setAmbientColor(0.2f, 0.2f, 0.2f, 1);
                    renderer.setDiffuseColor(1, 1, 1, 1);
                    renderer.setEmissiveColor(1, 0.5f, 0, 1);

                    renderer.draw(part.vertexBuffer, part.vertexElementBuffer);
                }
            }
        }
    }

    @Override
    public void render(GameSystem gs, GameNode node, SpriteRenderer spriteRenderer, BasicRenderer basicRenderer) throws Exception {
        Texture font = gs.game.getResources().load(new File("sprites.png"));
        int s = gs.game.getScale();

        spriteRenderer.begin(font);
        spriteRenderer.push(
            "FPS = " + gs.game.getFPS() + "\n" +
            "RES = " + Resource.getInstances() + "\n" +
            "TRI = " + basicRenderer.getTrianglesRendered(),
            16, 10, 12, s, 5, 10 * s, 10 * s, 1, 1, 0, 1, 1, 0.5f, 0, 1
        );
        spriteRenderer.end();
    }

    @Override
    public void finish() throws Exception {
        random = new Random(1000);
    }

    @Override
    public String toString() {
        return "worm";
    }
}
