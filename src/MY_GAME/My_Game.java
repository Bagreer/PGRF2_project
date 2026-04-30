package MY_GAME;
import MY_GAME.objects.Arrow;
import MY_GAME.objects.Asteroid;
import MY_GAME.objects.Fragment;
import MY_GAME.objects.Projectile;
import lwjglutils.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import transforms.*;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * GLSL sample:<br/>
 * Draw two different geometries with two different shader programs<br/>
 * Requires LWJGL3
 *
 * @author Michal Prause
 * @version 1
 * @since 2026-xx-xx
 */

public class My_Game {

    // window size
    int width = 1280, height = 800;
    double ox, oy;

    // speed of the spaceship
    int speed = 30;

    OGLTextureCube skybox;
    OGLBuffers skyboxBuffers; // Pro uložení geometrie kostky
    int skyboxShader;
    int shipShader, laserShader;
    OGLTexture2D spaceshipTexture;
    int locMatLaser, locColorLaser; // Přidej toto k polím třídy

    Arrow guideArrow;

    int arrowShader, locMatArrow, locColorArrow;

    boolean isDead = false;


    // key status
    protected boolean holdingW = false;
    protected boolean holdingA = false;
    protected boolean holdingS = false;
    protected boolean holdingD = false;

    boolean canPresF = false;
    boolean fPressed = false;

    // I am speed (zrychlení)
    protected boolean holdingL = false;

    // The window handle
    private long window;

    // PŘIDEJ TOTO:
    OGLModelOBJ spaceship;
    OGLModelOBJ portal;
    OGLBuffers buffers;

    private double startTime;

    int shaderProgram, locMat;

