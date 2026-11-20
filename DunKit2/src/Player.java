import java.io.File;

import org.game.BasicRenderer;
import org.game.CameraHelper;
import org.game.Collider;
import org.game.GameComponent;
import org.game.GameNode;
import org.game.GameSystem;
import org.game.Intersection;
import org.game.Keys;
import org.game.Log;
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

public class Player extends Model implements ContactHandler {

    private Collider collider = new Collider();
    private Vec3 f = new Vec3();
    private Vec3 d = new Vec3();
    private boolean load = false;
    private float seconds = 0;
    private boolean start = true;
    private Vec3 origin = new Vec3();
    private Vec3 direction = new Vec3();
    private float[] time = new float[1];
    private Triangle triangle = new Triangle();
    private boolean spaceDown = false;
    private SoundEngine.Sound death = null;
    
    public Player() {
        super("axis.obj");
    }

    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        super.init(gs, node);

        if(gs.inDesign()) {
            start = false;
            return;
        }

        death = gs.game.getResources().load(new File("death.wav"));
        death.setVolume(0.25f);

        collider.selectors.clear();
        collider.handler = this;

        for(int i = 0; i != gs.getCount(); i++) {
            GameComponent component = gs.getComponent(i);

            if(component instanceof Model) {
                Model model = (Model)component;
                String name = model.getMeshName();

                if(!name.equals("torch.obj") && !name.equals("door.obj") && !name.equals("coins.obj") && !name.equals("energy.obj") && !name.equals("orb.obj") && !name.equals("axis.obj")) {
                    collider.selectors.add(new GameNodeSelector(gs.getNode(i)));
                }
            } else if(component instanceof KFModel) {
                collider.selectors.add(new GameNodeSelector(gs.getNode(i)));
            }
        }

        gs.eye.set(node.position);
        gs.eye.add(node.r, gs.target);
        gs.up.set(node.u);

        gs.game.setMouseGrabbed(true);

        gs.game.resetTimer();
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        if(gs.inDesign() || load) {
            seconds += gs.game.getElapsedTime();

            return;
        }
        if(start) {
            if(gs.game.isMouseButtonDown(0)) {
                gs.eye.set(node.position);
                gs.eye.add(node.r, gs.target);
                gs.up.set(node.u);

                gs.game.setMouseGrabbed(true);

                start = false;
            }
            return;
        }
        
        CameraHelper.rotateAroundEye(gs.eye, gs.target, gs.up, gs.game.getDeltaX() * 0.025f, gs.game.getDeltaY() * 0.025f);

        gs.target.sub(gs.eye, d).normalize();
        f.set(d).mul(1, 0, 1);
        collider.velocity.mul(0, 1, 0);
        if(f.length() > 0.0000001) {
            f.normalize();
            if(gs.game.isMouseButtonDown(0)) {
                f.mul(75);
            } else if(gs.game.isMouseButtonDown(1)) {
                f.mul(-75);
            } else {
                f.zero();
            };
            collider.velocity.add(f);
        }
        collider.velocity.y -= 2000 * gs.game.getElapsedTime();
        collider.resolve(gs.eye);
        gs.eye.add(d, gs.target);

