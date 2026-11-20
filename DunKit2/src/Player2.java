import java.io.File;

import org.game.BasicRenderer;
import org.game.Collider;
import org.game.GameComponent;
import org.game.GameNode;
import org.game.GameSystem;
import org.game.Keys;
import org.game.Mesh;
import org.game.MeshLoader;
import org.game.MeshPart;
import org.game.Resource;
import org.game.SoundEngine;
import org.game.SpriteRenderer;
import org.game.Texture;
import org.game.Triangle;
import org.game.TriangleSelector;
import org.game.Vec3;
import org.game.Collider.ContactHandler;

public class Player2 extends Model implements ContactHandler {

    private Collider collider = new Collider();
    private Vec3 f = new Vec3();
    private Vec3 x = new Vec3();
    private Vec3 d = new Vec3();
    private Vec3 b = new Vec3();
    private Vec3 t = new Vec3();
    private boolean load = false;
    private float seconds = 0;
    private SoundEngine.Sound death = null;
    
    public Player2() {
        super("cube.obj");
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        super.init(gs, node);

        if(gs.inDesign()) {
            return;
        }

        doorTarget = true;

        death = gs.game.getResources().load(new File("death.wav"));
        death.setVolume(0.25f);

        collider.selectors.clear();
        collider.handler = this;

        for(int i = 0; i != gs.getCount(); i++) {
            GameComponent component = gs.getComponent(i);

            if(component instanceof Model) {
                Model model = (Model)component;
                String name = model.getMeshName();

                if(!name.equals("torch.obj") && !name.equals("door.obj") && !name.equals("coins.obj") && !name.equals("energy.obj") && !name.equals("orb.obj") && !name.equals("axis.obj") && !name.equals("cube.obj")) {
                    collider.selectors.add(new GameNodeSelector(gs.getNode(i)));
                }
            } else if(component instanceof KFModel) {
                collider.selectors.add(new GameNodeSelector(gs.getNode(i)));
            }
        }

        gs.target.set(node.position);
        d.set(0, 140, 32);
        gs.target.add(d, gs.eye);
        t.set(d);
        b.set(d);
        x.set(d);
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        if(gs.inDesign() || load) {
            seconds += gs.game.getElapsedTime();

            return;
        }

        collider.velocity.mul(0, 1, 0);
        f.zero();
        if(gs.game.isKeyDown(Keys.KEY_LEFT)) {
            f.set(-75, 0, 0);
        } else if(gs.game.isKeyDown(Keys.KEY_RIGHT)) {
            f.set(75, 0, 0);
        }
        if(gs.game.isKeyDown(Keys.KEY_UP)) {
            f.set(0, 0, -75);
        } else if(gs.game.isKeyDown(Keys.KEY_DOWN)) {
            f.set(0, 0, 75);
        }
        collider.velocity.add(f);
        if(f.length() > 0.0000001) {
            node.find("face").r.set(f).normalize();
            node.find("face").r.cross(0, 1, 0, node.find("face").f).normalize();
            node.find("cube").rotate(2, -180 * gs.game.getElapsedTime());
            x.set(f);
        }
        collider.velocity.y -= 2000 * gs.game.getElapsedTime();
        collider.resolve(node.position);
        gs.target.set(node.position);

        for(int i = 0; i != gs.getCount(); i++) {
            GameComponent c = gs.getComponent(i);

            if(c instanceof Model) {
                String name = ((Model)c).getMeshName();

                if(!name.equals("torch.obj") && !name.equals("door.obj") && !name.equals("coins.obj") && !name.equals("energy.obj") && !name.equals("orb.obj") && !name.equals("axis.obj") && !name.equals("cube.obj")) {
                    GameNode n = gs.getNode(i);
                    Mesh mesh = (Mesh)n.data.get("mesh");
                    
                    mesh.calcBoundsAndTransform(n);
                    if(mesh.bounds.containsPoint(node.position)) {
                        if(name.equals("door-way.obj") || name.startsWith("hall-")) {
                            t.set(node.f).mul(45).add(0, 8, 0);
                        } else if(name.equals("side.obj") || name.equals("corner.obj")) {
                            t.set(node.f).mul(8).add(0, 100, 0);
                        } else if(!name.equals("lava.obj")) {
                            t.set(b);
                        }
                    }
                }
            }
        }
        d.lerp(t, 0.025f);
        gs.target.add(d, gs.eye);
    }

    @Override
    public void render(GameSystem gs, GameNode node, SpriteRenderer spriteRenderer, BasicRenderer basicRenderer) throws Exception {
        Texture texture = gs.game.getResources().load(new File("sprites.png"));
        int s = gs.game.getScale();

        spriteRenderer.begin(texture);
        spriteRenderer.push(
            "FPS = " + gs.game.getFPS() + "\n" +
            "RES = " + Resource.getInstances() + "\n" +
            "TRI = " + basicRenderer.getTrianglesRendered() + "\n" +
            "TST = " + collider.getTested() + "\n" +
            "POS = " + (int)gs.eye.x + ", " + (int)gs.eye.y + ", " + (int)gs.eye.z + "\n",
            16, 10, 12, s * 2, 5, 10 * s, 10 * s, 1, 1, 0, 1, 1, 0.5f, 0, 1
        );
        spriteRenderer.end();
    }

    @Override
    public void contactMade(TriangleSelector selector, Triangle triangle) {
        if(selector instanceof GameNodeSelector) {
            GameNodeSelector gns = (GameNodeSelector)selector;
            Object obj = gns.getNode().data.get("mesh");

            if(obj instanceof Mesh) {
                Mesh mesh = (Mesh)obj;

                for(int i = 0; i != mesh.getPartCount(); i++) {
                    MeshPart part = mesh.getPart(i);
                    String name = part.get(MeshLoader.NAME);

                    if(name.equals("Lava")) {
                        if(triangle.n.y > 0.9f && triangle.p1.y < -1) {
                            load = true;
                            death.play(false);
                        }
                    }
                }
            } else if(obj instanceof KeyFrameMesh) {
                KeyFrameMesh mesh = (KeyFrameMesh)obj;

                if(mesh.file.getName().equals("worm")) {
                    load = true;
                    death.play(false);
                }
            }
        }
    }

    @Override
    public File loadFile(GameSystem gs, GameNode node) throws Exception {
        if(load && seconds > 1) {
            return new File(gs.game.getResources().getRoot(), gs.getSceneName());
        }
        return null;
    }

    @Override
    public void finish() throws Exception {
        load = false;
        seconds = 0;
    }

    @Override
    public String toString() {
        return "player2";
    }
}
