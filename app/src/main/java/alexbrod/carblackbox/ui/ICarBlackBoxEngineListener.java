package alexbrod.carblackbox.ui;

/**
 * Created by Alex Brod on 3/13/2017.
 */

public interface ICarBlackBoxEngineListener {

    void OnSuddenBreak(float x, float y, float z);
    void OnSharpTurnLeft(float x, float y, float z);
    void OnSharpTurnRight(float x, float y, float z);
    void onSuddenAcceleration(float x, float y, float z);
}
