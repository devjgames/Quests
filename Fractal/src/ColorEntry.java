import org.game.Vec4;

public class ColorEntry implements Comparable<ColorEntry> {
        public final Vec4 color;
        public float position;

        public ColorEntry(Vec4 color, float position) {
            this.color = new Vec4(color);
            this.position = position;
        }

        @Override
        public int compareTo(ColorEntry entry) {
            return Float.compare(position, entry.position);
        }
    }