    // camera things
    boolean depthTest = true, cCW = true, renderFront = false, renderBack = false;
    Camera cam = new Camera();
    double currentFov = Math.PI / 4;
    Mat4 proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 1000.0);


    // Objects
    List<Fragment> fragments = new ArrayList<>(); // PŘIDÁNO: Seznam pro exploze
    List<Asteroid> poleAsteroidu = new ArrayList<>();
    int numberOfAsteroids = 10000;
    List<Projectile> projectiles = new ArrayList<>();
    boolean canShoot = true;

    Vec3D portalPos = new  Vec3D(500,0,0);

    private OGLTextRenderer textRenderer;

    /**
     * initializes all objects
     */
    private void init() throws IOException {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the windowd
        window = glfwCreateWindow(width, height, "PRAUSE MICHAL - escaping space | PGRF2 | cv08", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // LISTENERY
        glfwSetKeyCallback(window, new InputController(this));


        glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0 &&
                        (w != width || h != height)) {
                    width = w;
                    height = h;
                    proj = new Mat4PerspRH(currentFov, height / (double) width, 0.01, 1000.0);
                }
            }
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        OGLUtils.printOGLparameters();

        // Background color
        glClearColor(0f, 0f, 0f, 1.0f);

        shaderProgram = ShaderUtils.loadProgram("/MY_GAME/simple/simple");

        glUseProgram(this.shaderProgram);

        locMat = glGetUniformLocation(shaderProgram, "mat");

        cam = cam.withPosition(new Vec3D(5, 5, 2.5))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125);

        float[] cubeVertices = {
                // Pozice (x, y, z) - stačí 8 rohů kostky
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f
        };

        int[] cubeIndices = {
                0, 1, 2, 2, 3, 0, // přední
                4, 5, 6, 6, 7, 4, // zadní
                0, 1, 5, 5, 4, 0, // spodní
                2, 3, 7, 7, 6, 2, // horní
                1, 2, 6, 6, 5, 1, // pravá
                0, 3, 7, 7, 4, 0  // levá
        };

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 3)
        };

        skyboxBuffers = new OGLBuffers(cubeVertices, attributes, cubeIndices);

        // Načtení shaderů
        laserShader = ShaderUtils.loadProgram("/MY_GAME/simple/laser");
        locMatLaser = glGetUniformLocation(laserShader, "mat");
        locColorLaser = glGetUniformLocation(laserShader, "laserColor");

        arrowShader = ShaderUtils.loadProgram("/MY_GAME/simple/arrow");
        locMatArrow = glGetUniformLocation(arrowShader, "mat");
        locColorArrow = glGetUniformLocation(arrowShader, "arrowColor");


        shipShader = ShaderUtils.loadProgram("/MY_GAME/simple/ship");
        spaceshipTexture = new OGLTexture2D("res/textures/metal-texture.jpg");

        String[] skyboxFiles = {
                "res/textures/seamlessSpace.png",  // +X
                "res/textures/seamlessSpace.png",  // -X
                "res/textures/seamlessSpace.png",  // +Y
                "res/textures/seamlessSpace.png",  // -Y
                "res/textures/seamlessSpace.png",  // +Z
                "res/textures/seamlessSpace.png"  // -Z
        };

        skybox = new OGLTextureCube(skyboxFiles);
        skyboxShader = ShaderUtils.loadProgram("/MY_GAME/simple/skybox");
        spawnPortal();
        System.out.println("Position of the portal is: " + portalPos.toString());

        glDisable(GL_CULL_FACE);

        // TADY TEPRVE NAČTEME LOĎ (Karta už běží)
        spaceship = new OGLModelOBJ("/MY_GAME/objects/Spaceship.obj");
        portal =  new OGLModelOBJ("/MY_GAME/objects/Portal.obj");
        guideArrow = new Arrow();

        startTime = glfwGetTime();
        generateAsteroids();

        textRenderer = new OGLTextRenderer(width, height);

        System.out.println("--------------------------------");
        System.out.println("PGRF2 Project - Space Shooter");
        System.out.println("Author: Michal Prause");
        System.out.println("Version: 1.0 (Final Build)");
        System.out.println("--------------------------------");

    }

    /**
     * repears every single frame
     */
    private void loop() {
        while ( !glfwWindowShouldClose(window) ) {

            if (isDead) {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                // Tady to padalo - ujisti se, že textRenderer není null
                if (textRenderer != null) {
                    textRenderer.addStr2D(width / 2 - 50, height / 2, "GAME OVER");
                    textRenderer.addStr2D(width / 2 - 80, height / 2 + 30, "Stiskni R pro restart");
                    textRenderer.draw(); // DŮLEŽITÉ: U textRendereru se často musí volat i draw()!
                }

                if (glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS) {
                    isDead = false;
                    cam.withPosition(new Vec3D(0, 0, 0));
                    startTime = glfwGetTime();
                }
                return;
            }

            // 1. NEJDŘÍV PŘÍPRAVA
            glViewport(0, 0, width, height);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // 2. SKYBOX (Vykreslení hvězd na pozadí)
            glDisable(GL_DEPTH_TEST); // Skybox je v nekonečnu, ignorujeme hloubku
            glUseProgram(skyboxShader);

            Mat4 skyView = cam.getViewMatrix();
            // Odstranění translace (poslední sloupec matice), aby Skybox letěl s námi
            skyView = skyView   .withElement(3, 0, 0.0)
                                .withElement(3, 1, 0.0)
                                .withElement(3, 2, 0.0);

            // V této knihovně násobíme View * Proj
            Mat4 skyMat = skyView.mul(proj);

            glUniformMatrix4fv(glGetUniformLocation(skyboxShader, "mat"), false,
                    ToFloatArray.convert(skyMat));

            skybox.bind(skyboxShader, "skyboxTexture", 0);
            skyboxBuffers.draw(GL_TRIANGLES, skyboxShader);

            glEnable(GL_DEPTH_TEST); // Znovu zapneme hloubku pro objekty

            // 3. LOGIKA POHYBU (Asteroidy, Loď...)
            turn();
            cam = cam.forward(speed / 100.0);

            // 4. VYKRESLENÍ LASERŮ
            glUseProgram(laserShader); // Aktivujeme správný shader


            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile p = projectiles.get(i);
                p.move();
                if (p.getPosition().sub(cam.getPosition()).length() > 300) {
                    projectiles.remove(i);
                } else {
                    // Použijeme locMatLaser místo locMat!
                    glUniformMatrix4fv(locMatLaser, false,
                            ToFloatArray.convert(p.getModelMatrix().mul(proj)));
                    p.getBuffers().draw(GL_TRIANGLES, laserShader); // Kreslíme laser shaderem
                }
            }

            glUseProgram(shaderProgram); // Přepneme zpět na hlavní shader

            Mat4 viewProj = cam.getViewMatrix().mul(proj);
            if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && canShoot) {
                Mat4 viewMat = cam.getViewMatrix();
                Vec3D camRight = new Vec3D(viewMat.get(0, 0), viewMat.get(1, 0), viewMat.get(2, 0));
                Vec3D camUp = new Vec3D(viewMat.get(0, 1), viewMat.get(1, 1), viewMat.get(2, 1));
                Vec3D camForward = new Vec3D(viewMat.get(0, 2), viewMat.get(1, 2), viewMat.get(2, 2)).mul(-1);
                Vec3D startingPosition = cam.getPosition().add(camForward.mul(4.0)).add(camUp.mul(-1.3));
                projectiles.add(new Projectile(startingPosition, camForward, camRight, camUp));
                canShoot = false;
            } else if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_RELEASE) {
                canShoot = true;
            }



            // --- 7. VYKRESLENÍ NAVÁDĚCÍ ŠIPKY (Vlastní Shader) ---
            glUseProgram(arrowShader);
            glUniform3f(locColorArrow, 1.0f, 0.0f, 0.0f); // Žlutá barva

            /// 1. Směr k portálu ve světě a transformace do prostoru kamery (w=0)
            Vec3D directionWorld = portalPos.sub(cam.getPosition());
            Point3D res = new Point3D(directionWorld.getX(), directionWorld.getY(), directionWorld.getZ(), 0.0)
                    .mul(cam.getViewMatrix());
            Vec3D localDir = new Vec3D(res.getX(), res.getY(), res.getZ());

