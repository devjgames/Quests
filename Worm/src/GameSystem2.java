import org.game.BasicRenderer;
import org.game.GameSystem;
import org.game.Log;
import org.game.OutlineRenderer;
import org.game.RenderTarget;

public class GameSystem2 extends GameSystem {

    private OutlineRenderer outlineRenderer = null;
    
    public GameSystem2() throws Exception {
        super(
            "scene1.scene", true, true,
            new Worm()
        );
    }

    @Override
    protected void clearPostProcess() {
        outlineRenderer = null;
    }

    @Override
    protected void postProcess(BasicRenderer renderer) throws Exception {
        boolean created = false;

        if(outlineRenderer == null) {
            Log.getInstance().put(0, "creating outline renderer ...");

            outlineRenderer = game.getResources().manage(new OutlineRenderer());
            created = true;
        }

        RenderTarget target = renderer.getRenderTarget();

        outlineRenderer.render(target.getTexture(1), game.getScale() * 2, target.getWidth(), target.getHeight());

        if(created) {
            outlineRenderer.getRenderTarget().getTexture(0).toNearest(true);
        }
    }

    @Override
    protected RenderTarget getPostProcessTarget() throws Exception {
        return outlineRenderer.getRenderTarget();
    }
}
