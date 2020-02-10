package tools;

import engine.camera.Camera;
import engine.camera.Camera3D;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.function.Function;

public class RayTracer {
    public static class TraceResult {
        private boolean valid;
        private Vector3i face;

        TraceResult(boolean valid, Vector3i face) {
            this.valid = valid;
            this.face = face;
        }

        private TraceResult set(boolean valid, Vector3i face) {
            this.valid = valid;
            this.face = face;
            return this;
        }

        public static TraceResult invalid() {
            return new TraceResult(false, null);
        }

        public boolean isValid() {
            return valid;
        }

        public Vector3i getFace() {
            return face;
        }
    }

    private static TraceResult res = new TraceResult(false,null);

    public static TraceResult traceVoxelGrid(Camera3D camera, float maxDistance, Function<Vector3i, Boolean> callback) {
        return traceVoxelGrid(camera.getFollow().getPosition(),camera.getForward(),maxDistance,callback);
    }
    private static Vector3d direction = new Vector3d(0,0,0);
    private static Vector3i voxelPos = new Vector3i(0,0,0);
    private static Vector3i face = new Vector3i(0, 0, 0);
    public static TraceResult traceVoxelGrid(Vector3d origin, Vector3f direction, float maxDistance, Function<Vector3i, Boolean> callback) {
        double distance = (float) Math.sqrt(direction.x * direction.x + direction.y * direction.y + direction.z * direction.z);
        if (distance == 0)
            return res.set(false,null);

        RayTracer.direction.set(direction).normalize(distance);
        RayTracer.voxelPos.set((int) Math.floor(origin.x), (int) Math.floor(origin.y), (int) Math.floor(origin.z));

        int stepX = direction.x > 0 ? 1 : -1;
        int stepY = direction.y > 0 ? 1 : -1;
        int stepZ = direction.z > 0 ? 1 : -1;

        boolean xInf = direction.x == 0;
        boolean yInf = direction.y == 0;
        boolean zInf = direction.z == 0;

        double txDelta = !xInf ? Math.abs(1f / direction.x) : Float.MAX_VALUE;
        double tyDelta = !yInf ? Math.abs(1f / direction.y) : Float.MAX_VALUE;
        double tzDelta = !zInf ? Math.abs(1f / direction.z) : Float.MAX_VALUE;

        double xDist = stepX > 0 ? voxelPos.x + 1 - origin.x : origin.x - voxelPos.x;
        double yDist = stepY > 0 ? voxelPos.y + 1 - origin.y : origin.y - voxelPos.y;
        double zDist = stepZ > 0 ? voxelPos.z + 1 - origin.z : origin.z - voxelPos.z;

        double txMax = !xInf ? txDelta * xDist : Float.MAX_VALUE;
        double tyMax = !yInf ? tyDelta * yDist : Float.MAX_VALUE;
        double tzMax = !zInf ? tzDelta * zDist : Float.MAX_VALUE;

        double t = 0;
        int pickDirection = -1;
        face.set(0,0,0);
        while (t < maxDistance) {
            boolean b = callback.apply(voxelPos);
            if (b) {
                switch (pickDirection) {
                    case 0:
                        face.x = -stepX;
                        break;
                    case 1:
                        face.y = -stepY;
                        break;
                    case 2:
                        face.z = -stepZ;
                        break;
                }
                return res.set(true, face);
            }
            if (txMax < tyMax) {
                if (txMax < tzMax) {
                    voxelPos.x += stepX;
                    t = txMax;
                    txMax += txDelta;
                    pickDirection = 0;
                } else {
                    voxelPos.z += stepZ;
                    t = tzMax;
                    tzMax += tzDelta;
                    pickDirection = 2;
                }
            } else {
                if (tyMax < tzMax) {
                    voxelPos.y += stepY;
                    t = tyMax;
                    tyMax += tyDelta;
                    pickDirection = 1;

                } else {
                    voxelPos.z += stepZ;
                    t = tzMax;
                    tzMax += tzDelta;
                    pickDirection = 2;
                }
            }
        }
        return res.set(false,null);
    }
}