// 2. Výpočet úhlů relativně k dopřednému směru (-Z)
// Yaw: Jak moc je portál vlevo/vpravo od středu
            double yaw = Math.atan2(localDir.getX(), -localDir.getZ());
// Pitch: Jak moc je portál nad/pod středem
            double pitch = -Math.atan2(localDir.getY(), Math.sqrt(localDir.getX() * localDir.getX() + localDir.getZ() * localDir.getZ()));

// 3. Sestavení matice (Matice jdou v pořadí od modelu k obrazovce)
            Mat4 maticeSipka = new Mat4Scale(0.5) // 1. Zmenšíme
                    .mul(new Mat4RotY(-Math.PI / 2.0)) // 2. Otočíme základní model (+X) tak, aby mířil dopředu (-Z)
                    .mul(new Mat4RotY(yaw))            // 3. Otočíme vlevo/vpravo
                    .mul(new Mat4RotX(pitch))           // 4. Nakloníme nahoru/dolů
                    .mul(new Mat4Transl(0.0, 1.2, -4.0)) // 5. Posuneme v HUDu
                    .mul(proj);                          // 6. Promítneme

            glUseProgram(arrowShader);
            glUniformMatrix4fv(locMatArrow, false, ToFloatArray.convert(maticeSipka));
            guideArrow.getBuffers().draw(GL_TRIANGLES, arrowShader);


            // Návrat k hlavnímu shaderu pro další snímek
            glUseProgram(shaderProgram);


            // FOV a zpomalení
            double targetFov = holdingL ? Math.PI / 3 : Math.PI / 4;
            currentFov += (targetFov - currentFov) * 0.1;
            proj = new Mat4PerspRH(currentFov, height / (double) width, 0.01, 1000.0);

            viewProj = cam.getViewMatrix().mul(proj);


            // 3. VYKRESLENÍ SVĚTA A ASTEROIDŮ
            glUseProgram(shaderProgram);

            for (int i = poleAsteroidu.size() - 1; i >= 0; i--) {
                Asteroid ast = poleAsteroidu.get(i);
                Vec3D astPos = ast.getPosition();

                // V loop() uvnitř cyklu asteroidů:
                double dist = astPos.sub(cam.getPosition()).length();

                if (dist > 150.0) {
                    // 1. Vygenerujeme náhodný bod v krychli -1 až 1
                    double rx = (Math.random() * 2.0) - 1.0;
                    double ry = (Math.random() * 2.0) - 1.0;
                    double rz = (Math.random() * 2.0) - 1.0;

                    // 2. Normalizujeme ho na jednotkovou délku (získáme směr na kouli)
                    double mag = Math.sqrt(rx * rx + ry * ry + rz * rz);
                    if (mag < 0.0001) {
                        rx = 0;
                        ry = 1;
                        rz = 0;
                        mag = 1;
                    } // Sychr proti nule

                    Vec3D smer = new Vec3D(rx / mag, ry / mag, rz / mag);

                    double r = 145.0;

                    ast.setPosition(cam.getPosition().add(smer.mul(r)));
                    continue;
                }

                // Kolize se střelami
                boolean znicen = false;
                for (int j = projectiles.size() - 1; j >= 0; j--) {
                    if (astPos.sub(projectiles.get(j).getPosition()).length() < ast.getScale() + 0.3) {
                        projectiles.remove(j);
                        znicen = true;
                        break;
                    }
                }

                if (znicen) {
                    // --- PŘIDÁNO: Exploze! Spawneme 4 až 7 malých úlomků ---
                    int pocetUlomku = 4 + (int)(Math.random() * 4);
                    for (int u = 0; u < pocetUlomku; u++) {
                        // Předáme jim pozici a model zničeného asteroidu
                        fragments.add(new Fragment(ast.getPosition(), ast.getScale(), ast.getBuffers()));
                    }

                    // Reset asteroidu dopředu (tvůj stávající kód)
                    double spawnDistance = 200.0 + (Math.random() * 50.0);
                    ast.setPosition(cam.getPosition().add(cam.getViewVector().mul(spawnDistance)));
                    continue;
                }

                // TimeStop
                if (glfwGetKey(window, GLFW_KEY_F) == GLFW_PRESS) {
                    speed = 0;
                    ast.setVelocity(new Vec3D(0, 0, 0));
                } else {
                    speed = 30;
                }

                // Kolize s lodí (HUD space)
                Point3D astKamera = new Point3D(astPos).mul(cam.getViewMatrix());
                double distShip = Math.sqrt(Math.pow(astKamera.getX(), 2) + Math.pow(astKamera.getY() + 1.2, 2) + Math.pow(astKamera.getZ() + 4.0, 2));
                if (distShip <= ast.getScale() + ast.getScale() / 10) {
                    System.out.println("YOU LOST");
                    isDead = true;
                }

                // Kreslení asteroidu
                ast.move();
                glUniformMatrix4fv(locMat, false, ToFloatArray.convert(ast.getModelMatrix().mul(viewProj)));
                ast.getBuffers().draw(GL_TRIANGLES, shaderProgram);
            }

