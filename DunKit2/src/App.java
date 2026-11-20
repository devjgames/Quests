import java.io.File;

import org.game.Game;
import org.game.GameSystem;
import org.game.ResourceManager.Loader;

public class App {
    
    public static void main(String[] args) throws Exception {
        Game game = null;

        try {
            game = new Game(900, 600, "DunKit", true);

            game.getResources().setLoader(new Loader() {

                @Override
                public Object load(File file, String extension) throws Exception {
                    if(file.isDirectory()) {
                        return KeyFrameMesh.load(file);
                    }
                    return null;
                }
                
            });

            GameSystem gs = new GameSystem2();

            game.resetTimer();

            while(game.beginFrame()) {
                gs.update();
                game.endFrame();
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }
}
