import org.game.GameLight;
import org.game.GameNode;
import org.game.GameSystem;

public class Lever extends KFModel {
    
    public Lever() {
        super("lever");
    }

    @Override
    public GameLight getLight(GameSystem gs, GameNode node, int i) throws Exception {
        if(i == 0) {
            GameLight light = (GameLight)node.data.get("light");

            if(light == null) {
                node.data.put("light", light = new GameLight());
            }

            light.radius = 75;
            light.color.set(0, 2, 4, 1);
            light.position.set(node.position).add(0, 50, 0);

            return light;
        }
        return null;
    }
}
