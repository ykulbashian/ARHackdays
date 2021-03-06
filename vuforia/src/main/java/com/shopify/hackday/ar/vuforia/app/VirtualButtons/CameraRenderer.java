/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.shopify.hackday.ar.vuforia.app.VirtualButtons;

import android.graphics.Point;
import android.opengl.Matrix;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;

import com.shopify.hackday.ar.vuforia.R;
import com.shopify.hackday.ar.vuforia.ApplicationSession;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackableResult;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


class CameraRenderer extends org.rajawali3d.renderer.Renderer {

    private static final String LOGTAG = "CameraRenderer";

    private ApplicationSession vuforiaAppSession;
    private boolean isActive;


    CameraRenderer(BaseActivity activity,
                          ApplicationSession session) {
        super(activity);
        vuforiaAppSession = session;
    }


    void onSurfaceDestroyed() {

    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }


    private static class FloatingPoint {

        double x;
        boolean twoFingers;

        FloatingPoint() {
            x = 0;
            twoFingers = false;
        }
    }

    private final FloatingPoint rotation = new FloatingPoint();

    private Object3D parsedObject;

    private Point currentObjectPoint, previousObjectPoint;
    private MotionEvent.PointerCoords curPointer1, curPointer2, prevPointer1, prevPointer2;

    private static final float SCALE_FACTOR = 20f;
    private static final float LIGHT_SCALE_FACTOR = 10f;

    @Override
    protected void initScene() {

        currentObjectPoint = new Point(0, 0);
        previousObjectPoint = new Point(0, 0);
        curPointer1 = new MotionEvent.PointerCoords();
        prevPointer1 = new MotionEvent.PointerCoords();
        curPointer2 = new MotionEvent.PointerCoords();
        prevPointer2 = new MotionEvent.PointerCoords();

        PointLight light1 = new PointLight();
        light1.setPosition(-2*LIGHT_SCALE_FACTOR, 2*LIGHT_SCALE_FACTOR, -2*LIGHT_SCALE_FACTOR);
        light1.setPower(2*LIGHT_SCALE_FACTOR);
        PointLight light2 = new PointLight();
        light2.setPosition(5*LIGHT_SCALE_FACTOR, 5*LIGHT_SCALE_FACTOR, 5*LIGHT_SCALE_FACTOR);
        light2.setPower(8*LIGHT_SCALE_FACTOR);

        getCurrentScene().addLight(light1);
        getCurrentScene().addLight(light2);
        getCurrentScene().alwaysClearColorBuffer(false);

        getCurrentCamera().setFarPlane(1000);

        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.untitled);
        try {
            objParser.parse();
            parsedObject = objParser.getParsedObject();
//            parsedObject.setRotZ(-90);
            parsedObject.setPosition(-SCALE_FACTOR/2, -SCALE_FACTOR/2, 0);
            parsedObject.setScale(new Vector3(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR));
            parsedObject.setBackSided(true);


            Material material = new Material();
            material.addTexture(new org.rajawali3d.materials.textures.Texture("material0",
                    R.drawable.couch_image));
            material.setColorInfluence(0);
//            parsedObject.setMaterial(material);

            getCurrentScene().addChild(parsedObject);

        } catch (ParsingException | ATexture.TextureException e) {
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
                    rotation.twoFingers = true;
                    event.getPointerCoords(0, curPointer1);
                    event.getPointerCoords(0, prevPointer1);
                    event.getPointerCoords(1, curPointer2);
                    event.getPointerCoords(1, prevPointer2);
                } else {
                    currentObjectPoint.set((int) event.getX(), (int) event.getY());
                    previousObjectPoint.set(currentObjectPoint.x, currentObjectPoint.y);
                }
            case (MotionEvent.ACTION_MOVE):
                if (event.getPointerCount() == 2) {
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
                    previousObjectPoint.set(currentObjectPoint.x, currentObjectPoint.y);
                    currentObjectPoint.set((int) event.getX(), (int) event.getY());
                    int deltaX = currentObjectPoint.x - previousObjectPoint.x;
                    int deltaY = currentObjectPoint.y - previousObjectPoint.y;
                    parsedObject.setX(parsedObject.getX() + (deltaX));
                    parsedObject.setY(parsedObject.getY() - (deltaY));
                }
            case (MotionEvent.ACTION_UP):
                rotation.twoFingers = false;
            case (MotionEvent.ACTION_CANCEL):
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