        if(gs.game.isKeyDown(Keys.KEY_SPACE)) {
            if(!spaceDown) {
                spaceDown = true;

                origin.set(gs.eye);
                gs.target.sub(gs.eye, direction).normalize();

                time[0] = Float.MAX_VALUE;

                KeyFrameMesh lever = null;
                GameNode leverNode = null;

                for(int i = 0; i != gs.getCount(); i++) {
                    GameNode n = gs.getNode(i);
                    GameComponent component = gs.getComponent(i);

                    if(component instanceof Model) {
                        if(((Model)component).getMeshName().equals("axis.obj")) {
                            continue;
                        }
                    }

                    Object obj = n.data.get("mesh");

                    if(obj instanceof Mesh) {
                        Mesh mesh = (Mesh)obj;

                        mesh.calcBoundsAndTransform(n);
                        pick(mesh, origin, direction, time);
                    } else if(obj instanceof KeyFrameMesh) {
                        KeyFrameMesh mesh = (KeyFrameMesh)obj;

                        mesh.getMesh().calcBoundsAndTransform(n);
                        if(pick(mesh.getMesh(), origin, direction, time)) {
                            KFModel model = (KFModel)component;

                            if(model.getMeshName().equals("lever")) {
                                lever = mesh;
                                leverNode = n;
                            }
                        }
                    }
                }
                if(lever != null) {
                    if(lever.isDone()) {
                        for(int i = 0; i != gs.getCount(); i++) {
                            GameComponent c = gs.getComponent(i);

                            if(c instanceof Model) {
                                Model model = (Model)c;

                                if(model.getMeshName().equals("bridge.obj")) {
                                    if(gs.getNode(i).value.equals(leverNode.value)) {
                                        boolean raiseLower = (Boolean)gs.getNode(i).data.get("raise-lower");
                                        
                                        if(!raiseLower) {
                                            gs.getNode(i).data.put("raise-lower", true);

                                            boolean on = (Boolean)leverNode.data.get("on");

                                            SoundEngine.Sound sound = gs.game.getResources().load(new File("lever.wav"));

                                            sound.setVolume(0.25f);
                                            sound.play(false);

                                            if(on) {
                                                lever.setSequence(5, 9, 5, false);
                                            } else {
                                                lever.setSequence(0, 4, 5, false);
                                            }
                                            leverNode.data.put("on", !on);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            spaceDown = false;
        }
    }

    private boolean pick(Mesh mesh, Vec3 origin, Vec3 direction, float[] time) {
        boolean hit = false;

        if (mesh.bounds.intersectsRay(origin.x, origin.y, origin.z, direction.x, direction.y, direction.z)) {
            for(int p = 0; p != mesh.getPartCount(); p++) {
                MeshPart part = mesh.getPart(p);

                if (part.bounds.intersectsRay(origin.x, origin.y, origin.z, direction.x, direction.y, direction.z)) {
                    for(int i = 0; i != part.vertexElementBuffer.getCount(); i += 3) {
                        try {
                            int i1 = part.vertexElementBuffer.get(i + 0);
                            int i2 = part.vertexElementBuffer.get(i + 1);
                            int i3 = part.vertexElementBuffer.get(i + 2);
                            float x1 = part.vertexBuffer.get(i1 * 12 + 0);
                            float y1 = part.vertexBuffer.get(i1 * 12 + 1);
                            float z1 = part.vertexBuffer.get(i1 * 12 + 2);
                            float x2 = part.vertexBuffer.get(i2 * 12 + 0);
                            float y2 = part.vertexBuffer.get(i2 * 12 + 1);
                            float z2 = part.vertexBuffer.get(i2 * 12 + 2);
                            float x3 = part.vertexBuffer.get(i3 * 12 + 0);
                            float y3 = part.vertexBuffer.get(i3 * 12 + 1);
                            float z3 = part.vertexBuffer.get(i3 * 12 + 2);
                
                            triangle.p1.set(x1, y1, z1).mulPosition(mesh.model);
                            triangle.p2.set(x2, y2, z2).mulPosition(mesh.model);
                            triangle.p3.set(x3, y3, z3).mulPosition(mesh.model);
                            triangle.calcPlane();

                            float t = Intersection.intersectRayTriangle(origin, direction, triangle.p1, triangle.p2, triangle.p3, 1.0E-7F);
                            if ((double)t > 1.0E-8 && t < time[0]) {
                                time[0] = t;
                                hit = true;
                            }
                        } catch (Exception ex) {
                            Log.getInstance().put(0, ex);
                        }
                    }
                }
            }
        }
        return hit;
    }

    @Override
    public void render(GameSystem gs, GameNode node, SpriteRenderer spriteRenderer, BasicRenderer basicRenderer) throws Exception {
        Texture texture = gs.game.getResources().load(new File("sprites.png"));
        int s = gs.game.getScale();
        int w = gs.game.getFrameWidth();
        int h = gs.game.getFrameHeight();

        spriteRenderer.begin(texture);
        if(start) {
            spriteRenderer.push("click to start", 16, 10, 12, s * 2, 5, w / 2 - 10 * 7 * s * 2, h / 2 - 6 * s * 2, 1, 1, 1, 1, 0.5f, 0.5f, 0.5f, 1);
        } else {
            spriteRenderer.push(
                "FPS = " + gs.game.getFPS() + "\n" +
                "RES = " + Resource.getInstances() + "\n" +
                "TRI = " + basicRenderer.getTrianglesRendered() + "\n" +
                "TST = " + collider.getTested() + "\n" +
                "POS = " + (int)gs.eye.x + ", " + (int)gs.eye.y + ", " + (int)gs.eye.z + "\n",
                16, 10, 12, s * 2, 5, 10 * s, 10 * s, 1, 1, 0, 1, 1, 0.5f, 0, 1
            );
            if(!gs.inDesign()) {
                spriteRenderer.push(
                    14, 5, 1, 1,
                    w / 2 - 16 * s, h / 2 - 2 * s, 32 * s, 4 * s, 1, 0.5f, 0, 1, 1, 0.5f, 0, 1, false
                );
                spriteRenderer.push(
                    14, 5, 1, 1, 
                    w / 2 - 2 * s, h / 2 - 16 * s, 4 * s, 32 * s, 1, 0.5f, 0, 1, 1, 0.5f, 0, 1, false
                );
            }
        }
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
        start = true;
        spaceDown = false;
    }

    @Override
    public String toString() {
        return "player";
    }
}
