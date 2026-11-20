import java.io.File;

import org.game.BasicRenderer;
import org.game.GameComponent;
import org.game.GameNode;
import org.game.GameSystem;
import org.game.MeshLoader;
import org.game.MeshPart;
import org.game.RenderPass;
import org.game.Vec3;

public class KFModel extends GameComponent {
    
    private String meshName;

    public KFModel(String meshName) {
        this.meshName = meshName;
    }

    public String getMeshName() {
        return meshName;
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        KeyFrameMesh mesh = gs.game.getResources().load(new File(meshName));

        mesh = gs.game.getResources().manage(new KeyFrameMesh(mesh));

        if(meshName.equals("worm")) {
            mesh.setSequence(0, mesh.getCount() - 1, 16, true);
            node.scale.mul(0.7f);
            if(!gs.inDesign()) {
                node.data.put("center", new Vec3(node.position));
                node.position.x += 50;
            }
        } else if(meshName.equals("lever")) {
            node.data.put("on", false);
        }
        node.data.put("mesh", mesh);

    }

    @Override
    public boolean pick(GameSystem gs, GameNode node, Vec3 origin, Vec3 direction, float[] time) throws Exception {
        KeyFrameMesh mesh = (KeyFrameMesh)node.data.get("mesh");

        return mesh.getMesh().pick(origin, direction, time);
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        KeyFrameMesh mesh = (KeyFrameMesh)node.data.get("mesh");

        mesh.update();

        if(gs.inDesign()) {
            return ;
        }

        if(meshName.equals("worm")) {
            node.r.x = (float)Math.cos(gs.game.getTotalTime() * 0.5f);
            node.r.z = (float)Math.sin(gs.game.getTotalTime() * 0.5f);
            node.r.cross(node.u.set(0, 1, 0), node.f).normalize();
            node.position.set((Vec3)node.data.get("center")).add(-node.r.z * 50, 0, node.r.x * 50);
        }
    }

    @Override
    public void render(GameSystem gs, GameNode node, BasicRenderer renderer, RenderPass renderPass) throws Exception {
        KeyFrameMesh mesh = (KeyFrameMesh)node.data.get("mesh");

        mesh.getMesh().calcBoundsAndTransform(node);

        if(gs.frustum.testAab(mesh.getMesh().bounds)) {
            for(int i = 0; i != mesh.getMesh().getPartCount(); i++) {
                MeshPart part = mesh.getMesh().getPart(i);

                if(gs.frustum.testAab(part.bounds)) {
                    renderer.setTexture(part.get(MeshLoader.TEXTURE));
                    renderer.setAmbientColor(0.5f, 0.5f, 0.5f, 1);
                    renderer.setDiffuseColor(1, 1, 1, 1);
                    renderer.setModel(mesh.getMesh().model);
                    renderer.setModelIT(mesh.getMesh().modelIT);
                    renderer.setLightingEnabled(true);

                    ((BasicRenderer2)renderer).setWarp(false, 0, 0, 0, false);
                    if(meshName.equals("worm")) {
                        ((BasicRenderer2)renderer).setDecal(gs.game.getResources().load(new File("worm", "worm-decal.png")));
                    } else {
                        ((BasicRenderer2)renderer).setDecal(null);
                    }

                    renderer.draw(part.vertexBuffer, part.vertexElementBuffer);
                }
            }
        }
    }


    @Override
    public String toString() {
        return meshName;
    }
}
