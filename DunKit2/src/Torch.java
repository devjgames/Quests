import java.io.File;
import java.util.Random;

import org.game.AABB;
import org.game.BasicRenderer;
import org.game.BlendState;
import org.game.GFX;
import org.game.GameLight;
import org.game.GameNode;
import org.game.GameSystem;
import org.game.Mesh;
import org.game.Particle;
import org.game.ParticleSystem;
import org.game.RenderPass;

public class Torch extends Model {

    private Random random = new Random(100);
    private Particle particle = new Particle();

    public Torch() {
        super("torch.obj");
    }

    @Override
    public GameLight getLight(GameSystem gs, GameNode node, int i) throws Exception {
        if(i == 0) {
            GameLight light = (GameLight)node.data.get("light");

            light.position.set(node.position).add(0, 32, 0);
            light.color.set(4, 2, 0, 1);
            light.radius = 75;

            return light;
        }
        return null;
    }
    
    @Override
    public void init(GameSystem gs, GameNode node) throws Exception {
        super.init(gs, node);

        node.data.put("light", new GameLight());

        ParticleSystem ps = gs.game.getResources().manage(new ParticleSystem(500));

        ps.texture = gs.game.getResources().load(new File("fire.png"));

        node.data.put("ps", ps);
    }

    @Override
    public void update(GameSystem gs, GameNode node) throws Exception {
        super.update(gs, node);

        ParticleSystem ps = (ParticleSystem)node.data.get("ps");
        float startColor = 0.15f + random.nextFloat() * 0.15f;
        float startSize = 5 + random.nextFloat() * 10;
        
        particle.velocityY = 10 + random.nextFloat() * 10;
        particle.positionX = -2 + random.nextFloat() * 4;
        particle.positionY = -2 + random.nextFloat() * 4 + 30;
        particle.positionZ = -2 + random.nextFloat() * 4;
        particle.startR = startColor;
        particle.startG = startColor;
        particle.startB = startColor;
        particle.startA = 1;
        particle.endR = 0;
        particle.endG = 0;
        particle.endB = 0;
        particle.endA = 1;
        particle.startX = startSize;
        particle.startY = startSize;
        particle.endX = 1;
        particle.endY = 1;
        particle.lifeSpan = 0.5f + random.nextFloat() * 1.5f;

        ps.emit(particle, gs);
    }

    @Override
    public void render(GameSystem gs, GameNode node, BasicRenderer renderer, RenderPass renderPass) throws Exception {
        super.render(gs, node, renderer, renderPass);

        if(renderPass != RenderPass.TRANSPARENT) {
            return ;
        }

        GFX.setBlendState(BlendState.ADDITIVE);

        ParticleSystem ps = (ParticleSystem)node.data.get("ps");
        Mesh mesh = (Mesh)node.data.get("mesh");

        mesh.calcBoundsAndTransform(node);

        ps.update(gs, mesh.model);

        AABB b = ps.getBounds();

        b.transform(mesh.model);

        if(gs.frustum.testAab(b)) {
            renderer.setModel(mesh.model);
            renderer.setModelIT(mesh.modelIT);
            renderer.setLightingEnabled(false);
            renderer.setTexture(ps.texture);
            ((BasicRenderer2)renderer).setWarp(false, 0);
            ((BasicRenderer2)renderer).setDecal(null);

            renderer.draw(ps.vertexBuffer);
        }
    }

    @Override
    public String toString() {
        return "torch";
    }
}
