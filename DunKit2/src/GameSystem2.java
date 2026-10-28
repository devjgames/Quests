import org.game.BasicRenderer;
import org.game.GameSystem;

public class GameSystem2 extends GameSystem {
    
    public GameSystem2() throws Exception {
        super(
            "scene1.scene", true, true,
            new Model("coins.obj"), 
            new Model("corner.obj"),
            new Model("door-way.obj"),
            new Model("door.obj"),
            new Model("energy.obj"), 
            new Model("flats.obj"),
            new Model("hall-corner.obj"),
            new Model("hall-end.obj"),
            new Model("hall-ramp.obj"),
            new Model("hall-short.obj"),
            new Model("hall-side.obj"),
            new Model("orb.obj"),
            new Model("side.obj"),
            new Model("axis.obj"),
            new Torch(),
            new Player()
        );
    }

    @Override
    protected BasicRenderer createRenderer() throws Exception {
        return new BasicRenderer2();
    }

}
