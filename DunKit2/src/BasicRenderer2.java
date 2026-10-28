import org.game.BasicRenderer;
import org.game.Texture;
import org.game.Vec4;

public class BasicRenderer2 extends BasicRenderer {

    public  BasicRenderer2() throws Exception {
    }

    @Override
    public void begin(int width, int height, Vec4 backgroundColor) throws Exception {
        super.begin(width, height, backgroundColor);

        setWarp(false, 0);
        setDecal(null);
    }

    public void setWarp(boolean enabled, float time) {
        getShader().set("uWarp", enabled);
        getShader().set("uWarpTime", time);
    }

    public void setDecal(Texture decal) {
        getShader().set("uDecalEnabled", decal != null);
        if(decal != null) {
            getShader().set("uDecal", decal, 1);
        }
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
            "\t\tposition.x = position.x + 2.0 * cos(position.z * 0.25 + uWarpTime) * sin(position.y * 0.25 + uWarpTime);\n" +
            "\t\tposition.y = position.y + 2.0 * cos(position.x * 0.25 + uWarpTime) * sin(position.z * 0.25 + uWarpTime);\n" +
            "\t\tposition.z = position.z + 2.0 * cos(position.y * 0.25 + uWarpTime) * sin(position.x * 0.25 + uWarpTime);\n" +
            "\t}\n";
    }

    @Override
    protected String getCustomFragmentUniforms() {
        return 
            "uniform sampler2D uDecal;\n" +
            "uniform int uDecalEnabled;\n";
    }

    @Override
    protected String getCustomFragmentMain() {
        return
            "\tif(uDecalEnabled != 0) {\n" +
            "\t\tvec4 c = texture(uDecal, vTextureCoordinate);\n" +
            "\t\tcolor.rgb = (1.0 - c.a) * color.rgb + c.a * c.rgb;\n" +
            "\t}\n";
    }
}
