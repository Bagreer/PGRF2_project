package MY_GAME;

import org.lwjgl.glfw.GLFWKeyCallbackI;

import java.awt.event.KeyListener;

import static org.lwjgl.glfw.GLFW.*;

public class InputController implements GLFWKeyCallbackI {

    private My_Game game;

    public InputController(My_Game game) {
        this.game = game;
    }


    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        // Klávesu jsme právě zmáčkli
        if (action == GLFW_PRESS) {
            if (key == GLFW_KEY_W) game.holdingW = true;
            if (key == GLFW_KEY_S) game.holdingS = true;
            if (key == GLFW_KEY_A) game.holdingA = true;
            if (key == GLFW_KEY_D) game.holdingD = true;
            if (key == GLFW_KEY_L) game.holdingL = true;
        }
        // Klávesu jsme právě pustili
        else if (action == GLFW_RELEASE) {
            if (key == GLFW_KEY_W) game.holdingW = false;
            if (key == GLFW_KEY_S) game.holdingS = false;
            if (key == GLFW_KEY_A) game.holdingA = false;
            if (key == GLFW_KEY_D) game.holdingD = false;
            if (key == GLFW_KEY_L) game.holdingL = false;
        }
    }
}
