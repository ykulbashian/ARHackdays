package com.shopify.hackday.ar;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

import rx.exceptions.Exceptions;

public class ObjLoadActivity extends AppCompatActivity {

    private static final class LoadModelRenderer extends Renderer {

        private static final String TAG = ObjLoadActivity.class.getSimpleName();

        private PointLight light;
        private Object3D objectGroup, object, chairObject;

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

            initSuzanneObj();
//            initChairObj();

            getCurrentScene().addChild(object);

            cameraAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
            cameraAnim.setDurationMilliseconds(8000);
            cameraAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
            cameraAnim.setTransformable3D(object);

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

            getCurrentScene().setBackgroundColor(0f,0f,1f,0f);
        }

        private void initSuzanneObj() {
            LoaderAWD suzanneLoaderAWD = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
            try {
                suzanneLoaderAWD.parse();
                object = suzanneLoaderAWD.getParsedObject();

                Material material = new Material();
                material.setDiffuseMethod(new DiffuseMethod.Lambert());
                material.setColor(0xff990000);
                material.enableLighting(true);
                object.setMaterial(material);

            } catch (ParsingException e) {
                throw Exceptions.propagate(e);
            }
        }

        private void initChairObj() {
            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.model01_obj);
            try {
                objParser.parse();
                object = objParser.getParsedObject();
            } catch (ParsingException e) {
                throw Exceptions.propagate(e);
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        }

        private PointLight light1;
        private Point currentObjectPoint;
        private Point previousObjectPoint;

        @Override
        public void onTouchEvent(MotionEvent event) {
            int action = MotionEventCompat.getActionMasked(event);

            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    System.out.println("Action was DOWN");
                    currentObjectPoint.set((int) event.getX(), (int) event.getY());
                    previousObjectPoint.set(currentObjectPoint.x, currentObjectPoint.y);
//                    return true;
                case (MotionEvent.ACTION_MOVE):
                    System.out.println("Action was MOVE");
                    previousObjectPoint.set(currentObjectPoint.x, currentObjectPoint.y);
                    currentObjectPoint.set((int) event.getX(), (int) event.getY());
                    System.out.println("X:" + currentObjectPoint.x + "   Y:" + currentObjectPoint.y);
                    int deltaX = currentObjectPoint.x - previousObjectPoint.x;
                    int deltaY = currentObjectPoint.y - previousObjectPoint.y;
                    object.setX(object.getX() + (deltaX / 300F));
                    object.setZ(object.getZ() + (deltaY / 300F));
//                    return true;
                case (MotionEvent.ACTION_UP):
                    System.out.println("Action was UP");
//                    return true;
                case (MotionEvent.ACTION_CANCEL):
                    System.out.println("Action was CANCEL");
//                    return true;
                case (MotionEvent.ACTION_OUTSIDE):
                    Log.w(TAG,"Movement occurred outside bounds of current screen element");
//                    return true;
                default:
                    //SHIT
            }
//            return true;
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
//        surfaceView.setTransparent(true);

        renderSurface = (ISurface) surfaceView;
        // Create the renderer
        renderer = new LoadModelRenderer(this);
        // Apply the renderer
        renderSurface.setSurfaceRenderer(renderer);
    }

}
