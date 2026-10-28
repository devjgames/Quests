import org.game.BasicRenderer;
import org.game.Vec4;

public class BasicRenderer2 extends BasicRenderer {

    public  BasicRenderer2() throws Exception {
    }

    @Override
    public void begin(int width, int height, Vec4 backgroundColor) throws Exception {
        super.begin(width, height, backgroundColor);

        setWarp(false, 0);
    }

    public void setWarp(boolean enabled, float time) {
        getShader().set("uWarp", enabled);
        getShader().set("uWarpTime", time);
    }

    @Override
    protected String getCustomVertexUniforms() {
        return 
            "uniform int uWarp;\n" +
            "uniform float uWarpTime;\n";
    }

    @Override
    protected String getCustomVertexMain() {
        return 
            "\tif(uWarp != 0) {\n" +
            "\t\tposition.x = position.x + 8.0 * cos(position.z * 0.05 + uWarpTime) * sin(position.y * 0.05 + uWarpTime);\n" +
            "\t\tposition.y = position.y + 8.0 * cos(position.x * 0.05 + uWarpTime) * sin(position.z * 0.05 + uWarpTime);\n" +
            "\t\tposition.z = position.z + 8.0 * cos(position.y * 0.05 + uWarpTime) * sin(position.x * 0.05 + uWarpTime);\n" +
            "\t}\n";
    }
}
