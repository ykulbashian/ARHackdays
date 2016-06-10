package com.shopify.hackday.ar.vuforia;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.shopify.hackday.ar.vuforia.utils.Texture;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ykulbashian on 2016-06-09.
 */


public final class LoadedModelRenderer extends Renderer {

    private PointLight light;
    private Object3D objectGroup;
    private Animation3D cameraAnim, lightAnim;

    private SampleApplicationSession vuforiaAppSession;

    public boolean mIsActive = false;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    public LoadedModelRenderer(Context context, SampleApplicationSession session) {
        super(context);
        this.vuforiaAppSession = session;
    }

    public void setIsActive(boolean isActive){
        this.mIsActive = isActive;
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

        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();

//            getCurrentScene().setBackgroundColor(0f,0f,1f,0f);
    }

    // We want to load specific textures from the APK, which we will later use
    // for rendering.
    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBrass.png",
                mContext.getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotRed.png",
                mContext.getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png",
                mContext.getAssets()));
        mTextures.add(Texture.loadTextureFromApk(
                "VirtualButtons/TextureTeapotYellow.png", mContext.getAssets()));
        mTextures.add(Texture.loadTextureFromApk(
                "VirtualButtons/TextureTeapotGreen.png", mContext.getAssets()));
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
    }

    @Override
    public void onRenderFrame(GL10 gl) {

        if (!mIsActive)
            return;

        super.onRenderFrame(gl);
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }

    @Override
    public void onRenderSurfaceDestroyed(SurfaceTexture surface) {
        super.onRenderSurfaceDestroyed(surface);

        // Unload texture:
        mTextures.clear();
        mTextures = null;
    }
}