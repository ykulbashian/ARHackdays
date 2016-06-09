package com.shopify.hackday.ar;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

public class ObjLoadActivity extends AppCompatActivity {

    private static final class LoadModelRenderer extends Renderer {

        private PointLight light;
        private Object3D objectGroup;
        private Animation3D cameraAnim, lightAnim;

        public LoadModelRenderer(Context context) {
            super(context);
        }

        @Override
        protected void initScene() {
            light = new PointLight();
            light.setPosition(0, 0, 4);
            light.setPower(8);

            getCurrentScene().addLight(light);
            getCurrentCamera().setZ(8);

            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.model01_obj);
            try {
                objParser.parse();
                objectGroup = objParser.getParsedObject();
                getCurrentScene().addChild(objectGroup);

                cameraAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
                cameraAnim.setDurationMilliseconds(8000);
                cameraAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
                cameraAnim.setTransformable3D(objectGroup);
            } catch (ParsingException e) {
                e.printStackTrace();
            }

            lightAnim = new EllipticalOrbitAnimation3D(new Vector3(),
                    new Vector3(0, 10, 0), Vector3.getAxisVector(Vector3.Axis.Z), 0,
                    360, EllipticalOrbitAnimation3D.OrbitDirection.CLOCKWISE);

            lightAnim.setDurationMilliseconds(3000);
            lightAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
            lightAnim.setTransformable3D(light);

            getCurrentScene().registerAnimation(cameraAnim);
            getCurrentScene().registerAnimation(lightAnim);

            cameraAnim.play();
            lightAnim.play();

//            getCurrentScene().setBackgroundColor(0f,0f,1f,0f);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
        }
    }

    protected ISurface renderSurface;
    protected ISurfaceRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obj_load);

        // Find the TextureView, make it transparent to allow for video feed behind us.
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.rajwali_surface);
        surfaceView.setTransparent(true);

        renderSurface = (ISurface) surfaceView;
        // Create the renderer
        renderer = new LoadModelRenderer(this);
        // Apply the renderer
        renderSurface.setSurfaceRenderer(renderer);
    }

}
