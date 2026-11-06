import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.function.IntBinaryOperator;

import org.game.BasicRenderer;
import org.game.BufferUsage;
import org.game.Vec2;
import org.game.Vec3;
import org.game.VertexArrayBuffer;
import org.game.VertexElementBuffer;
import org.joml.GeometryUtils;

public class HEMesh {
 
    public static class Vertex {
        public int index;
        public int edge = -1;
        public final Vec3 position = new Vec3();
        public final Vec3 normal = new Vec3();
 
        public Vertex(int index) {
            this.index = index;
        }
 
        public Vertex(Vertex vertex) {
            index = vertex.index;
            edge = vertex.edge;
            position.set(vertex.position);
            normal.set(vertex.normal);
        }
    }
 
    public static class Edge {
        public int index;
        public int pair = -1;
        public int next = -1;
        public int vertex = -1;
        public int face = -1;
        public final Vec2 textureCoordinate = new Vec2();
        public final Vec3 normal = new Vec3();
 
        public Edge(int index) {
            this.index = index;
        }
 
        public Edge(Edge edge) {
            index = edge.index;
            pair = edge.pair;
            next = edge.next;
            vertex = edge.vertex;
            face = edge.face;
            textureCoordinate.set(edge.textureCoordinate);
            normal.set(edge.normal);
        }
    }
 
    public static class Face {
        public int index;
        public int edge = -1;
        public final Vec3 normal = new Vec3();
 
        public Face(int index) {
            this.index = index;
        }
 
        public Face(Face face) {
            index = face.index;
            edge = face.edge;
            normal.set(face.normal);
        }
    }
 
    private final Vector<Vertex> vertices = new Vector<>();
    private final Vector<Edge> edges = new Vector<>();
    private final Vector<Face> faces = new Vector<>();
    private final Hashtable<String, Edge> vertexEdges = new Hashtable<>();
 
    public HEMesh() {
    }
 
    public HEMesh(HEMesh mesh) {
        set(mesh);
    }
 
    public String keyFor(int v1, int v2) {
        if(v1 > v2) {
            int t = v1;
 
            v1 = v2;
            v2 = t;
        }
        return v1 + ":" + v2;
    }
 
    public int getVertexCount() {
        return vertices.size();
    }
 
    public Vertex getVertex(int i) {
        return vertices.get(i);
    }
 
    public Vertex addVertex(float x, float y, float z) {
        Vertex vertex = new Vertex(vertices.size());
 
        vertex.position.set(x, y, z);
        vertices.add(vertex);
 
        return  vertex;
    }
 
    public Vertex addVertex(Vec3 position) {
        return addVertex(position.x, position.y, position.z);
    }
 
    public int getEdgeCount() {
        return edges.size();
    }
 
    public Edge getEdge(int i) {
        return edges.get(i);
    }
 
    public int getFaceCount() {
        return faces.size();
    }
 
    public Face getFace(int i) {
        return faces.get(i);
    }
 
    public Face addFace(int ... indices) throws Exception {
        if(indices.length < 3) {
            throw new Exception("indices < 3");
        }
 
        Face face = new Face(faces.size());
 
        face.edge = edges.size();
        for(int i = 0; i != indices.length; i++) {
            int idx = indices[i];
 
            if(idx < 0 || idx >= vertices.size()) {
                throw new Exception("index out of bounds");
            }
 
            Edge edge = new Edge(edges.size());
            Vertex vertex = getVertex(idx);
 
            if(vertex.edge == -1) {
                vertex.edge = edge.index;
            }
            edge.face = face.index;
            edge.vertex = vertex.index;
            if(i > 0) {
                edges.get(face.edge + i - 1).next = edge.index;
 
                if(i == indices.length - 1) {
                    edge.next = face.edge;
                }
            }
            edges.add(edge);
        }
        for(int i = 0; i != indices.length; i++) {
            Edge edge1 = edges.get(face.edge + i);
            Edge edge2 = edges.get(edge1.next);
            int v1 = vertices.get(edge1.vertex).index;
            int v2 = vertices.get(edge2.vertex).index;
            String key = keyFor(v1, v2);
 
            if(vertexEdges.containsKey(key)) {
                Edge pair = vertexEdges.get(key);
 
                if(pair.pair != -1) {
                    throw new Exception("edge already has a pair");
                }
                pair.pair = edge1.index;
                edge1.pair = pair.index;
            } else {
                vertexEdges.put(key, edge1);
            }
        }
        faces.add(face);
 
        return face;
    }
 
    public void clear() {
        vertices.clear();
        edges.clear();
        faces.clear();
        vertexEdges.clear();
    }
 
