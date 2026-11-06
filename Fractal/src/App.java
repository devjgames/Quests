import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.game.BlendState;
import org.game.ColorFormat;
import org.game.CullState;
import org.game.DepthState;
import org.game.GFX;
import org.game.Game;
import org.game.IOHelper;
import org.game.Log;
import org.game.RenderTarget;
import org.game.SpriteRenderer;
import org.game.Texture;
import org.game.UI;
import org.game.UIColors;
import org.game.Vec4;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;

public class App {

    static boolean render = true;
    static Vector<Object> scripts = new Vector<>();
    static int selectedScript = -2;
    static int iScript = -1;

    static void populate() {
        File[] files = new File("assets", "lines").listFiles();

        scripts.clear();
        if(files != null) {
            for(File file : files) {
                if(file.getName().endsWith(".js")) {
                    scripts.add(file.getName());
                }
            }
        }
        scripts.sort((a, b) -> a.toString().compareTo(b.toString()));
        selectedScript = -1;
        iScript = -1;
    }

    static void openFile(File file) throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb;

        if (os.contains("win")) {
            pb = new ProcessBuilder("cmd.exe", "/c", "start", "", file.getAbsolutePath());
        } else if (os.contains("mac")) {
            pb = new ProcessBuilder("open", file.getAbsolutePath());
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            pb = new ProcessBuilder("xdg-open", file.getAbsolutePath());
        } else {
            throw new UnsupportedOperationException("Platform not supported: " + os);
        }

