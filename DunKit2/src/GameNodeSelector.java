import org.game.AABB;
import org.game.GameNode;
import org.game.Log;
import org.game.Mesh;
import org.game.TriangleSelector;

public class GameNodeSelector implements TriangleSelector {

    private GameNode node;

    public GameNodeSelector(GameNode node) {
        this.node = node;
    }

    public GameNode getNode() {
        return node;
    }

    @Override
    public void select(AABB bounds, Object userData, Visitor vistitor) {
        try {
            Object obj = node.data.get("mesh");

            if(obj instanceof KeyFrameMesh) {
                KeyFrameMesh mesh = (KeyFrameMesh)obj;

                mesh.getMesh().calcBoundsAndTransform(node);
                mesh.getMesh().select(bounds, userData, vistitor);
            } else {
                Mesh mesh = (Mesh)obj;

                mesh.calcBoundsAndTransform(node);
                mesh.select(bounds, userData, vistitor);
            }
        } catch(Exception ex) {
            Log.getInstance().put(0, ex);
        }
    }

}