    public void set(HEMesh mesh) {
        clear();
 
        for(Vertex vertex : mesh.vertices) {
            vertices.add(new Vertex(vertex));
        }
        for(Edge edge : mesh.edges) {
            edges.add(new Edge(edge));
        }
        for(Face face : mesh.faces) {
            faces.add(new Face(face));
        }
        for(String key : mesh.vertexEdges.keySet()) {
            vertexEdges.put(key, edges.get(mesh.vertexEdges.get(key).index));
        }
    }
 
    public void translate(float x, float y, float z) {
        for(Vertex vertex : vertices) {
            vertex.position.add(x, y, z);
        }
    }
 
    public void translate(Vec3 translation) {
        translate(translation.x, translation.y, translation.z);
    }
 
    public void calcTextureCoordinate(float units) {
        for(Edge edge : edges) {
            Vec3 n = faces.get(edge.face).normal;
            Vec3 p = vertices.get(edge.vertex).position;
            Vec2 t = edge.textureCoordinate;
            float x = Math.abs(n.x);
            float y = Math.abs(n.y);
            float z = Math.abs(n.z);
 
            if(x > y && x > z) {
                t.set(p.z, p.y).div(units);
            } else if(y > x && y > z) {
                t.set(p.x, p.z).div(units);
            } else {
                t.set(p.x, p.y).div(units);
            }
        }
    }
 
    public void perturb(Random random, float min, float max) {
        for(Vertex vertex : vertices) {
            Vec3 n = vertex.normal;
            float a = min + random.nextFloat() * (max - min);
 
            vertex.position.add(n.x * a, n.y * a, n.z * a);
        }
    }
 
    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------
 
    // the edge in the same face whose next is the given edge
    private Edge prevEdge(Edge edge) {
        Edge p = edge;
 
        while(edges.get(p.next) != edge) {
            p = edges.get(p.next);
        }
        return p;
    }
 
    // collects every edge that starts at the given vertex, walking around the vertex.
    // returns true if the vertex lies on a boundary (open fan) rather than a closed ring
    private boolean collectRing(Vertex vertex, List<Edge> out) {
        Edge start = edges.get(vertex.edge);
        Edge e = start;
        boolean boundary = false;
 
        do {
            out.add(e);
            if(e.pair == -1) {
                boundary = true;
                break;
            }
            e = edges.get(edges.get(e.pair).next);
        } while(e != start);
 
        if(boundary) {
            e = start;
            while(true) {
                Edge p = prevEdge(e);
 
                if(p.pair == -1) {
                    break;
                }
                e = edges.get(p.pair);
                if(e == start) {
                    break;
                }
                out.add(e);
            }
        }
        return boundary;
    }
 
