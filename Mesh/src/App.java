import java.io.File;
import java.util.Vector;

import org.game.BlendState;
import org.game.CullState;
import org.game.DepthState;
import org.game.GFX;
import org.game.Game;
import org.game.Resource;
import org.game.ResourceManager;
import org.game.SpriteRenderer;
import org.game.Texture;
import org.game.UI;
import org.game.UIColors;

public class App {
    public static void main(String[] args) throws Exception {
        Game game = null;

        try {
            game = new Game(900, 600, "Mesh", true);

            ResourceManager resources = null;
            SpriteRenderer spriteRender = game.getResources().manage(new SpriteRenderer());
            Texture sprites = game.getResources().load(new File("sprites.png"));
            UI ui = new UI(spriteRender, sprites, 16, 10, 12, 14, 5);
            Vector<Object> demos = new Vector<>();
            int iDemo = -1;
            int selDemo = -1;
            int s = game.getScale();
            boolean running = false;

            demos.add(new Basic1());
            demos.add(new Basic2());
            demos.add(new Basic3());
            demos.add(new Basic4());

            if(!demos.isEmpty()) {
                selDemo = 0;
            }

            while(game.beginFrame()) {
                int x = 220 * s;
                int y = 10 * s;
                int w = game.getFrameWidth() - 230 * s;
                int h = game.getFrameHeight() - 20 * s;

                if(running) {
                    Demo demo = (Demo)demos.get(iDemo);

                    demo.render(game, resources, w, h);
                }
                GFX.clear(UIColors.BACKGROUND);
                GFX.setDepthState(DepthState.NONE);
                GFX.setBlendState(BlendState.ALPHA);
                GFX.setCullState(CullState.BACK);
                ui.begin();
                ui.moveTo(10, 10);
                if(!running) {
                    iDemo = ui.list("App.demo.list", demos, 16, 16, selDemo, 0);
                    if(iDemo >= 0) {
                        ui.addRow(5);
                        if(ui.button("App.run.button", "Run", false, 0)) {
                            Demo demo = (Demo)demos.get(iDemo);

                            resources = game.getResources().manage(new ResourceManager());

                            demo.init(game, resources);
                            running = true;
                        }
                    }
                }
                if(running) {
                    Demo demo = (Demo)demos.get(iDemo);

                    demo.ui(game, resources, ui);
                    ui.addRow(5);
                    if(ui.button("App.quit.button", "Quit", false, 0)) {
                        game.getResources().unManage(resources);
                        running = false;
                    }
                }
                if(ui.button("App.si.button", "SI", game.isSwapOne(), 5)) {
                    game.toggleSwapInterval();
                }
                if(ui.button("App.fs.button", "FS", game.isFullscreen(), 5)) {
                    game.toggleFullscreen();
                }
                ui.addRow(5);
                ui.label("App.info.label", "F:" + game.getFPS() + ", R:" + Resource.getInstances(), 16, 0);
                ui.end(game);
                if(running) {
                    Demo demo = (Demo)demos.get(iDemo);

                    demo.renderTargets(game, resources, x, y, w, h, spriteRender);

                    GFX.setViewport(x, y, w, h);
                    spriteRender.setProjection(w, h);
                    demo.render(game, resources, sprites, spriteRender);
                }
                game.endFrame();

                if(running) {
                    Demo demo = (Demo)demos.get(iDemo);

                    demo.update(game, resources, x, y, w, h);
                }

                selDemo = -2;
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }
}