        pb.start();
    }

    public static void main(String[] args) throws Exception {
        Game game = null;

        try {
            game = new Game(900, 700, "Fractal", true);

            Quad quad = game.getResources().manage(new Quad());
            Fractal fractal = game.getResources().manage(new Fractal());
            SpriteRenderer spriteRenderer = game.getResources().manage(new SpriteRenderer());
            UI ui = new UI(
                spriteRenderer, 
                game.getResources().load(new File("sprites.png")), 16, 10, 12, 14, 5
            );
            RenderTarget target = game.getResources().manage(new RenderTarget(1024, 1024, false, ColorFormat.COLOR));
            Texture arrow = game.getResources().load(new File("arrow.png"));
            boolean down = false;
            int s = game.getScale();
            boolean setText = true;
            String r = null;
            String scriptName = "Line.js";
            File imageFile = new File("images", "image1.png");
            float amount = 0.9f;
            int count = 7;
            String info = "";
            int i = -2;
            boolean doPop = false;
            boolean renderImage = false;
            int drag = -1;
            int lastDrag = -1;
            boolean setColor = false;

            GLFW.glfwSetWindowSizeCallback(game.getWindow(), new GLFWWindowSizeCallbackI() {

                @Override
                public void invoke(long window, int w, int h) {
                    render = true;
                }
                
            });

            populate();

            target.getTexture(0).bind();
            target.getTexture(0).toLinear(true);
            target.getTexture(0).unBind();

            while(game.beginFrame()) {
                int x = 200 * s;
                int y = 10 * s;
                int w = game.getFrameWidth() - (210 + 32 + 8) * s;
                int h = game.getFrameHeight() - 60 * s;

                if(doPop) {
                    populate();
                    doPop = false;
                }

                if(renderImage) {
                    fractal.plot(target, quad, target.getWidth(), target.getHeight());
                    target.getTexture(0).bind();
                    target.getTexture(0).save(imageFile);
                    target.getTexture(0).unBind();
                    renderImage = false;
                }

                if(drag != -1 || lastDrag != -1) {
                    fractal.gradient.render(quad);
                }

                if(render) {
                    fractal.plot(quad, w, h);
                    render = false;
                }
                GFX.clear(UIColors.BACKGROUND);
                GFX.setDepthState(DepthState.NONE);
                GFX.setBlendState(BlendState.ALPHA);
                GFX.setCullState(CullState.NONE);
                ui.begin();
                ui.moveTo(10, 10);
                if((r = ui.text("image.name.text", "image1.png", 10, setText, 0)) != null) {
                    r = r.trim();
                    if(!r.isEmpty() && r.endsWith(".png")) {
                        imageFile = new File("images", r);
                    }
                }
                if(ui.button("save.image.button", "Save", false, 5)) {
                    renderImage = true;
                }
                if(ui.button("init.button", "Init", false, 5)) {
                    fractal.initDimensions();
                    render = true;
                }
                if((r = ui.text("amount.text", "" + amount, 5, setText, 5)) != null) {
                    try {
                        float a = Float.parseFloat(r.trim());

                        amount = a;
                    } catch(NumberFormatException ex) {
                    }
                }
                if(ui.button("render.button", "Render", false, 5)) {
                    render = true;
                }
                if((r = ui.text("seed.count.text", "7", 5, setText, 5)) != null) {
                    try {
                        int ic = Integer.parseInt(r.trim());

                        count = ic;
                    } catch(NumberFormatException ex) {
                    }
                }
                if(ui.button("rand.button", "Gen", false, 5)) {
                    fractal.gradient.generate(count);
                    render = true;
                    drag = -1;
                    lastDrag = -1;
                }
                ui.label("info.label", info, 50, 5);
                ui.addRow(10);
                i = ui.list("scripts.list", scripts, 4, 15, selectedScript, 0);
                if(i != -1) {
                    iScript = i;
                }
                ui.addRow(5);
                if((r = ui.text("script.text", scriptName, 16, setText, 0)) != null) {
                    scriptName = r;
                }
                ui.addRow(5);
                if(ui.button("add.script.button", "+", false, 0)) {
                    info = "";
                    try {
                        File file = new File(new File("assets", "lines"), scriptName);

                        Log.getInstance().put(0, file);
                        IOHelper.writeAllBytes(
                            "lines.addPoint(-0.5, -0.5);\nlines.addPoint(+0.5, +0.5);\nlines.addLine(0, 1);\n".getBytes(), file
                        );
                        populate();
                    } catch(Exception ex) {
                        info = "failed to write file";

                        Log.getInstance().put(0, ex);
                    }
                }
                if(iScript >= 0) {
                    if(ui.button("del.script.button", "-", false, 5)) {
                        File file = new File(new File("assets", "lines"), scripts.get(iScript).toString());

                        if(file.exists()) {
                            file.delete();
                        }
                        doPop = true;
                    }
                    if(ui.button("edit.script.button", "Edit", false, 5)) {
                        File file = new File(new File("assets", "lines"), scripts.get(iScript).toString());

                        openFile(file);
                    }
                    if(ui.button("eval.script.button", "Val", false, 5)) {
                        info = "";
                        if(!fractal.lines.eval(new File(new File("assets", "lines"), scripts.get(i).toString()))) {
                            info = "failed to eval script";
                        }
                        render = true;
                    }
                }
                ui.addRow(5);
                if((r = ui.text("julia.seed.text", "" + fractal.juliaX + " " + fractal.juliaY, 10, setText, 0)) != null) {
                    try {   
                        String[] tokens = r.split("\\s+");

                        if(tokens.length >= 2) {
                            float ix = Float.parseFloat(tokens[0]);
                            float iy = Float.parseFloat(tokens[1]);

                            fractal.juliaX = ix;
                            fractal.juliaY = iy;
                        }
                    } catch(NumberFormatException ex) {
                    }
                }
                if(ui.button("julia.button", "Jul", fractal.julia, 5)) {
                    fractal.julia = !fractal.julia;
                    render = true;
                }
                ui.addRow(5);
                ui.label("max.count.label", "Max Count", 15, 0);
                ui.addRow(0);
                if((r = ui.text("max.count.text", "" + fractal.maxCount, 16, setText, 0)) != null) {
                    try {
                        int ix = Integer.parseInt(r.trim());

                        if(ix >= 100 && ix <= 1000) {
                            fractal.maxCount = ix;
                        }
                    } catch(NumberFormatException ex) {
                    }
                }
                ui.addRow(5);
                ui.label("bailout.label", "Bailout", 15, 0);
                ui.addRow(0);
                if((r = ui.text("bailout.text", "" + fractal.bailout, 16, setText, 0)) != null) {
                    try {
                        float ix = Float.parseFloat(r);

                        fractal.bailout = ix;
                    } catch(NumberFormatException ex) {
                    }
                }
                ui.addRow(5);
                ui.label("frequency.label", "Frequency", 15, 0);
                ui.addRow(0);
                if((r = ui.text("frequency.text", "" + fractal.frequency, 16, setText, 0)) != null) {
                    try {
                        float ix = Float.parseFloat(r);

                        fractal.frequency = ix;
                    } catch(NumberFormatException ex) {
                    }
                }
                ui.addRow(5);
                ui.label("scale.label", "Scale", 15, 0);
                ui.addRow(0);
                if((r = ui.text("scale.text", "" + fractal.scaleX + " " + fractal.scaleY, 16, setText, 0)) != null) {
                    try {
                        String[] tokens = r.trim().split("\\s+");

                        if(tokens.length >= 2) {
                            float ix = Float.parseFloat(tokens[0]);
                            float iy = Float.parseFloat(tokens[1]);

                            fractal.scaleX = ix;
                            fractal.scaleY = iy;
                        }
                    } catch(NumberFormatException ex) {
                    }
                }
                ui.addRow(5);
                ui.label("center.label", "Center", 15, 0);
                ui.addRow(0);
                if((r = ui.text("center.text", "" + fractal.centerX + " " + fractal.centerY, 16, setText, 0)) != null) {
                    try {
                        String[] tokens = r.trim().split("\\s+");

                        if(tokens.length >= 2) {
                            float ix = Float.parseFloat(tokens[0]);
                            float iy = Float.parseFloat(tokens[1]);

                            fractal.centerX = ix;
                            fractal.centerY = iy;
                        }
                    } catch(NumberFormatException ex) {
                    }
                }
                if(lastDrag != -1) {
                    ColorEntry c = fractal.gradient.get(lastDrag);

                    ui.addRow(5);
                    c.color.x = ui.slider("r.slider", 15, c.color.x, setColor, 0);
                    ui.addRow(5);
                    c.color.y = ui.slider("g.slider", 15, c.color.y, setColor, 0);
                    ui.addRow(5);
                    c.color.z = ui.slider("b.slider", 15, c.color.z, setColor, 0);

                    if(lastDrag == 0) {
                        Vec4 color = c.color;
                        
                        c = fractal.gradient.get(fractal.gradient.getCount() - 1);
                        c.color.set(color.x, color.y, color.z, 1);
                    }
                }
                ui.end(game);
                spriteRenderer.begin(fractal.gradient.getTarget().getTexture(0));
                spriteRenderer.push(
                    0, 0, fractal.gradient.getTarget().getWidth(), fractal.gradient.getTarget().getHeight(),
                    x + w + 10 * s, 50 * s, fractal.gradient.getTarget().getWidth() * s, fractal.gradient.getTarget().getHeight() * s,
                    1, 1, 1, 1, 1, 1, 1, 1, false
                );
                spriteRenderer.end();
                spriteRenderer.begin(arrow);
                for(int e = 1; e != fractal.gradient.getCount() - 1; e++) {
                    float p = fractal.gradient.get(e).position;
                    int py = 50 * s + (int)(p * fractal.gradient.getTarget().getHeight()) * s;

                    spriteRenderer.push(
                        0, 0, arrow.width, arrow.height,
                        x + w + 10 * s + 16 * s, py - arrow.height / 2 * s, arrow.width / 2 * s, arrow.height / 2 * s,
                        1, 1, 1, 1, 1, 1, 1, 1, false
                    );
                }
                spriteRenderer.end();
                GFX.setViewport(x, y, w, h);
                spriteRenderer.setProjection(w, h);
                spriteRenderer.begin(fractal.getTarget().getTexture(0));
                spriteRenderer.push(0, 0, w, h, 0, 0, w, h, 1, 1, 1, 1, 1, 1, 1, 1, true);
                spriteRenderer.end();
                game.endFrame();

                setText = false;
                selectedScript = -2;
                setColor = false;

                int mx = game.getMouseX() * s - x;
                int my = h - ((h + 50 * s) - game.getMouseY() * s);

                if(game.isMouseButtonDown(0)) {
                    if(!down) {
                        down = true;
                        if(mx >= 0 && mx <= w && my >= 0 && my <= h) {
                            fractal.panZoom(mx, my, w, h, amount);
                            render = true;
                        }

                        mx = game.getMouseX() * s;
                        my = game.getMouseY() * s;

                        if(mx > x + w + 10 * s && mx < x + w + 10 * s + 16 * s && my > 50 * s && my < 50 * s + fractal.gradient.getTarget().getHeight() * s) {
                            lastDrag = 0;
                            setColor = true;
                        } else {
                            for(int e = 1; e != fractal.gradient.getCount() - 1; e++) {
                                float p = fractal.gradient.get(e).position;
                                int py = 50 * s + (int)(p * fractal.gradient.getTarget().getHeight()) * s;
                                int x1 = x + w + 10 * s + 16 * s;
                                int y1 = py - arrow.height / 2 * s;
                                int x2 = x1 + arrow.width / 2 * s;
                                int y2 = y1 + arrow.height / 2 * s;

                                if(mx >= x1 && mx <= x2 && my >= y1 && my <= y2) {
                                    drag = e;
                                    lastDrag = e;
                                    setColor = true;
                                    break;
                                }
                            }
                        }
                    }
                    if(drag != -1) {
                        my = game.getMouseY() - 50;

                        float p = my / (float)fractal.gradient.getTarget().getHeight();

                        p = Math.max(0, Math.min(1, p));
                        fractal.gradient.get(drag).position = p;

                        ColorEntry e = fractal.gradient.get(drag);

                        fractal.gradient.sort();

                        for(int ie = 1; ie != fractal.gradient.getCount() - 1; ie++) {
                            if(fractal.gradient.get(ie) == e) {
                                lastDrag = ie;
                                drag = ie;
                                break;
                            }
                        }
                    }
                } else {
                    if(drag != -1) {
                        render = true;
                    }
                    drag = -1;
                    down = false;
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }
}
