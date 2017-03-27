package alexbrod.carblackbox.ui;

/**
 * Created by Alex Brod on 3/13/2017.
 */

public interface ICarBlackBoxEngineListener {

    void onSuddenBreak(float Acceleration);
    void onSharpTurnLeft(float Acceleration);
    void onSharpTurnRight(float Acceleration);
    void onSuddenAcceleration(float Acceleration);
}
