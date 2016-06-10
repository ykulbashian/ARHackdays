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
import com.shopify.hackday.ar.vuforia.utils.CubeShaders;
import com.shopify.hackday.ar.vuforia.utils.SampleUtils;
import com.shopify.hackday.ar.vuforia.utils.Teapot;
import com.shopify.hackday.ar.vuforia.utils.Texture;
import com.vuforia.CameraCalibration;
import com.vuforia.CameraDevice;
import com.vuforia.ImageTargetResult;
import com.vuforia.Matrix34F;
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
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class VirtualButtonRenderer extends org.rajawali3d.renderer.Renderer implements GLSurfaceView.Renderer {

    private static final String LOGTAG = "VirtualButtonRenderer";

    private SampleApplicationSession vuforiaAppSession;

    public boolean mIsActive = false;

    private VirtualButtonsActivity mActivity;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private Teapot mTeapot = new Teapot();

    // OpenGL ES 2.0 specific (3D model):
    private int shaderProgramID = 0;
    private int vertexHandle = 0;
    private int normalHandle = 0;
    private int textureCoordHandle = 0;
    private int mvpMatrixHandle = 0;
    private int texSampler2DHandle = 0;

    // Constants:
    static private float kTeapotScale = 3.f;


    public VirtualButtonRenderer(VirtualButtonsActivity activity,
                                 SampleApplicationSession session) {
        super(activity);
        mActivity = activity;
        vuforiaAppSession = session;
    }

    public void setIsActive(boolean isActive) {
        this.mIsActive = isActive;
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");


        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();


        // Call function to initialize rendering:
        initRendering();

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }

    public void onSurfaceDestroyed() {

        // Unload texture:
        mTextures.clear();
        mTextures = null;
    }

    // We want to load specific textures from the APK, which we will later use
    // for rendering.
    private void loadTextures() {
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBrass.png",
                mActivity.getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotRed.png",
                mActivity.getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png",
                mActivity.getAssets()));
        mTextures.add(Texture.loadTextureFromApk(
                "VirtualButtons/TextureTeapotYellow.png", mActivity.getAssets()));
        mTextures.add(Texture.loadTextureFromApk(
                "VirtualButtons/TextureTeapotGreen.png", mActivity.getAssets()));
    }

    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }


    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

        // Call our function to render content
        renderFrame();
    }


    private void initRendering() {
        Log.d(LOGTAG, "VirtualButtonsRenderer.initRendering");

        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);

        // Now generate the OpenGL texture objects and add settings
        for (Texture t : mTextures) {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");

    }


    private void renderFrame() {
        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Get the state from Vuforia and mark the beginning of a rendering section
        State state = Renderer.getInstance().begin();

        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // We must detect if background reflection is active and adjust the
        // culling direction.
        // If the reflection is active, this means the post matrix has been
        // reflected as well,
        // therefore counter standard clockwise face culling will result in
        // "inside out" models.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera

        // Set the viewport
        int[] viewport = vuforiaAppSession.getViewport();
        GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

        // Did we find any trackables this frame?
        if (state.getNumTrackableResults() > 0) {
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(0);
            float[] modelViewMatrix = Tool.convertPose2GLMatrix(trackableResult.getPose()).getData();

            // The image target specific result:
            assert (trackableResult.getType() == ImageTargetResult
                    .getClassType());
            ImageTargetResult imageTargetResult = (ImageTargetResult) trackableResult;

            // Set transformations:
            float[] modelViewProjection = new float[16];
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                    .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

            // Set the texture used for the teapot model:
            int textureIndex = 0;

            // Assumptions:
            assert (textureIndex < mTextures.size());
            Texture thisTexture = mTextures.get(textureIndex);

            // Scale 3D model
            float[] modelViewScaled = modelViewMatrix;
            Matrix.scaleM(modelViewScaled, 0, kTeapotScale, kTeapotScale, kTeapotScale);

            float[] modelViewProjectionScaled = new float[16];
            Matrix.multiplyMM(modelViewProjectionScaled, 0, vuforiaAppSession
                    .getProjectionMatrix().getData(), 0, modelViewScaled, 0);

            // Render 3D model
            GLES20.glUseProgram(shaderProgramID);

            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                    false, 0, mTeapot.getVertices());
            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                    false, 0, mTeapot.getNormals());
            GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                    GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());

            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                    thisTexture.mTextureID[0]);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                    modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                    mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                    mTeapot.getIndices());

            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);

            SampleUtils.checkGLError("VirtualButtonsActivity renderFrame");

        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        Renderer.getInstance().end();

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

    //    private PointLight light1;
    private Point currentObjectPoint, previousObjectPoint;
    private MotionEvent.PointerCoords curPointer1, curPointer2, prevPointer1, prevPointer2;


    @Override
    protected void initScene() {
        Animation3D cameraAnim, lightAnim;

        currentObjectPoint = new Point(0, 0);
        previousObjectPoint = new Point(0, 0);
        curPointer1 = new MotionEvent.PointerCoords();
        prevPointer1 = new MotionEvent.PointerCoords();
        curPointer2 = new MotionEvent.PointerCoords();
        prevPointer2 = new MotionEvent.PointerCoords();

        light1 = new PointLight();
        light1.setPosition(5, 5, 5);
        light1.setPower(8);
        light2 = new PointLight();
        light2.setPosition(5, 5, 5);
        light2.setPower(8);

        getCurrentScene().addLight(light1);
        getCurrentScene().addLight(light2);
        getCurrentCamera().setZ(8);
//        getCurrentCamera().setPosition(0, 2, 4);
//        getCurrentCamera().setLookAt(0, 0, 0);

        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.model01_obj);
        try {
            objParser.parse();
            parsedObject = objParser.getParsedObject();
            getCurrentScene().addChild(parsedObject);

        } catch (ParsingException e) {
            e.printStackTrace();
        }

