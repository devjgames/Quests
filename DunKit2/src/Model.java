import java.io.File;

import org.game.BasicRenderer;
import org.game.BlendState;
import org.game.GFX;
import org.game.GameComponent;
import org.game.GameNode;
import org.game.GameSystem;
import org.game.Mesh;
import org.game.MeshLoader;
import org.game.MeshPart;
import org.game.RenderPass;
import org.game.Texture;
import org.game.Vec3;
import org.game.Vec4;

public class Model extends GameComponent {

    private String meshName;

    public Model(String meshName) {
        this.meshName = meshName;
    }

    public String getMeshName() {
        return meshName;
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        Mesh mesh = gs.game.getResources().load(new File(meshName));

        if(meshName.equals("orb.obj") || meshName.equals("coins.obj") || meshName.equals("energy.obj") || meshName.equals("axis.obj")) {
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(1, 1, 1, 1));
            mesh.put(MeshLoader.DIFFUSE_COLOR, new Vec4(0, 0, 0, 1));
            if(meshName.equals("orb.obj")) {
                node.scale.mul(1.25f, 1.25f, 1.25f);
            }
            if(meshName.equals("axis.obj")) {
                node.scale.mul(0.5f);
            }
        } else if(meshName.equals("door.obj")) { 
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(0.35f, 0.35f, 0.35f, 1));
            node.data.put("y", node.position.y);
        } else if(meshName.equals("torch.obj")) {
            GameNode child = new GameNode();

            child.name = "torch";
            child.position.set(28, 0, 0);
            node.addChild(child);
        } else {
            mesh.put(MeshLoader.AMBIENT_COLOR, new Vec4(0.75f, 0.75f, 0.75f, 1));
            if(meshName.equals("bridge.obj")) {
                node.data.put("up", true);
                node.data.put("y", node.position.y);
                node.data.put("raise-lower", false);
            }
        }
        node.data.put("mesh", mesh);
    }

    @Override
    public boolean pick(GameSystem gs, GameNode node, Vec3 origin, Vec3 direction, float[] time) throws Exception {
        Mesh mesh = (Mesh)node.data.get("mesh");

        if(meshName.equals("torch.obj")) {
            mesh.calcBoundsAndTransform(node.find("torch"));
        } else {
            mesh.calcBoundsAndTransform(node);
        }

        return mesh.pick(origin, direction, time);
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        if(meshName.equals("energy.obj")) {
            if(!gs.inDesign()) {
                node.rotate(1, 45 * gs.game.getElapsedTime());
            }
        } else if(meshName.equals("axis.obj") && this instanceof Model) {
            node.position.set(gs.target);
        } else if(meshName.equals("door.obj")) {
            if(gs.inDesign()) {
                return ;
            }

            float ex = gs.eye.x;
            float ez = gs.eye.z;
            float px = node.position.x;
            float pz = node.position.z;
            float dx = px - ex;
            float dz = pz - ez;
            float d = (float)Math.sqrt(dx * dx + dz * dz);
            float a = 1 - Math.min(d / 75, 1);

            node.position.y = (float)node.data.get("y") - a * 150;
        } else if(meshName.equals("bridge.obj")) {
            if(gs.inDesign()) {
                return ;
            }
            boolean raiseLower = (Boolean)node.data.get("raise-lower");

            if(raiseLower) {
                boolean up = (Boolean)node.data.get("up");
                float y = (Float)node.data.get("y");

                if(up) {
                    node.position.y += 16 * gs.game.getElapsedTime();
                    if(node.position.y > y + 64) {
                        node.position.y = y + 64;
                        node.data.put("raise-lower", false);
                        node.data.put("up", false);
                    }
                } else {
                    node.position.y -= 16 * gs.game.getElapsedTime();
                    if(node.position.y < y) {
                        node.position.y = y;
                        node.data.put("raise-lower", false);
                        node.data.put("up", true);
                    }
                }
            }
        }
    }

    @Override
    public void render(GameSystem gs, GameNode node, BasicRenderer renderer, RenderPass renderPass) throws Exception {
        if(meshName.equals("energy.obj")) {
            if(renderPass != RenderPass.TRANSPARENT) {
                return ;
            }
            GFX.setBlendState(BlendState.ALPHA);
        } else if(renderPass != RenderPass.SOLID) {
            return ;
        }

        if(!gs.inDesign() && meshName.equals("axis.obj")) {
            return ;
        }

        Texture decal = null;

        if(meshName.equals("door.obj")) {
            decal = gs.game.getResources().load(new File("door-glow.png"));
        }


        Mesh mesh = (Mesh)node.data.get("mesh");

        if(meshName.equals("torch.obj")) {
            mesh.calcBoundsAndTransform(node.find("torch"));
        } else {
            mesh.calcBoundsAndTransform(node);
        }

        if(gs.frustum.testAab(mesh.bounds)) {
            for(int i = 0; i != mesh.getPartCount(); i++) {
                MeshPart part = mesh.getPart(i);

                if(gs.frustum.testAab(part.bounds)) {
                    String name = part.get(MeshLoader.NAME);

                    renderer.setTexture(part.get(MeshLoader.TEXTURE));
                    renderer.setAmbientColor(mesh.get(MeshLoader.AMBIENT_COLOR));
                    renderer.setDiffuseColor(mesh.get(MeshLoader.DIFFUSE_COLOR));
                    renderer.setModel(mesh.model);
                    renderer.setModelIT(mesh.modelIT);
                    renderer.setLightingEnabled(true);

                    if(meshName.equals("orb.obj")) {
                        ((BasicRenderer2)renderer).setWarp(true, gs.game.getTotalTime() * 2, 2, 0.25f, true);
                    } else if(meshName.equals("lava.obj") && name.equals("Lava")) {
                        ((BasicRenderer2)renderer).setWarp(true, gs.game.getTotalTime() * 2, 4, 0.15f, true);
                        renderer.setAmbientColor(1, 1, 1, 1);
                        renderer.setDiffuseColor(0, 0, 0, 1);
                    } else {
                        ((BasicRenderer2)renderer).setWarp(false, 0, 0, 0, false);
                    }
                    ((BasicRenderer2)renderer).setDecal(decal);

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