    private Face addFaceUnchecked(int ... indices) {
        try {
            return addFace(indices);
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }
 
    // vertex index loops and per corner texture coordinates for every face
    private void snapshotFaces(List<int[]> indices, List<Vec2[]> uvs) {
        for(Face face : faces) {
            List<Integer> vs = new ArrayList<>();
            List<Vec2> ts = new ArrayList<>();
            Edge first = edges.get(face.edge);
            Edge edge = first;
 
            do {
                vs.add(edge.vertex);
                ts.add(vec2(edge.textureCoordinate.x, edge.textureCoordinate.y));
                edge = edges.get(edge.next);
            } while(edge != first);
 
            int[] idx = new int[vs.size()];
 
            for(int i = 0; i != idx.length; i++) {
                idx[i] = vs.get(i);
            }
            indices.add(idx);
            uvs.add(ts.toArray(new Vec2[0]));
        }
    }
 
    // throws away all faces and edges and rebuilds them from the given loops, vertices are kept
    private void rebuildFaces(List<int[]> indices, List<Vec2[]> uvs) {
        edges.clear();
        faces.clear();
        vertexEdges.clear();
        for(Vertex vertex : vertices) {
            vertex.edge = -1;
        }
        for(int i = 0; i != indices.size(); i++) {
            Face face = addFaceUnchecked(indices.get(i));
            Vec2[] t = uvs.get(i);
 
            for(int k = 0; k != t.length; k++) {
                edges.get(face.edge + k).textureCoordinate.set(t[k].x, t[k].y);
            }
        }
    }
 
    private static Vec2 vec2(float x, float y) {
        Vec2 v = new Vec2();
 
        v.set(x, y);
        return v;
    }
 
    private static Vec2 mid(Vec2 a, Vec2 b) {
        return vec2((a.x + b.x) * 0.5f, (a.y + b.y) * 0.5f);
    }
 
    // ------------------------------------------------------------------
 
    public void calcNormals(boolean smooth) {
        for(Face face : faces) {
            Edge e1 = edges.get(face.edge);
            Edge e2 = edges.get(e1.next);
            Edge e3 = edges.get(e2.next);
            Vec3 p1 = vertices.get(e1.vertex).position;
            Vec3 p2 = vertices.get(e2.vertex).position;
            Vec3 p3 = vertices.get(e3.vertex).position;
 
            GeometryUtils.normal(p1, p2, p3, face.normal);
 
            face.normal.normalize();
        }
 
        List<Edge> ring = new ArrayList<>();
 
        for(Vertex vertex : vertices) {
            vertex.normal.zero();
            if(vertex.edge == -1) {
                continue;
            }
 
            ring.clear();
            collectRing(vertex, ring);
            for(Edge edge : ring) {
                vertex.normal.add(faces.get(edge.face).normal);
            }
            vertex.normal.normalize();
        }
 
        for(Edge edge : edges) {
            if(smooth) {
                edge.normal.set(vertices.get(edge.vertex).normal);
            } else {
                edge.normal.set(faces.get(edge.face).normal);
            }
        }
    }
 
    // creates a box centered on the origin. Only the outside grid vertices exist, faces share them.
    // Faces are wound counter clockwise when seen from outside. Call calcNormals / calcTextureCoordinate afterwards.
    public void createBox(float sizeX, float sizeY, float sizeZ, int divsX, int divsY, int divsZ) {
        clear();
 
        final int nx = Math.max(1, divsX);
        final int ny = Math.max(1, divsY);
        final int nz = Math.max(1, divsZ);
        final int[][][] idx = new int[nx + 1][ny + 1][nz + 1];
 
        for(int i = 0; i <= nx; i++) {
            for(int j = 0; j <= ny; j++) {
                for(int k = 0; k <= nz; k++) {
                    boolean outside = i == 0 || i == nx || j == 0 || j == ny || k == 0 || k == nz;
 
                    if(outside) {
                        idx[i][j][k] = addVertex(
                            -sizeX * 0.5f + sizeX * i / nx,
                            -sizeY * 0.5f + sizeY * j / ny,
                            -sizeZ * 0.5f + sizeZ * k / nz).index;
                    } else {
                        idx[i][j][k] = -1;
                    }
                }
            }
        }
 
        addGrid(ny, nz, (u, v) -> idx[nx][u][v]); // +X
        addGrid(nz, ny, (u, v) -> idx[0][v][u]);  // -X
        addGrid(nz, nx, (u, v) -> idx[v][ny][u]); // +Y
        addGrid(nx, nz, (u, v) -> idx[u][0][v]);  // -Y
        addGrid(nx, ny, (u, v) -> idx[u][v][nz]); // +Z
        addGrid(ny, nx, (u, v) -> idx[v][u][0]);  // -Z
    }
 
    private void addGrid(int nu, int nv, IntBinaryOperator at) {
        for(int u = 0; u != nu; u++) {
            for(int v = 0; v != nv; v++) {
                addFaceUnchecked(
                    at.applyAsInt(u, v),
                    at.applyAsInt(u + 1, v),
                    at.applyAsInt(u + 1, v + 1),
                    at.applyAsInt(u, v + 1));
            }
        }
    }
 
    // splits every n-gon into n quads (face center + edge midpoints). Positions are linear, use smooth() to round it.
    // Texture coordinates are split per face corner so UV seams are kept. Normals must be recalculated afterwards.
    public void subdivide(int times) {
        for(int i = 0; i != times; i++) {
            List<int[]> faceIdx = new ArrayList<>();
            List<Vec2[]> faceUV = new ArrayList<>();
 
            snapshotFaces(faceIdx, faceUV);
 
            HashMap<String, Integer> mids = new HashMap<>();
            List<int[]> newIdx = new ArrayList<>();
            List<Vec2[]> newUV = new ArrayList<>();
 
            for(int f = 0; f != faceIdx.size(); f++) {
                int[] c = faceIdx.get(f);
                Vec2[] uv = faceUV.get(f);
                int n = c.length;
                float cx = 0, cy = 0, cz = 0, cu = 0, cv = 0;
 
                for(int k = 0; k != n; k++) {
                    Vec3 p = vertices.get(c[k]).position;
 
                    cx += p.x;
                    cy += p.y;
                    cz += p.z;
                    cu += uv[k].x;
                    cv += uv[k].y;
                }
 
                int center = addVertex(cx / n, cy / n, cz / n).index;
                Vec2 uvc = vec2(cu / n, cv / n);
                int[] m = new int[n];
 
                for(int k = 0; k != n; k++) {
                    int a = c[k];
                    int b = c[(k + 1) % n];
                    String key = keyFor(a, b);
                    Integer found = mids.get(key);
 
                    if(found == null) {
                        Vec3 pa = vertices.get(a).position;
                        Vec3 pb = vertices.get(b).position;
 
                        found = addVertex((pa.x + pb.x) * 0.5f, (pa.y + pb.y) * 0.5f, (pa.z + pb.z) * 0.5f).index;
                        mids.put(key, found);
                    }
                    m[k] = found;
                }
 
                for(int k = 0; k != n; k++) {
                    int p = (k + n - 1) % n;
 
                    newIdx.add(new int[] { c[k], m[k], center, m[p] });
                    newUV.add(new Vec2[] {
                        vec2(uv[k].x, uv[k].y),
                        mid(uv[k], uv[(k + 1) % n]),
                        vec2(uvc.x, uvc.y),
                        mid(uv[p], uv[k])
                    });
                }
            }
            rebuildFaces(newIdx, newUV);
        }
    }
 
    // Catmull-Clark vertex update (interior: (F + 2R + (n-3)P) / n, boundary: 3/4 P + 1/8 (b1 + b2)).
    // Normals must be recalculated afterwards.
    public void smooth(int times) {
        for(int i = 0; i != times; i++) {
            int count = vertices.size();
            float[] pos = new float[count * 3];
            float[] out = new float[count * 3];
 
            for(int v = 0; v != count; v++) {
                Vec3 p = vertices.get(v).position;
 
                pos[v * 3] = p.x;
                pos[v * 3 + 1] = p.y;
                pos[v * 3 + 2] = p.z;
            }
 
            // face centers from the original positions
            float[] centers = new float[faces.size() * 3];
 
            for(Face face : faces) {
                Edge first = edges.get(face.edge);
                Edge edge = first;
                int n = 0;
                float x = 0, y = 0, z = 0;
 
                do {
                    x += pos[edge.vertex * 3];
                    y += pos[edge.vertex * 3 + 1];
                    z += pos[edge.vertex * 3 + 2];
                    n++;
                    edge = edges.get(edge.next);
                } while(edge != first);
 
                centers[face.index * 3] = x / n;
                centers[face.index * 3 + 1] = y / n;
                centers[face.index * 3 + 2] = z / n;
            }
 
            List<Edge> ring = new ArrayList<>();
 
            for(int v = 0; v != count; v++) {
                Vertex vertex = vertices.get(v);
                float px = pos[v * 3], py = pos[v * 3 + 1], pz = pos[v * 3 + 2];
 
                out[v * 3] = px;
                out[v * 3 + 1] = py;
                out[v * 3 + 2] = pz;
                if(vertex.edge == -1) {
                    continue;
                }
 
                ring.clear();
 
                boolean boundary = collectRing(vertex, ring);
                int n = ring.size();
 
                if(boundary) {
                    int found = 0;
                    float bx = 0, by = 0, bz = 0;
 
                    for(Edge e : ring) {
                        if(e.pair == -1) {
                            int b = edges.get(e.next).vertex;
 
                            bx += pos[b * 3];
                            by += pos[b * 3 + 1];
                            bz += pos[b * 3 + 2];
                            found++;
                        }
 
                        Edge p = prevEdge(e);
 
                        if(p.pair == -1) {
                            bx += pos[p.vertex * 3];
                            by += pos[p.vertex * 3 + 1];
                            bz += pos[p.vertex * 3 + 2];
                            found++;
                        }
                    }
                    if(found == 2) {
                        out[v * 3] = 0.75f * px + 0.125f * bx;
                        out[v * 3 + 1] = 0.75f * py + 0.125f * by;
                        out[v * 3 + 2] = 0.75f * pz + 0.125f * bz;
                    }
                } else if(n >= 3) {
                    float fx = 0, fy = 0, fz = 0;
                    float rx = 0, ry = 0, rz = 0;
 
                    for(Edge e : ring) {
                        int nb = edges.get(e.next).vertex;
 
                        fx += centers[e.face * 3];
                        fy += centers[e.face * 3 + 1];
                        fz += centers[e.face * 3 + 2];
                        rx += (px + pos[nb * 3]) * 0.5f;
                        ry += (py + pos[nb * 3 + 1]) * 0.5f;
                        rz += (pz + pos[nb * 3 + 2]) * 0.5f;
                    }
                    fx /= n; fy /= n; fz /= n;
                    rx /= n; ry /= n; rz /= n;
 
                    out[v * 3] = (fx + 2 * rx + (n - 3) * px) / n;
                    out[v * 3 + 1] = (fy + 2 * ry + (n - 3) * py) / n;
                    out[v * 3 + 2] = (fz + 2 * rz + (n - 3) * pz) / n;
                }
            }
 
            for(int v = 0; v != count; v++) {
                vertices.get(v).position.set(out[v * 3], out[v * 3 + 1], out[v * 3 + 2]);
            }
        }
    }
 
    // extrudes the face along its normal. The face is removed, one new vertex is added per face corner,
    // and the new side quads and end cap are joined to the original vertices (no exploded faces).
    // Texture coordinates of the new faces reuse the original corner coordinates; call calcTextureCoordinate to regenerate.
    public void extrude(int face, float amount) {
        if(face < 0 || face >= faces.size()) {
            throw new IllegalArgumentException("face out of bounds");
        }
 
        List<int[]> faceIdx = new ArrayList<>();
        List<Vec2[]> faceUV = new ArrayList<>();
 
        snapshotFaces(faceIdx, faceUV);
 
        int[] c = faceIdx.get(face);
        Vec2[] uv = faceUV.get(face);
        int n = c.length;
 
        // Newell normal, so this doesn't depend on calcNormals having been called
        float nx = 0, ny = 0, nz = 0;
 
        for(int k = 0; k != n; k++) {
            Vec3 a = vertices.get(c[k]).position;
            Vec3 b = vertices.get(c[(k + 1) % n]).position;
 
            nx += (a.y - b.y) * (a.z + b.z);
            ny += (a.z - b.z) * (a.x + b.x);
            nz += (a.x - b.x) * (a.y + b.y);
        }
 
        float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
 
        if(len > 0) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
 
        int[] top = new int[n];
 
        for(int k = 0; k != n; k++) {
            Vec3 p = vertices.get(c[k]).position;
 
            top[k] = addVertex(p.x + nx * amount, p.y + ny * amount, p.z + nz * amount).index;
        }
 
        faceIdx.remove(face);
        faceUV.remove(face);
 
        for(int k = 0; k != n; k++) {
            int j = (k + 1) % n;
 
            // bottom edge runs opposite to the neighbouring face's edge so it pairs with it
            faceIdx.add(new int[] { c[k], c[j], top[j], top[k] });
            faceUV.add(new Vec2[] {
                vec2(uv[k].x, uv[k].y), vec2(uv[j].x, uv[j].y),
                vec2(uv[j].x, uv[j].y), vec2(uv[k].x, uv[k].y)
            });
        }
 
        Vec2[] capUV = new Vec2[n];
 
        for(int k = 0; k != n; k++) {
            capUV[k] = vec2(uv[k].x, uv[k].y);
        }
        faceIdx.add(top);
        faceUV.add(capUV);
 
        rebuildFaces(faceIdx, faceUV);
    }
 
    public void build(VertexArrayBuffer vertexBuffer, VertexElementBuffer vertexElementBuffer) {
        int vertexCount = 0;
        int indexCount = 0;
      
        for(Face face : faces) {
            Edge edge = edges.get(face.edge);
            int count = 0;
 
            do {
                count++;
                edge = edges.get(edge.next);
            } while(edge != edges.get(face.edge));
 
            vertexCount += count;
            
            int tris = count - 2;
 
            indexCount += tris * 3;
        }
 
        vertexBuffer.begin(vertexCount * BasicRenderer.VERTEX_STRIDE);
        for(Face face : faces) {
            Edge edge = edges.get(face.edge);
 
            do {
                Vec3 position = vertices.get(edge.vertex).position;
                Vec2 textureCoordinate = edge.textureCoordinate;
                Vec3 normal = edge.normal;
 
                vertexBuffer.push(position);
                vertexBuffer.push(textureCoordinate);
                vertexBuffer.push(normal);
                vertexBuffer.push(1, 1, 1, 1);
 
                edge = edges.get(edge.next);
            } while(edge != edges.get(face.edge));
        }
        vertexBuffer.end(BufferUsage.STATIC);
 
        vertexCount = 0;
 
        vertexElementBuffer.begin(indexCount);
        for(Face face : faces) {
            Edge edge = edges.get(face.edge);
            int count = 0;
            int b = vertexCount;
 
            do {
                count++;
                edge = edges.get(edge.next);
            } while(edge != edges.get(face.edge));
 
            vertexCount += count;
            
            int tris = count - 2;
 
            for(int i = 0; i != tris; i++) {
                vertexElementBuffer.push(b, b + i + 1, b + i + 2);
            }
        }
        vertexElementBuffer.end(BufferUsage.STATIC);
    }
}
 