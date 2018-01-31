package com.ulfben.spaceshooter;
//Created by Ulf Benjaminsson (ulfben) on 2018-01-20.
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Random;
/**
*   Entity
*       mX / mY (coordinates)
*       width / height
*       mVelocity (x/y)
*       input()
*       update()
*       render(surface)
*       isColliding(Entity);
*       onCollision(Entity);
*
  *   Player -> Entity
  *   Enemy -> Entity
*     Star -> Entity
*
* */
public class Entity {
    protected static Random mDice = new Random();
    protected float mX = 0;
    protected float mY = 0;
    protected float mWidth = 0;
    protected float mHeight = 0;
    protected float mVelocityX = 0;
    protected float mVelocityY = 0;
    public Entity(){}
    public Entity(float x, float y){
        mX = x;
        mY = y;
    }


    public void worldWrap(final float width, final float height){
        mX = Utils.wrap(mX, -mWidth, width+mWidth);
    }

    public void respawn(){};

    public void input(Game game){}

    public void update(){
        mX += mVelocityX;
        mY += mVelocityY;
    }
    public void render(final Canvas canvas, final Paint paint){

    }

    public boolean isColliding(Entity that){
        return Entity.intersectsAABB(this, that);
    }

    public void onCollision(Entity that){
        // no default implementation
    }

    public static boolean intersectsAABB(Entity a, Entity b) {
        return !(a.right() < b.left()
                || b.right() < a.left()
                || a.bottom() < b.top()
                || b.bottom() < a.top());
    }

    public float left(){
        return mX;
    }
    public float right(){
        return mX+mWidth;
    }
    public float top(){
        return mY;
    }
    public float bottom(){
        return mY+mHeight;
    }
    public float getX() {
        return mX;
    }
    public void setX(final float x) {
        mX = x;
    }
    public float getY() {
        return mY;
    }
    public void setY(final float y) {
        mY = y;
    }
    public float getWidth() {
        return mWidth;
    }
    public void setWidth(final float width) {
        mWidth = width;
    }
    public float getHeight() {
        return mHeight;
    }
    public void setHeight(final float height) {
        mHeight = height;
    }
    public float getVelocityX() {
        return mVelocityX;
    }
    public void setVelocityX(final float velocityX) {
        mVelocityX = velocityX;
    }
    public float getVelocityY() {
        return mVelocityY;
    }
    public void setVelocityY(final float velocityY) {
        mVelocityY = velocityY;
    }
}
