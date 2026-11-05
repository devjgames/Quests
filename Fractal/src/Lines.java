import java.io.File;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.game.IOHelper;
import org.game.Log;
import org.game.Vec2;

public class Lines {

    public static final int MAX_LINES = 64;
    
    private Vector<Vec2> points = new Vector<>();
    private int[] lines = new int[MAX_LINES];
    private int lineCount = 0;

    public Lines() throws Exception {
        init();
    }

    void init() throws Exception {
        clear();

        addPoint(-0.5f, -0.5f);
        addPoint(+0.5f, -0.5f);
        addPoint(+0.0f, +0.5f);

        addLine(0, 1);
        addLine(1, 2);
        addLine(2, 0);
    }

    public int getCount() {
        return lineCount;
    }

    public void addPoint(float x, float y) {
        points.add(new Vec2(x, y));
    }

    void clear() {
        points.clear();
        lineCount = 0;
    }

    public void addLine(int i, int j) throws Exception {
        if(i < 0 || i >= points.size() || j < 0 || j >= points.size()) {
            throw new Exception("point indice(s) out of bounds");
        }
        lines[lineCount++] = i;
        lines[lineCount++] = j;
    }

    float getX(int i) {
        return points.get(lines[i]).x;
    }

    float getY(int i) {
        return points.get(lines[i]).y;
    }

    public boolean eval(File file) throws Exception {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByExtension("js");

            clear();

            engine.put("lines", this);
            engine.eval(new String(IOHelper.readAllBytes(file)));

            return true;
        } catch(Exception ex) {
            Log.getInstance().put(0, ex);
            init();
        }
        return false;
    }
}
