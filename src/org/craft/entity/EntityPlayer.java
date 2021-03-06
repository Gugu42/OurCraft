package org.craft.entity;

import org.craft.blocks.*;
import org.craft.client.*;
import org.craft.maths.*;
import org.craft.util.*;
import org.craft.util.CollisionInfos.CollisionType;
import org.craft.world.*;
import org.lwjgl.input.*;

public class EntityPlayer extends Entity
{

    public EntityPlayer(World world)
    {
        super(world);
        setSize(0.85f, 1.80f, 0.85f);
    }

    public void onEntityUpdate()
    {
        float speed = 1f / 10f;
        if(!isOnGround()) speed /= 2f;
        if(Keyboard.isKeyDown(Keyboard.KEY_Z))
        {
            this.moveForward(speed);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_S))
        {
            this.moveBackwards(speed);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_Q))
        {
            this.moveLeft(speed);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_D))
        {
            this.moveRight(speed);
        }

        rotate(Vector3.yAxis, (float)Math.toRadians(OurCraft.getOurCraft().getMouseHandler().getDX()));
        rotate(getRotation().getRight(), (float)Math.toRadians(-OurCraft.getOurCraft().getMouseHandler().getDY()));

        CollisionInfos infos = OurCraft.getOurCraft().getObjectInFront();
        if(infos.type == CollisionType.BLOCK)
        {
            if(Mouse.isButtonDown(0))
                getWorld().setBlock((int)infos.x + infos.side.getTranslationX(), (int)(infos.y) + infos.side.getTranslationY(), (int)(infos.z + infos.side.getTranslationZ()), Blocks.dirt);
            else if(Mouse.isButtonDown(1)) getWorld().setBlock((int)Math.round(infos.x), (int)Math.round(infos.y), (int)Math.round(infos.z), Blocks.air);
        }
    }

    public float getEyeOffset()
    {
        return 1.7f;
    }

}
