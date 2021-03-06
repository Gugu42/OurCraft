package org.craft.client;

import static org.lwjgl.opengl.GL11.*;

import java.io.*;
import java.util.*;

import org.craft.blocks.*;
import org.craft.client.render.*;
import org.craft.entity.*;
import org.craft.maths.*;
import org.craft.resources.*;
import org.craft.util.*;
import org.craft.util.CollisionInfos.CollisionType;
import org.craft.utils.*;
import org.craft.world.*;
import org.craft.world.populators.*;
import org.lwjgl.input.*;
import org.lwjgl.opengl.*;

public class OurCraft implements Runnable
{

    private File                          gameFolder;
    private int                           displayWidth  = 960;
    private int                           displayHeight = 540;
    private boolean                       running       = false;
    private RenderEngine                  renderEngine  = null;
    private Matrix4                       modelMatrix;
    private Shader                        basicShader;
    private ClasspathSimpleResourceLoader classpathLoader;
    private RenderBlocks                  renderBlocks;
    private World                         clientWorld;
    private MouseHandler                  mouseHandler;
    private EntityPlayer                  player;
    private static OurCraft               instance;
    private CollisionInfos                objectInFront = null;

    public OurCraft()
    {
        instance = this;
        classpathLoader = new ClasspathSimpleResourceLoader();
    }

    public void start()
    {
        new Thread(this).start();
    }

    public void run()
    {
        try
        {
            Display.setResizable(true);
            Display.setTitle("OurCraft");
            Display.setDisplayMode(new DisplayMode(displayWidth, displayHeight));
            Display.create();
            mouseHandler = new MouseHandler();
            mouseHandler.grab();
            running = true;
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            glOrtho(0, Display.getWidth(), Display.getHeight(), 0, -1, 1);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);

            GL11.glLoadIdentity();

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            renderEngine = new RenderEngine();

            Blocks.init();

            WorldGenerator generator = new WorldGenerator()
            {

            };
            generator.addPopulator(new RockPopulator());
            generator.addPopulator(new GrassPopulator());
            clientWorld = new World(new BaseChunkProvider(), generator);
            renderBlocks = new RenderBlocks(renderEngine);

            basicShader = new Shader(IOUtils.readString(OurCraft.class.getResourceAsStream("/assets/shaders/base.vsh"), "UTF-8"), IOUtils.readString(OurCraft.class.getResourceAsStream("/assets/shaders/base.fsh"), "UTF-8"));
            modelMatrix = new Matrix4().initIdentity();

            player = new EntityPlayer(clientWorld);
            player.setLocation(0, 160 + 17, 0);
            clientWorld.spawn(player);
            renderEngine.setRenderViewEntity(player);

            new ThreadGetChunksFromCamera(this).start();
            while(running)
            {
                tick();
                Display.sync(60);
                Display.update();

                if(Display.isCloseRequested()) running = false;
            }
            Log.error("BYE");
            Display.destroy();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void tick()
    {
        render();
        update();
    }

    private void update()
    {
        mouseHandler.update();
        if(player != null)
        {
            objectInFront = player.getObjectInFront(5f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
        {
            running = false;
            return;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
        {
            player.jump();
        }
        clientWorld.update();
    }

    private void render()
    {
        ArrayList<Chunk> visiblesChunks = new ArrayList<>();
        if(player != null)
        {
            int renderDistance = 8;
            int ox = (int)renderEngine.getRenderViewEntity().getPos().x;
            int oy = (int)renderEngine.getRenderViewEntity().getPos().y;
            int oz = (int)renderEngine.getRenderViewEntity().getPos().z;
            for(int x = -renderDistance; x < renderDistance; x++ )
            {
                for(int y = -renderDistance; y < renderDistance; y++ )
                {
                    for(int z = -renderDistance; z < renderDistance; z++ )
                    {
                        int fx = x * 16 + ox;
                        int fy = y * 16 + oy;
                        int fz = z * 16 + oz;
                        if(fy < 0) continue;
                        Chunk c = clientWorld.getChunkProvider().get(clientWorld, (int)Math.floor((float)fx / 16f), (int)Math.floor((float)fy / 16f), (int)Math.floor((float)fz / 16f));
                        if(c != null) visiblesChunks.add(c);
                    }
                }
            }
        }
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        renderEngine.enableGLCap(GL_DEPTH_TEST);
        basicShader.bind();
        basicShader.setUniform("modelview", this.modelMatrix);
        basicShader.setUniform("projection", this.renderEngine.getProjectionMatrix());
        renderBlocks.render(clientWorld, visiblesChunks);

        glClear(GL_DEPTH_BUFFER_BIT);
        renderEngine.disableGLCap(GL_DEPTH_TEST);

        if(objectInFront != null && objectInFront.type == CollisionType.BLOCK)
        {
            glBindTexture(GL_TEXTURE_2D, 0);
            glBegin(GL_LINES);
            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z);
            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z);

            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z);
            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z);

            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z + 1);
            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z + 1);

            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z + 1);
            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z + 1);

            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z);
            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z + 1);

            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z);
            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z + 1);

            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z);
            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z + 1);

            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z);
            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z + 1);

            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z);
            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z);

            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z);
            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z);

            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z + 1);
            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z + 1);

            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z + 1);
            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z + 1);

            glEnd();
        }

    }

    public File getGameFolder()
    {
        if(gameFolder == null)
        {
            String appdata = System.getenv("APPDATA");
            if(appdata != null)
                gameFolder = new File(appdata, ".ourcraft");
            else
                gameFolder = new File(System.getProperty("user.home"), ".ourcraft");
        }
        return gameFolder;
    }

    public static OurCraft getOurCraft()
    {
        return instance;
    }

    public ResourceLoader getBaseLoader()
    {
        return classpathLoader;
    }

    public MouseHandler getMouseHandler()
    {
        return mouseHandler;
    }

    public CollisionInfos getObjectInFront()
    {
        return objectInFront;
    }

    public boolean isRunning()
    {
        return running;
    }

    public World getClientWorld()
    {
        return clientWorld;
    }

    public RenderEngine getRenderEngine()
    {
        return renderEngine;
    }
}
