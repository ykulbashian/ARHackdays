/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.shopify.hackday.ar.vuforia.app.VirtualButtons;

import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;

import com.shopify.hackday.ar.vuforia.R;
import com.shopify.hackday.ar.vuforia.SampleApplicationSession;
import com.shopify.hackday.ar.vuforia.utils.CubeObject;
import com.shopify.hackday.ar.vuforia.utils.CubeShaders;
import com.shopify.hackday.ar.vuforia.utils.SampleUtils;
import com.shopify.hackday.ar.vuforia.utils.Teapot;
import com.shopify.hackday.ar.vuforia.utils.Texture;
import com.vuforia.ImageTargetResult;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.Vuforia;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class CameraRenderer extends org.rajawali3d.renderer.Renderer implements GLSurfaceView.Renderer {

    private static final String LOGTAG = "CameraRenderer";

    private SampleApplicationSession vuforiaAppSession;

    public boolean mIsActive = false;


    public CameraRenderer(VirtualButtonsActivity activity,
                          SampleApplicationSession session) {
        super(activity);
        vuforiaAppSession = session;
    }

    public void setIsActive(boolean isActive) {
        this.mIsActive = isActive;
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    public void onSurfaceDestroyed() {

    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }


    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

    }



    class FloatingPoint {

        public double x;
        public boolean twoFingers;

        public FloatingPoint() {
            x = 0;
            twoFingers = false;
        }
    }
    final FloatingPoint rotation = new FloatingPoint();

    private PointLight light1, light2;
    private Object3D parsedObject;

    private Point currentObjectPoint, previousObjectPoint;
    private MotionEvent.PointerCoords curPointer1, curPointer2, prevPointer1, prevPointer2;

    private static final float SCALE_FACTOR = 200f;

    @Override
    protected void initScene() {

        currentObjectPoint = new Point(0, 0);
        previousObjectPoint = new Point(0, 0);
        curPointer1 = new MotionEvent.PointerCoords();
        prevPointer1 = new MotionEvent.PointerCoords();
        curPointer2 = new MotionEvent.PointerCoords();
        prevPointer2 = new MotionEvent.PointerCoords();

        light1 = new PointLight();
        light1.setPosition(-2*SCALE_FACTOR, 2*SCALE_FACTOR, -2*SCALE_FACTOR);
        light1.setPower(2*SCALE_FACTOR);
        light2 = new PointLight();
        light2.setPosition(5*SCALE_FACTOR, 5*SCALE_FACTOR, 5*SCALE_FACTOR);
        light2.setPower(8*SCALE_FACTOR);

        getCurrentScene().addLight(light1);
        getCurrentScene().addLight(light2);
        getCurrentScene().alwaysClearColorBuffer(false);

        getCurrentCamera().setFarPlane(1000);

        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.model01_obj);
        try {
            objParser.parse();
            parsedObject = objParser.getParsedObject();
            parsedObject.setRotZ(90);
            parsedObject.setPosition(-SCALE_FACTOR/2, -SCALE_FACTOR/2, SCALE_FACTOR/2);
            parsedObject.setScale(new Vector3(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR));


            Material material = new Material();
            material.addTexture(new org.rajawali3d.materials.textures.Texture("material0",
                    R.drawable.couch_image));
            material.setColorInfluence(0);
            parsedObject.setMaterial(material);

            getCurrentScene().addChild(parsedObject);

        } catch (ParsingException e) {
            e.printStackTrace();
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        if(parsedObject == null)return;

        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                if (event.getPointerCount() == 2) {
                    System.out.println("TWO FINGER DOWN");
                    rotation.twoFingers = true;
                    event.getPointerCoords(0, curPointer1);
                    event.getPointerCoords(0, prevPointer1);
                    event.getPointerCoords(1, curPointer2);
                    event.getPointerCoords(1, prevPointer2);
                } else {
                    System.out.println("Action was DOWN");
                    currentObjectPoint.set((int) event.getX(), (int) event.getY());
                    previousObjectPoint.set(currentObjectPoint.x, currentObjectPoint.y);
                }
            case (MotionEvent.ACTION_MOVE):
                if (event.getPointerCount() == 2) {
                    System.out.println("TWO TOUCH MOVE");
                    if (!rotation.twoFingers) {
                        event.getPointerCoords(0, curPointer1);
                        event.getPointerCoords(0, prevPointer1);
                        event.getPointerCoords(1, curPointer2);
                        event.getPointerCoords(1, prevPointer2);
                    } else {
                        prevPointer1.copyFrom(curPointer1);
                        prevPointer2.copyFrom(curPointer2);
                        event.getPointerCoords(0, curPointer1);
                        event.getPointerCoords(1, curPointer2);
                    }
                    rotation.twoFingers = true;
                    double v1x = (double) (curPointer1.x - prevPointer1.x);
                    double v1y = (double) (curPointer1.y - prevPointer1.y);
                    double v2x = (double) (curPointer2.x - prevPointer2.x);
                    double v2y = (double) (curPointer2.y - prevPointer2.y);
                    System.out.println((v1x + v2x) / (-7));
                    rotation.x = (rotation.x + (v1x + v2x) / (-7)) % 360;
                    parsedObject.setRotY(rotation.x);
                } else if (!rotation.twoFingers) {
                    System.out.println("Action was MOVE");
                    previousObjectPoint.set(currentObjectPoint.x, currentObjectPoint.y);
                    currentObjectPoint.set((int) event.getX(), (int) event.getY());
                    System.out.println("X:" + currentObjectPoint.x + "   Y:" + currentObjectPoint.y);
                    int deltaX = currentObjectPoint.x - previousObjectPoint.x;
                    int deltaY = currentObjectPoint.y - previousObjectPoint.y;
                    parsedObject.setX(parsedObject.getX() + (deltaX));
                    parsedObject.setY(parsedObject.getY() - (deltaY));
                }
            case (MotionEvent.ACTION_UP):
                System.out.println("Action was UP");
                rotation.twoFingers = false;
            case (MotionEvent.ACTION_CANCEL):
                System.out.println("Action was CANCEL");
            case (MotionEvent.ACTION_OUTSIDE):
                System.out.println("Movement occurred outside bounds " +
                        "of current screen element");
            default:
                //SHIT
        }
    }

    @Override
    public void onRenderFrame(GL10 gl) {

        State state = Renderer.getInstance().begin();


        Renderer.getInstance().drawVideoBackground();

        if (state.getNumTrackableResults() > 0) {
            parsedObject.setVisible(true);

            TrackableResult trackableResult = state.getTrackableResult(0);
            float[] modelViewMatrix = Tool.convertPose2GLMatrix(trackableResult.getPose()).getData();

            float[] modelViewProjectionScaled = new float[16];
            Matrix.multiplyMM(modelViewProjectionScaled, 0, vuforiaAppSession
                    .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

            Matrix4 m = new Matrix4(modelViewProjectionScaled);

            getCurrentCamera().setProjectionMatrix(m);

        } else {
            parsedObject.setVisible(false);
        }

        super.onRenderFrame(gl);
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);

        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
}
