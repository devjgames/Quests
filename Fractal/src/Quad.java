import org.game.BufferUsage;
import org.game.Log;
import org.game.Resource;
import org.game.VertexArrayBuffer;

public class Quad extends Resource {
    
    public final VertexArrayBuffer vertexBuffer;

    public Quad() {
        Log.getInstance().put(0, "creating quad ...");
        vertexBuffer = new VertexArrayBuffer(0);
        vertexBuffer.begin(2 * 6);
        vertexBuffer.push(0, 0);
        vertexBuffer.push(1, 0);
        vertexBuffer.push(1, 1);
        vertexBuffer.push(1, 1);
        vertexBuffer.push(0, 1);
        vertexBuffer.push(0, 0);
        vertexBuffer.end(BufferUsage.STATIC);
    }

    @Override
    public void destroy() {
        vertexBuffer.destroy();
        super.destroy();
    }
}
