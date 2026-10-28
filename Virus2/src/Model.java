import java.io.File;

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

public class Model extends GameComponent {

    private static boolean createLight = true;

    private String meshName;

    public Model(String meshName) {
        this.meshName = meshName;
    }

    public String getMeshName() {
        return meshName;
    }

    @Override
    public GameLight getLight(GameSystem gs, GameNode node, int i) throws Exception {
        GameLight light = (GameLight)node.data.get("light");

        if(light != null && i == 0) {
            light.position.set(200, 400, 200);
            light.radius = 800;
            light.color.set(2, 2, 2, 1);

            return light;
        }
        return null;
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {

        if(createLight) {
            createLight = false;
            node.data.put("light", new GameLight());

            gs.eye.set(150, 200, 150);
            gs.backgroundColor.set(0, 0, 0, 0);
        }

        Mesh mesh = gs.game.getResources().load(new File(meshName));

        node.data.put("mesh", mesh);
        node.data.put("warp", false);
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        if(gs.inDesign()) {
            return ;
        }

        if(meshName.equals("ledge.obj")) {
            node.data.put("warp", gs.game.isMouseButtonDown(0));
            node.rotate(1, 45 * gs.game.getElapsedTime());
        }
    }

    @Override
    public boolean pick(GameSystem gs, GameNode node, Vec3 origin, Vec3 direction, float[] time) throws Exception {
        Mesh mesh = (Mesh)node.data.get("mesh");

        mesh.calcBoundsAndTransform(node);

        return mesh.pick(origin, direction, time);
    }

    @Override
    public void render(GameSystem gs, GameNode node, BasicRenderer renderer, RenderPass renderPass) throws Exception {
        if(renderPass != RenderPass.SOLID) {
            return ;
        }

        Mesh mesh = (Mesh)node.data.get("mesh");

        mesh.calcBoundsAndTransform(node);

        if(gs.frustum.testAab(mesh.bounds)) {
            for(int i = 0; i != mesh.getPartCount(); i++) {
                MeshPart part = mesh.getPart(i);

                if(gs.frustum.testAab(part.bounds)) {
                    renderer.setTexture(part.get(MeshLoader.TEXTURE));
                    renderer.setAmbientColor(mesh.get(MeshLoader.AMBIENT_COLOR));
                    renderer.setDiffuseColor(mesh.get(MeshLoader.DIFFUSE_COLOR));
                    if(meshName.equals("ledge.obj")) {
                        renderer.setEmissiveColor(1, 0.5f, 0, 1);
                    } else {
                        renderer.setEmissiveColor(0, 0, 0, 0);
                    }
                    renderer.setModel(mesh.model);
                    renderer.setModelIT(mesh.modelIT);

                    ((BasicRenderer2)renderer).setWarp((Boolean)node.data.get("warp"), gs.game.getTotalTime() * 2);

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
        createLight = true;
    }

    @Override
    public String toString() {
        return meshName;
    }
}