//            System.out.println("Camera position: " + cam.getPosition());

            // 4. VYKRESLENÍ LASERŮ
            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile p = projectiles.get(i);
                p.move();
                if (p.getPosition().sub(cam.getPosition()).length() > 300) {
                    projectiles.remove(i);
                } else {
                    glUniformMatrix4fv(locMat, false, ToFloatArray.convert(p.getModelMatrix().mul(viewProj)));
                    p.getBuffers().draw(GL_TRIANGLES, shaderProgram);
                }
            }

            // VYKRESLENÍ ÚLOMKŮ
            for (int i = fragments.size() - 1; i >= 0; i--) {
                Fragment u = fragments.get(i);

                if (u.moveAndCheck()) {
                    fragments.remove(i); // Smazání po vypršení životnosti
                } else {
                    glUniformMatrix4fv(locMat, false, ToFloatArray.convert(u.getModelMatrix().mul(viewProj)));
                    u.getBuffers().draw(GL_TRIANGLES, shaderProgram);
                }
            }


            glUseProgram(shipShader);
            spaceshipTexture.bind(shipShader, "shipTexture", 0);
            // 5. VYKRESLENÍ LODĚ (HUD)
            Mat4 modelLod = new Mat4Scale(0.1)
                    .mul(new Mat4RotY(Math.toRadians(180)))
                    .mul(new Mat4RotX(Math.toRadians(5)));

            // Přidání náklonu při zatáčení
            if (holdingA) modelLod = modelLod.mul(new Mat4RotZ(Math.toRadians(10)));
            if (holdingD) modelLod = modelLod.mul(new Mat4RotZ(Math.toRadians(-10)));

            if (glfwGetTime() - startTime > 30.0) {
                speed = 60; // Prostě jen přepíšeme stávající proměnnou rychlosti
            }

            // HUD
            modelLod = modelLod.mul(new Mat4Transl(0.0, -1.0, -4.0));
            glUniformMatrix4fv(locMat, false, ToFloatArray.convert(modelLod.mul(proj)));
            glUniformMatrix4fv(glGetUniformLocation(shipShader, "mat"), false,
                    ToFloatArray.convert(modelLod.mul(proj)));
            spaceship.getBuffers().draw(spaceship.getTopology(), shipShader);


            // 6. VYKRESLENÍ PORTÁLU
            Mat4 maticePortal = new Mat4Scale(2.5) // Trochu menší, jak jsi chtěl
                    .mul(new Mat4RotY(Math.PI/2)) // Natočení čelem k lodi
                    .mul(new Mat4Transl(portalPos))   // Přesun na náhodnou pozici
                    .mul(viewProj);                   // Projekce a kamera

            glUniformMatrix4fv(locMat, false, ToFloatArray.convert(maticePortal));
            portal.getBuffers().draw(portal.getTopology(), shaderProgram);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public void run() {
        try {
            init();
            loop();

            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            // Terminate GLFW and free the error callback
            glDeleteProgram(shaderProgram);
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }

    }

    /**
     * generates asteroids in a cube (200x200x200)
     */
    public void generateAsteroids() {
        for (int i = 0; i < numberOfAsteroids; i++) {
            double x, y, z;
            double distFromPlayer;

            do {
                x = (Math.random() * 200.0) - 100.0; // left right   (-100,100)
                y = (Math.random() * 200.0) - 100.0; // up down      (-100,100)
                z = (Math.random() * 200.0) - 100.0; // front back   (-100,100)

                distFromPlayer = Math.sqrt(x*x + y*y + z*z);

            } while (distFromPlayer < 20.0);

            double scale = 0.5 + (Math.random() * 2);

            poleAsteroidu.add(new Asteroid(new Vec3D(x, y, z), scale));
        }
    }


    /**
     * randomly generates portal coords
     */
    void spawnPortal() {
        double alpha = Math.random() * 2 * Math.PI;

        // Omezíme výšku (beta) jen na malý rozptyl kolem roviny (např. +/- 5 stupňů)
        // Aby nebyl přesně v nule, což je nuda, ale ani nad hlavou
        double beta = (Math.random() - 0.5) * Math.toRadians(10);

        double dist = 500.0; // Vzdálenost

        double px = Math.cos(beta) * Math.cos(alpha) * dist;
        double pz = Math.random() * 100;
        double py = Math.cos(beta) * Math.sin(alpha) * dist;

        portalPos = new Vec3D(px, py, pz);

        System.out.println("Portál čeká na: " + portalPos.toString());
    }

    /**
     * slows down ship when turning
     */
    public void slowDown() {
        if (holdingW || holdingA || holdingS || holdingD) {
            speed *= 0.95;
            double targetFov;

            if (holdingW || holdingA || holdingS || holdingD) {
                speed *= 0.99;
                targetFov = Math.toRadians(40);
            } else {
                speed = 30;
                targetFov = Math.toRadians(45);
            }

            currentFov += (targetFov - currentFov) * 0.1;

        } else {
            speed = 30;
        }

    }

    // METHODS FOR MOVING
    public void turn() {
        turnUp();
        turnDown();
        turnLeft();
        turnRight();
    }
    float TURNING_RATE_IN_DEGREES = (float) Math.toRadians(1.0); // 0,5 - 1 je celkem fajn
    public void turnLeft() {
        if (holdingA) {
            cam = cam.addAzimuth(TURNING_RATE_IN_DEGREES);
        }
    }
    public void turnRight() {
        if (holdingD) {
            cam = cam.addAzimuth(-TURNING_RATE_IN_DEGREES);
        }
    }
    public void turnUp() {
        if (holdingW) {
            cam = cam.addZenith(TURNING_RATE_IN_DEGREES);
        }
    }
    public void turnDown() {
        if (holdingS) {
            cam = cam.addZenith(-TURNING_RATE_IN_DEGREES);
        }
    }


    // FOR RUNNING THE APP
    public static void main(String[] args) {
        new My_Game().run();
    }
}


// TODO uklidit kód
// TODO upgrady
// TODO infobox (autor a nějaké další blbosti) start,options,quit (jakoby asi mam infobox, ale aspoň ten endscreen)
// TODO spustitelný soubor

// .mtl soubor (textura)
// nevyřešeno
// zvuky
// otáčení kamery nezávisle na prostoru
// smooth animace zatáčení (rotace lodě)


// hotové věci z todo
// změna FOV při zrychlení/zpomalení
// náklon lodi při zatáčení by mohlo vypadat dobře
// "animace" asteroidů (pohyb a otáčení)
// spomalení při zatáčení
// kolize
// střílení
// kolize doladit aby se něco stalo (viz. #418)
// kolize střílení
// dořešit mizení asteroidů mimo kameru (funguje jenom směrem dolů)
// portál
// vylepšení vzhledu lodi / asteroidů
// cooldown na střílení

// animace asteroidů (fragment)
// safespace okolo hráče.

// skybox
// skybox upscaling + pohyb

// šipka navádějící k portálu
