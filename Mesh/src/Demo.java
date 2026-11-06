import org.game.Game;
import org.game.ResourceManager;
import org.game.SpriteRenderer;
import org.game.Texture;
import org.game.UI;

public abstract class Demo {
    
    public abstract void init(Game game, ResourceManager resources) throws Exception;

    public abstract void render(Game game, ResourceManager resources, int w, int h) throws Exception;

    public abstract void renderTargets(Game game, ResourceManager resources, int x, int y, int w, int h, SpriteRenderer spriteRenderer) throws Exception;

    public abstract void render(Game game, ResourceManager resources, Texture font, SpriteRenderer spriteRenderer) throws Exception;

    public abstract void update(Game game, ResourceManager resources, int x, int y, int w, int h) throws Exception;

    public abstract void ui(Game game, ResourceManager resources, UI ui) throws Exception;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
