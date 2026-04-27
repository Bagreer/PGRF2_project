package MY_GAME.objects; // Uprav si package

import lwjglutils.OGLBuffers;
import transforms.*;

public class Projectile {
    private OGLBuffers buffers;
    private Vec3D position;
    private Vec3D velocity;

    // Zapamatujeme si orientaci v prostoru
    private Vec3D right, up, forward;

    public Projectile(Vec3D startPozice, Vec3D dopredu, Vec3D vpravo, Vec3D nahoru) {
        this.position = startPozice;
        this.velocity = dopredu.mul(2.0); // Rychlost letu
        this.right = vpravo;
        this.up = nahoru;
        this.forward = dopredu;
        generateObject();
    }

    private void generateObject() {
        float t = 0.05f; // tloušťka
        float d = 0.5f;  // délka (po ose Z)

        float[] vertices = {
                -t, -t, -d, t, -t, -d, t, t, -d, -t, t, -d,
                -t, -t, d, t, -t, d, t, t, d, -t, t, d
        };
        int[] indices = {
                0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7,
                0, 4, 7, 0, 7, 3, 1, 5, 6, 1, 6, 2,
                3, 7, 6, 3, 6, 2, 0, 4, 5, 0, 5, 1
        };

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 3)
        };
        this.buffers = new OGLBuffers(vertices, attributes, indices);
    }

    public void move() {
        position = position.add(velocity);
    }

    public Mat4 getModelMatrix() {
        // Použijeme tvůj konstruktor Mat4(Point3D p1, Point3D p2, Point3D p3, Point3D p4)
        // Každý řádek reprezentuje jednu osu (X, Y, Z) a poslední je position (W=1)
        Mat4 orientaceAPozice = new Mat4(
                new Point3D(right.getX(), right.getY(), right.getZ(), 0),
                new Point3D(up.getX(), up.getY(), up.getZ(), 0),
                new Point3D(forward.getX(), forward.getY(), forward.getZ(), 0),
                new Point3D(position.getX(), position.getY(), position.getZ(), 1)
        );


        // Nakonec jen zmenšíme (0.2f pro tenký laser)
        return new Mat4Scale(0.5).mul(orientaceAPozice);
    }



    public Vec3D getPosition() {
        return position;
    }

    public OGLBuffers getBuffers() {
        return buffers;
    }
}