/*
        cameraAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
        cameraAnim.setDurationMilliseconds(8000);
        cameraAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
        cameraAnim.setTransformable3D(parsedObject);

        lightAnim = new EllipticalOrbitAnimation3D(new Vector3(),
                new Vector3(0, 10, 0), Vector3.getAxisVector(Vector3.Axis.Z), 0,
                360, EllipticalOrbitAnimation3D.OrbitDirection.CLOCKWISE);

        lightAnim.setDurationMilliseconds(3000);
        lightAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
        lightAnim.setTransformable3D(light);

        getCurrentScene().registerAnimation(cameraAnim);
        getCurrentScene().registerAnimation(lightAnim);
*/

        getCurrentScene().alwaysClearColorBuffer(false);

/*
        cameraAnim.play();
        lightAnim.play();
*/
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {
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
                    parsedObject.setX(parsedObject.getX() + (deltaX / 300F));
                    parsedObject.setZ(parsedObject.getZ() + (deltaY / 300F));
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

        onDrawFrame(gl);

        // FIXME / TODO This is broken, but could be a good start to hook tracking data to model.
        // Get the state from Vuforia and mark the beginning of a rendering section
        State state = Renderer.getInstance().begin();
        // Did we find any trackables this frame?
        if (state.getNumTrackableResults() > 0) {
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(0);
            float[] modelViewMatrix = Tool.convertPose2GLMatrix(trackableResult.getPose()).getData();
//            parsedObject.getModelViewMatrix().setAll(modelViewMatrix);

            // Looks potentially like the right thing to do.
            getCurrentCamera().setProjectionMatrix(new Matrix4(vuforiaAppSession.getProjectionMatrix().getData()));
        }
        // FIXME / TODO This is broken, but could be a good start to hook tracking data to model.

        super.onRenderFrame(gl);
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);

        onSurfaceCreated(gl, config);
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);

        onSurfaceChanged(gl, width, height);
    }
}
