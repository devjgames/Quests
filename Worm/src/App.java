import org.game.Game;
import org.game.GameSystem;

public class App {
    
    public static void main(String[] args) throws Exception {
        Game game = null;

        try {
            game = new Game(900, 600, "Worm", true);

            game.getResources().setLoader((file, extension) -> {
                if(file.isDirectory()) {
                    return KeyFrameMesh.load(file);
                }
                return null;
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
