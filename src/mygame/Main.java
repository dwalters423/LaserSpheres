package mygame;


/*
 * 
 * All sound effects taken from soundbible.com under Public Domain
 * Skybox from: 
 *      http://skybox.xaelan.com/ (Author [spaceman])
 * Explosion effect code sampled and edited from: 
 *      https://code.google.com/p/jmonkeyengine/source/browse/trunk/engine/src/test/jme3test/effect/TestExplosionEffect.java
 * 
 */
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.ui.Picture;
import com.jme3.util.SkyFactory;

public class Main extends SimpleApplication {
    
    private RigidBodyControl playerController = new RigidBodyControl();
    private RigidBodyControl blueSphereController, redSphereController, yellowSphereController,
            cyanSphereController, healthSphereController;
    protected RigidBodyControl laserController;
    private BulletAppState physicsState = new BulletAppState();
    protected  CharacterControl character;
    private Material wallMaterial, floorMaterial, laserMaterial, blueEnemyMat, redEnemyMat,
            yellowEnemyMat, cyanEnemyMat, healthMaterial, healthBarMaterial;
    private Node environmentNode = new Node();
    private Node charModel;
    private boolean left = false, right = false, up = false, down = false, jump = false;
    private Vector3f walkDirection = new Vector3f(0,0,0); 
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();    
    protected Node laser = new Node(), blueSphereNode = new Node(), redSphereNode = new Node(), 
            yellowSphereNode = new Node(), cyanSphereNode = new Node(), healthSphereNode = new Node();
    private Node explosionNode = new Node();
    private ParticleEmitter shockwave, flash, flame, smoketrail;
    private static final int COUNT_FACTOR = 1;
    private static final float COUNT_FACTOR_F = 1f;
    private static final Type EMITTER_TYPE = true ? Type.Point : Type.Triangle;
    private int score = 0;
    private int shotsFired = 0;
    private int blueDestroyed = 0;
    private int redDestroyed = 0;
    private int yellowDestroyed = 0;
    private int cyanDestroyed = 0;
    private int hitCount = 0;
    private int playerHealth = 100;
    protected BitmapText scoreHUD, highScoreHUD, blueHUD, redHUD, yellowHUD, cyanHUD, 
            shotsFiredHUD, healthHUD, timeHUD;
    private RandomPhysics randomPhy = new RandomPhysics();
    private Box healthBarBox;
    private Geometry healthBar;
    private AudioNode explosionSound, heartBeatSound, laserSound, healthSound, punchSound;
    private String endReason;
    private boolean blueCollide = false, redCollide = false, yellowCollide = false,
            cyanCollide = false, healthCollide=false;
    private boolean playerBlueCollide = false, playerRedCollide = false, playerYellowCollide = false,
            playerCyanCollide = false, playerHealthCollide = false;
    
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        
       flyCam.setEnabled(true);

       cam.setFrustumFar(2000);
        
      //initializes glow with the FilterPost Processor  
       FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
       BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
       bloom.setBloomIntensity(3f);
       fpp.addFilter(bloom);    
       viewPort.addProcessor(fpp);
        
        
     //initializes key inputs   
       initKeyMapping();
        
      //initialzies global physics  
        stateManager.attach(physicsState);
        physicsState.setSpeed(4);
        
      //calls void methods to initialize all objects in the scene
        initSounds();
        initMaterials();
        initGameGUI();
        initEnvironment();
        initPlayer();
        
        
        initBlueEnemy();
        initRedEnemy();
        initYellowEnemy();
        initCyanEnemy();
        initHealthSphere();
        
      //initializes the particle effects for the explosion of enemies.
        createShockwave();
        createFlash();
        createFlame();
        createSmokeTrail();
        explosionNode.scale(5f);
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        
      //sets the movement for the player  
        camDir.set(cam.getDirection()).multLocal(0.6f);
        camLeft.set(cam.getLeft()).multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        character.setWalkDirection(walkDirection);
        cam.setLocation(character.getPhysicsLocation());
        
      //sets the audio listener  
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
        
        if (playerHealth <= 0){
            endGame();
        }
        sphereOutofBoundsChecker();
        collideChecker();
        
    } //end update loop.
    
    /*
     * Initialize the key mapping for the player control.
     */
    private void initKeyMapping() {
      
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "Left", "Right");
        inputManager.addListener(actionListener, "Forward", "Back");
        inputManager.addListener(actionListener, "Jump", "Shoot");
  }

    /*
     * Adds the action listener for keyboard controls.
     */
    private ActionListener actionListener = new ActionListener() {
        
        public void onAction(String binding, boolean value, float tpf) {
            if (binding.equals("Left")) {
                left = value;    
            } else if (binding.equals("Right")) {
                right = value;
            } else if (binding.equals("Forward")) {
                up = value;
            } else if (binding.equals("Back")) {
                down = value; 
            } if (binding.equals("Jump")) {
                character.jump();
            } if (binding.equals("Shoot") && !value)
                shoot();
        } //end onAction        
    }; //end actionListener
    
    
    /*
     * Initiates all in game Heads up Display
     */
    private void initGameGUI() {
        
      //to determine y values for enemys destroyed HUD  
        float y;
        
      //initiates the heads up display aimer  
        Picture aimSight = new Picture ("HUD Sight");
        aimSight.setImage(assetManager, "Sprites/aimsight.gif", true);
        aimSight.setPosition(settings.getWidth()/2, settings.getHeight()/2 - 15);
        aimSight.setWidth(64);
        aimSight.setHeight(40);
        guiNode.attachChild(aimSight);
        
      //initiates the score  
        BitmapText scoreAnnouncerHUD = new BitmapText(guiFont);
        scoreAnnouncerHUD.setSize(guiFont.getCharSet().getRenderedSize()+5);
        scoreAnnouncerHUD.setColor(ColorRGBA.Green);                        
        scoreAnnouncerHUD.setText("SCORE:");          
        scoreAnnouncerHUD.setLocalTranslation(10, settings.getHeight(), 0);
        guiNode.attachChild(scoreAnnouncerHUD);
        
        scoreHUD = new BitmapText(guiFont);
        scoreHUD.setSize(guiFont.getCharSet().getRenderedSize());
        scoreHUD.setColor(ColorRGBA.White);
        scoreHUD.setText(Integer.toString(score));
        scoreHUD.setLocalTranslation(15, settings.getHeight()-scoreAnnouncerHUD.getHeight()-3, 0);
        guiNode.attachChild(scoreHUD);
        
                
      //initiates total for each ball 
        BitmapText enemyAnnouncerHUD = new BitmapText(guiFont);
        enemyAnnouncerHUD.setSize(guiFont.getCharSet().getRenderedSize()+5);
        enemyAnnouncerHUD.setColor(ColorRGBA.Green);
        enemyAnnouncerHUD.setText("ENEMIES DESTROYED:");
        enemyAnnouncerHUD.setLocalTranslation(settings.getWidth() - enemyAnnouncerHUD.getLineWidth() - 10, settings.getHeight(), 0);
        guiNode.attachChild(enemyAnnouncerHUD);
        
        y = settings.getHeight() - enemyAnnouncerHUD.getHeight() - 3;
        
        blueHUD = new BitmapText(guiFont);
        blueHUD.setSize(guiFont.getCharSet().getRenderedSize());
        blueHUD.setColor(ColorRGBA.Blue);
        blueHUD.setText(Integer.toString(score));
        blueHUD.setLocalTranslation(settings.getWidth() - blueHUD.getLineWidth() - 20, y, 0);
        guiNode.attachChild(blueHUD);
        
        y = y - blueHUD.getHeight() - 3;
        
        redHUD = new BitmapText(guiFont);
        redHUD.setSize(guiFont.getCharSet().getRenderedSize());
        redHUD.setColor(ColorRGBA.Red);
        redHUD.setText(Integer.toString(redDestroyed));
        redHUD.setLocalTranslation(settings.getWidth() - redHUD.getLineWidth() - 20, y, 0);
        guiNode.attachChild(redHUD);
        
        y = y - redHUD.getHeight() - 3;
        
        yellowHUD = new BitmapText(guiFont);
        yellowHUD.setSize(guiFont.getCharSet().getRenderedSize());
        yellowHUD.setColor(ColorRGBA.Yellow);
        yellowHUD.setText(Integer.toString(yellowDestroyed));
        yellowHUD.setLocalTranslation(settings.getWidth() - yellowHUD.getLineWidth() - 20, y, 0);
        guiNode.attachChild(yellowHUD);
        
        y = y - yellowHUD.getHeight() - 3;
        
        cyanHUD = new BitmapText(guiFont);
        cyanHUD.setSize(guiFont.getCharSet().getRenderedSize());
        cyanHUD.setColor(ColorRGBA.Cyan);
        cyanHUD.setText(Integer.toString(cyanDestroyed));
        cyanHUD.setLocalTranslation(settings.getWidth() - cyanHUD.getLineWidth() - 20, y, 0);
        guiNode.attachChild(cyanHUD);
        
        shotsFiredHUD = new BitmapText(guiFont);
        shotsFiredHUD.setSize(guiFont.getCharSet().getRenderedSize());
        shotsFiredHUD.setColor(ColorRGBA.White);
        shotsFiredHUD.setText(Integer.toString(shotsFired));
        shotsFiredHUD.setLocalTranslation(settings.getWidth() - shotsFiredHUD.getLineWidth() - 30, 50, 0);
        guiNode.attachChild(shotsFiredHUD);
        
        BitmapText shotAnnouncerHUD = new BitmapText(guiFont);
        shotAnnouncerHUD.setSize(guiFont.getCharSet().getRenderedSize()+5);
        shotAnnouncerHUD.setColor(ColorRGBA.Green);                        
        shotAnnouncerHUD.setText("SHOTS FIRED:");          
        shotAnnouncerHUD.setLocalTranslation(settings.getWidth() - shotAnnouncerHUD.getLineWidth() - 10, shotsFiredHUD.getLineHeight()+60, 0);
        guiNode.attachChild(shotAnnouncerHUD);
        
              
      //displays the time
        
        timeHUD = new BitmapText(guiFont);
        timeHUD.setSize(guiFont.getCharSet().getRenderedSize()+5);
        timeHUD.setColor(ColorRGBA.Green);
        timeHUD.setText("5:00");
        timeHUD.setLocalTranslation((settings.getWidth()/2) - (timeHUD.getLineWidth()/2),
               timeHUD.getLineHeight(), 0);
        guiNode.attachChild(timeHUD);
        
        
        BitmapText timeAnnouncerHUD = new BitmapText(guiFont);
        timeAnnouncerHUD.setSize(guiFont.getCharSet().getRenderedSize()+5);
        timeAnnouncerHUD.setColor(ColorRGBA.White);
        timeAnnouncerHUD.setText("TIME:");
        timeAnnouncerHUD.setLocalTranslation((settings.getWidth()/2) - (timeAnnouncerHUD.getLineWidth()/2),
                (timeAnnouncerHUD.getLineHeight()) + (timeHUD.getLineHeight()) + 2 , 0);
        guiNode.attachChild(timeAnnouncerHUD);
        

        
        BitmapText healthAnnouncerHUD = new BitmapText(guiFont);
        healthAnnouncerHUD.setSize(guiFont.getCharSet().getRenderedSize()+5);
        healthAnnouncerHUD.setColor(ColorRGBA.White);
        healthAnnouncerHUD.setText("HEALTH:");
        healthAnnouncerHUD.setLocalTranslation((settings.getWidth() / 2) - (healthAnnouncerHUD.getLineWidth()/2), settings.getHeight(), 0);
        guiNode.attachChild(healthAnnouncerHUD);
        
        
      //creates health bar  
        healthBarBox = new Box (playerHealth,10,0);
        healthBar = new Geometry ("Health Bar", healthBarBox);
        healthBar.setMaterial (healthBarMaterial);
        healthBar.setLocalTranslation((settings.getWidth() / 2), settings.getHeight()
                - healthAnnouncerHUD.getLineHeight() - 10 , 0);
        guiNode.attachChild(healthBar);
        
        healthHUD = new BitmapText(guiFont);
        healthHUD.setSize(16);
        healthHUD.setColor(ColorRGBA.White);
        healthHUD.setText(Integer.toString(playerHealth));
        healthHUD.setLocalTranslation((settings.getWidth() / 2) - (healthHUD.getLineWidth()/2), settings.getHeight()
                - healthAnnouncerHUD.getLineHeight(), 0);
        guiNode.attachChild(healthHUD);
        
    }
    
    
    /*
     * Sets the textures for the game, including walls, floors, and targets.
     */
    private void initMaterials() {
        
      //sets the materials for the walls  
        wallMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        floorMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        wallMaterial.setTexture("DiffuseMap", assetManager.loadTexture("Textures/metalsurface.jpg"));
        floorMaterial.setTexture("DiffuseMap", assetManager.loadTexture("Textures/MetaxTexture.jpg"));
      
      //sets the  material for the lasers
        laserMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        laserMaterial.setColor("Color", new ColorRGBA (0,1,0, 0.8f));
        laserMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        laserMaterial.setColor("GlowColor", ColorRGBA.Green);
        
      //sets materials for enemy spheres
        redEnemyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        redEnemyMat.setColor("Color", ColorRGBA.Red);
        blueEnemyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blueEnemyMat.setColor("Color", ColorRGBA.Blue);        
        yellowEnemyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        yellowEnemyMat.setColor("Color", ColorRGBA.Yellow);    
        cyanEnemyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        cyanEnemyMat.setColor("Color", ColorRGBA.Cyan);  
        
      //sets health sphere material
        healthMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        healthMaterial.setColor("Color", ColorRGBA.White);
        healthMaterial.setTexture("ColorMap", assetManager.loadTexture("Textures/healthsphere.jpg"));
        healthMaterial.setColor("GlowColor", ColorRGBA.Red);
        
        healthBarMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        healthBarMaterial.setColor("Color", ColorRGBA.Red);
        
    } //end initMaterials
    
    /*
     * Initializes the environment, lighting and skybox.
     */    
    private void initEnvironment() {     
        
      //creates the floor and attaches it to environmentNode  
        Box floorShape = new Box(500,0,500);
        Geometry floor = new Geometry ("Bottom", floorShape);
        floor.setMaterial(floorMaterial);
        floor.setLocalTranslation(0,0,0);
        environmentNode.attachChild(floor);
        
      //creates the wall boxes and attaches them to the cubeNode
        Box rightWallBox = new Box(Vector3f.ZERO, 0,50,500);
        Geometry rightWall = new Geometry ("Right Wall", rightWallBox);
        rightWall.setMaterial(wallMaterial);
        rightWall.setLocalTranslation(500,0,0); //should this be Control.setPhysicsLocation()
        environmentNode.attachChild(rightWall);
        
        Box leftWallBox = new Box(Vector3f.ZERO, 0,50,500);
        Geometry leftWall = new Geometry ("Left Wall", leftWallBox);
        leftWall.setMaterial(wallMaterial);
        leftWall.setLocalTranslation(-500,0,0);
        environmentNode.attachChild(leftWall);
        
        Box frontWallBox = new Box(Vector3f.ZERO, 500,50,0);
        Geometry frontWall = new Geometry ("Front Wall", frontWallBox);
        frontWall.setMaterial(wallMaterial);
        frontWall.setLocalTranslation(0,0,500);
        environmentNode.attachChild(frontWall);
        
        Box backWallBox = new Box(Vector3f.ZERO, 500,50,0);
        Geometry backWall = new Geometry ("Back Wall", backWallBox);
        backWall.setMaterial(wallMaterial);
        backWall.setLocalTranslation (0,0,-500);
        environmentNode.attachChild(backWall);        
        
      //initializes the lighting for the scene
        AmbientLight ambient1 = new AmbientLight();
        ambient1.setColor(ColorRGBA.White);
        environmentNode.addLight(ambient1);
        AmbientLight ambient2 = new AmbientLight();
        ambient2.setColor(ColorRGBA.White);
        environmentNode.addLight(ambient2);
        
       //initializes the skybox for the scene.
       //Skybox .dds file taken from: http://skybox.xaelan.com/ (see citation)
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/BackgroundCube.dds", false));
        
        
       //initializes the collision detection for the environment  
        CollisionShape environmentCollisionShape = CollisionShapeFactory.createMeshShape((Node)environmentNode);
        RigidBodyControl environmentControl = new RigidBodyControl(environmentCollisionShape,0); //ensure mass==0
        environmentNode.addControl(environmentControl);
        environmentControl.setRestitution(0.5f);
        physicsState.getPhysicsSpace().add(environmentControl);
        
        
       //attaches environment to the scene  
        rootNode.attachChild(environmentNode);    
    } //end initEnvironment
    
    private void initPlayer() {
        
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(10, 80f);
        character = new CharacterControl(capsule, 10f);
        CharacterCollisionControl collisionControlTick = new CharacterCollisionControl(capsule);
        character.setJumpSpeed(50f);
        charModel = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        charModel.setName("Player");
        charModel.addControl(character);
        charModel.addControl(collisionControlTick);
        physicsState.getPhysicsSpace().add(character);
        physicsState.getPhysicsSpace().add(collisionControlTick);
        physicsState.getPhysicsSpace().addTickListener(collisionControlTick);
        //charModel.setLocalTranslation(0, 20, 0);
        character.setPhysicsLocation(new Vector3f (0,50,0));
        rootNode.attachChild(charModel);
        
        
    } //end initPlayer
    
    private void initBlueEnemy() {
                
      //initiates the sphere geometry
        Sphere blueSphere = new Sphere (10,10,10);
        Geometry blueSphereGeom = new Geometry("Blue Enemy", blueSphere);
        blueSphereGeom.setMaterial(blueEnemyMat);
        
        blueSphereController = new RigidBodyControl (10);
        blueSphereNode.setName("Blue");
        blueSphereNode.attachChild(blueSphereGeom);
        blueSphereNode.addControl(blueSphereController);
        
        blueSphereController.setPhysicsLocation(randomPhy.getRandomYPosVector3f(450, 20, 450));
        blueSphereController.setLinearVelocity(randomPhy.getRandomVector3f(100, 0, 100));
        
      //adds the collision object to the physics space
        physicsState.getPhysicsSpace().add(blueSphereController);
        
      //attaches Node to scene  
        rootNode.attachChild(blueSphereNode);

    } //end initBlueEnemy()   
    
    private void initRedEnemy() {
                    
      //initiates the sphere geometry
        Sphere redSphere = new Sphere (15,15,15);
        Geometry redSphereGeom = new Geometry("Red Enemy", redSphere);
        redSphereGeom.setMaterial(redEnemyMat);
        
        redSphereController = new RigidBodyControl (10);
        redSphereNode.setName("Red");
        redSphereNode.attachChild(redSphereGeom);
        redSphereNode.addControl(redSphereController); 
        
        redSphereController.setPhysicsLocation(randomPhy.getRandomYPosVector3f(450, 20, 450));
        redSphereController.setLinearVelocity(randomPhy.getRandomVector3f(100, 0, 100));
        
      //adds the collision object to the physics space
        physicsState.getPhysicsSpace().add(redSphereController);
        
      //attaches Node to scene  
        rootNode.attachChild(redSphereNode);

    }//end initRedEnemy()   
    
    private void initYellowEnemy() {
               
      //initiates the sphere geometry
        Sphere yellowSphere = new Sphere (20,20,20);
        Geometry yellowSphereGeom = new Geometry("Yellow Enemy", yellowSphere);
        yellowSphereGeom.setMaterial(yellowEnemyMat);
        
        yellowSphereController = new RigidBodyControl (10);
        yellowSphereNode.setName("Yellow");
        yellowSphereNode.attachChild(yellowSphereGeom);
        yellowSphereNode.addControl(yellowSphereController);
        
        yellowSphereController.setPhysicsLocation(randomPhy.getRandomYPosVector3f(450, 20, 450));
        yellowSphereController.setLinearVelocity(randomPhy.getRandomVector3f(100, 0, 100));
        
      //adds the collision object to the physics space
        physicsState.getPhysicsSpace().add(yellowSphereController);
        
      //attaches Node to scene  
        rootNode.attachChild(yellowSphereNode);
        
    } //end initYellowEnemy()   
    
    private void initCyanEnemy() {
        
      //initiates the sphere geometry
        Sphere cyanSphere = new Sphere (25,25,25);
        Geometry cyanSphereGeom = new Geometry("Cyan Enemy", cyanSphere);
        cyanSphereGeom.setMaterial(cyanEnemyMat);
        
        cyanSphereController = new RigidBodyControl (10);
        cyanSphereNode.setName("Cyan");
        cyanSphereNode.attachChild(cyanSphereGeom);
        cyanSphereNode.addControl(cyanSphereController);
        
        cyanSphereController.setPhysicsLocation(randomPhy.getRandomYPosVector3f(450, 20, 450));
        cyanSphereController.setLinearVelocity(randomPhy.getRandomVector3f(100, 0, 100));
        
      //adds the collision object to the physics space
        physicsState.getPhysicsSpace().add(cyanSphereController);
        
      //attaches Node to scene  
        rootNode.attachChild(cyanSphereNode);
        
    } //end initCyanEnemy
    
    private void initHealthSphere() {
        
      //initiates the sphere geometry
        Sphere healthSphere = new Sphere (10,10,10);
        Geometry healthSphereGeom = new Geometry("Health Sphere", healthSphere);
        healthSphereGeom.setMaterial(healthMaterial);
        
        healthSphereController = new RigidBodyControl (10);
        healthSphereNode.setName("Health");
        healthSphereNode.attachChild(healthSphereGeom);
        healthSphereNode.addControl(healthSphereController);
          
        healthSphereController.setPhysicsLocation(randomPhy.getRandomYPosVector3f(450, 20, 450));
        healthSphereController.setLinearVelocity(randomPhy.getRandomVector3f(100, 0, 100));
        
      //adds the collision object to the physics space
        physicsState.getPhysicsSpace().add(healthSphereController);
        
      //attaches Node to scene  
        rootNode.attachChild(healthSphereNode);
        
    } //end initHealthSphere  
    
    private void shoot() {
        
      //initializes laser geometry  
        Sphere laserMesh = new Sphere(3,3,3);
        Geometry laserGeom = new Geometry ("Laser",laserMesh);
        laserGeom.setMaterial(laserMaterial);
        laserGeom.setQueueBucket(RenderQueue.Bucket.Transparent);
        
        CollisionShape laserShape = new BoxCollisionShape(new Vector3f(5f,5f,5f));
        laser.setName("Laser");
        laser.attachChild(laserGeom);
        
      //initalizes controllers for laser  
        ProjectileCollisionControl laserControllerTick = new ProjectileCollisionControl(laserShape);
        laserController = new RigidBodyControl(10f);
        laser.addControl(laserController);
        laser.addControl(laserControllerTick);
        physicsState.getPhysicsSpace().add(laserController);
        physicsState.getPhysicsSpace().add(laserControllerTick);
        physicsState.getPhysicsSpace().addTickListener(laserControllerTick);
            
      //sets the shot direction to the direction of the player.  
        laserController.setPhysicsLocation(new Vector3f(cam.getLocation().x, cam.getLocation().y - 2, cam.getLocation().z +15));
        laserController.setLinearVelocity(new Vector3f(cam.getDirection().x, cam.getDirection().y, cam.getDirection().z).mult(1200));
      //plays the shooting sound  
        laserSound.playInstance();
        rootNode.attachChild(laser);
        
      //keeps score
        shotsFired++;
        shotsFiredHUD.setText(Integer.toString(shotsFired));
        
    } //end shoot()

    @Override
    public void simpleRender(RenderManager rm) {
       
    } //end simpleRender
    
    private void createShockwave(){
        
        shockwave = new ParticleEmitter("Shockwave", Type.Triangle, 1 * COUNT_FACTOR);
        shockwave.setFaceNormal(Vector3f.UNIT_Y);
        shockwave.setStartColor(new ColorRGBA(.48f, 0.17f, 0.01f, (float) (.8f / COUNT_FACTOR_F)));
        shockwave.setEndColor(new ColorRGBA(.48f, 0.17f, 0.01f, 0f));
        shockwave.setStartSize(0f);
        shockwave.setEndSize(7f);
        shockwave.setParticlesPerSec(0);
        shockwave.setGravity(0, 0, 0);
        shockwave.setLowLife(0.5f);
        shockwave.setHighLife(0.5f);
        shockwave.setInitialVelocity(new Vector3f(0, 0, 0));
        shockwave.setVelocityVariation(0f);
        shockwave.setImagesX(1);
        shockwave.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/shockwave.png"));
        shockwave.setMaterial(mat);
        explosionNode.attachChild(shockwave);
    } //end createShockwave();
    
    private void createFlash(){
        
        flash = new ParticleEmitter("Flash", EMITTER_TYPE, 24 * COUNT_FACTOR);
        flash.setSelectRandomImage(true);
        flash.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1f / COUNT_FACTOR_F)));
        flash.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        flash.setStartSize(.1f);
        flash.setEndSize(3.0f);
        flash.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        flash.setParticlesPerSec(0);
        flash.setGravity(0, 0, 0);
        flash.setLowLife(.2f);
        flash.setHighLife(.2f);
        flash.setInitialVelocity(new Vector3f(0, 5f, 0));
        flash.setVelocityVariation(1);
        flash.setImagesX(2);
        flash.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flash.png"));
        mat.setBoolean("PointSprite", true);
        flash.setMaterial(mat);
        explosionNode.attachChild(flash);
    } //end createFlash()
    
    private void createFlame(){
        
        flame = new ParticleEmitter("Flame", EMITTER_TYPE, 32 * COUNT_FACTOR);
        flame.setSelectRandomImage(true);
        flame.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (float) (1f / COUNT_FACTOR_F)));
        flame.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        flame.setStartSize(1.3f);
        flame.setEndSize(2f);
        flame.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        flame.setParticlesPerSec(0);
        flame.setGravity(0, -5, 0);
        flame.setLowLife(.4f);
        flame.setHighLife(.5f);
        flame.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 7, 0));
        flame.getParticleInfluencer().setVelocityVariation(1f);
        flame.setImagesX(2);
        flame.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        mat.setBoolean("PointSprite", true);
        flame.setMaterial(mat);
        explosionNode.attachChild(flame);
    } //end createFlame()
    
    private void createSmokeTrail(){
        
        smoketrail = new ParticleEmitter("SmokeTrail", Type.Triangle, 22 * COUNT_FACTOR);
        smoketrail.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1.0f / COUNT_FACTOR_F)));
        smoketrail.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        smoketrail.setStartSize(.2f);
        smoketrail.setEndSize(1f);
        smoketrail.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        smoketrail.setFacingVelocity(true);
        smoketrail.setParticlesPerSec(0);
        smoketrail.setGravity(0, 1, 0);
        smoketrail.setLowLife(.4f);
        smoketrail.setHighLife(.5f);
        smoketrail.setInitialVelocity(new Vector3f(0, 12, 0));
        smoketrail.setVelocityVariation(1);
        smoketrail.setImagesX(1);
        smoketrail.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/smoketrail.png"));
        smoketrail.setMaterial(mat);
        explosionNode.attachChild(smoketrail);
    } //end createSmokeTrail()
    
    /*
     * updateHealthBar() updates the health HUD and the red bar
     */
    private void updateHealthBar(){
        
        Vector3f translation = healthBar.getLocalTranslation();
        guiNode.detachChild(healthBar);
        guiNode.detachChild(healthHUD);
        healthBarBox = new Box(playerHealth, 10, 0);
        healthBar = new Geometry ("Health Bar", healthBarBox);
        healthBar.setMaterial (healthBarMaterial);
        healthBar.setLocalTranslation(translation);
        healthHUD.setText(Integer.toString(playerHealth));
        guiNode.attachChild(healthBar);
        guiNode.attachChild(healthHUD);
        
      //prevents the sound from playing multiple times.  
        boolean isPlaying;
        if (heartBeatSound.getStatus().equals(AudioSource.Status.Playing)) {
            isPlaying = true;
        }
        else {
            isPlaying = false;
        }
        
        
     //tells game to play heart beat if player is at 20 or less health
        if ((playerHealth <= 20) && (!isPlaying)) {
            heartBeatSound.play();
        }
        else if (playerHealth > 20 &&(isPlaying)) {
            heartBeatSound.stop();
        }
        if ((playerHealth <= 0)) {
            endReason = "You died.";
        }
        
    
    } //end updateHealthBar
    
    /*
     * Initializes all sounds
     */
    private void initSounds() {
        
        laserSound = new AudioNode(assetManager, "Sounds/laser1.wav");
        laserSound.setPositional(true);
        laserSound.setVolume(.5f);
        rootNode.attachChild(laserSound);
        
        explosionSound = new AudioNode(assetManager, "Sounds/boom.wav");
        explosionSound.setPositional(false);
        explosionSound.setVolume(.4f);
        rootNode.attachChild(laserSound);
        
        heartBeatSound = new AudioNode(assetManager, "Sounds/heartbeat.wav");
        heartBeatSound.setPositional(false);
        rootNode.attachChild(heartBeatSound);
        heartBeatSound.setLooping(true);
        
        healthSound = new AudioNode(assetManager, "Sounds/healthpickup.wav");
        healthSound.setPositional(false);
        rootNode.attachChild(healthSound);
        
        
        punchSound = new AudioNode(assetManager, "Sounds/punch.wav");
        punchSound.setPositional(false);
        rootNode.attachChild(punchSound);
        
        
    } //end initSound
    
    private void endGame(){
        
        rootNode.detachAllChildren();
        guiNode.detachAllChildren();
        flyCam.setEnabled(false);
        
        
      //displays Game Over text and reason that the game is over
      //eg. "You died." (player ran out of health) or "No time remaining" (5 minutes has passed)
        BitmapText gameOverText = new BitmapText(guiFont);
        gameOverText.setSize(guiFont.getCharSet().getRenderedSize()+10);
        gameOverText.setColor(ColorRGBA.Green);                        
        gameOverText.setText("GAME OVER");          
        gameOverText.setLocalTranslation(((settings.getWidth()/2)-(gameOverText.getLineWidth()/2)),
                (settings.getHeight() - (gameOverText.getLineHeight()/2) - 15), 0);
        guiNode.attachChild(gameOverText);
        
        BitmapText reasonText = new BitmapText(guiFont);
        reasonText.setSize(guiFont.getCharSet().getRenderedSize()+5);
        reasonText.setColor(ColorRGBA.White);
        reasonText.setText(endReason);
        reasonText.setLocalTranslation(((settings.getWidth()/2)-(reasonText.getLineWidth()/2)), 
                (settings.getHeight() - (gameOverText.getLineHeight()) - 25), 0);
        guiNode.attachChild(reasonText);      

        rootNode.attachChild(guiNode);
        
    }
    
    /*
     * Called from simpleUpdate, this method keeps
     * time and ends the game after 5 minutes.
     */
    private void timeKeeper() {
        
        
    }
    
    private void blueShootCollision() {

        laser.detachAllChildren();
        laser.removeControl(laserController);
        rootNode.detachChild(laser);
        laserController.destroy();
        blueSphereController.destroy();
        blueSphereNode.detachAllChildren();
        blueSphereNode.removeControl(blueSphereController);

      //instantiates audio  
        explosionNode.attachChild(explosionSound);
        explosionNode.setLocalTranslation(blueSphereNode.getLocalTranslation());
        rootNode.attachChild(explosionNode);

      //plays shockwave and sound  
        shockwave.emitAllParticles();
        flash.emitAllParticles();
        flame.emitAllParticles();
        smoketrail.emitAllParticles();
        explosionSound.playInstance();      


        //keeps score  
        score = score + 20;
        scoreHUD.setText(Integer.toString(score));
        blueDestroyed++;
        blueHUD.setText(Integer.toString(blueDestroyed));
        hitCount++;
        System.out.println("hit" + hitCount);

        blueCollide=false;
        
      //spawns a new sphere
        initBlueEnemy();
                
    } //end blueCollision
    
    private void redShootCollision() {

        laser.detachAllChildren();
        laser.removeControl(laserController);
        rootNode.detachChild(laser);
        laserController.destroy();
        redSphereController.destroy();
        redSphereNode.detachAllChildren();
        redSphereNode.removeControl(redSphereController);

      //instantiates audio  
        explosionNode.attachChild(explosionSound);
        explosionNode.setLocalTranslation(redSphereNode.getLocalTranslation());
        rootNode.attachChild(explosionNode);

      //plays shockwave and sound  
        shockwave.emitAllParticles();
        flash.emitAllParticles();
        flame.emitAllParticles();
        smoketrail.emitAllParticles();
        explosionSound.playInstance();      


        //keeps score  
        score = score + 20;
        scoreHUD.setText(Integer.toString(score));
        redDestroyed++;
        redHUD.setText(Integer.toString(redDestroyed));
        hitCount++;
        System.out.println("hit" + hitCount);

        redCollide=false;
        
      //spawns a new sphere
        initRedEnemy();
    }
    
    private void yellowShootCollision() {

        laser.detachAllChildren();
        laser.removeControl(laserController);
        rootNode.detachChild(laser);
        laserController.destroy();
        yellowSphereController.destroy();
        yellowSphereNode.detachAllChildren();
        yellowSphereNode.removeControl(yellowSphereController);

      //instantiates audio  
        explosionNode.attachChild(explosionSound);
        explosionNode.setLocalTranslation(yellowSphereNode.getLocalTranslation());
        rootNode.attachChild(explosionNode);

      //plays shockwave and sound  
        shockwave.emitAllParticles();
        flash.emitAllParticles();
        flame.emitAllParticles();
        smoketrail.emitAllParticles();
        explosionSound.playInstance();      

        //keeps score  
        score = score + 20;
        scoreHUD.setText(Integer.toString(score));
        yellowDestroyed++;
        yellowHUD.setText(Integer.toString(yellowDestroyed));
        hitCount++;
        System.out.println("hit" + hitCount);

        yellowCollide=false;
      //spawns a new sphere
        initYellowEnemy();
        
    }
    
    private void cyanShootCollision() {

        laser.detachAllChildren();
        laser.removeControl(laserController);
        rootNode.detachChild(laser);
        laserController.destroy();
        cyanSphereController.destroy();
        cyanSphereNode.detachAllChildren();
        cyanSphereNode.removeControl(cyanSphereController);
        
      //instantiates audio  
        explosionNode.attachChild(explosionSound);
        explosionNode.setLocalTranslation(cyanSphereNode.getLocalTranslation());
        rootNode.attachChild(explosionNode);


      //plays shockwave and sound  
        shockwave.emitAllParticles();
        flash.emitAllParticles();
        flame.emitAllParticles();
        smoketrail.emitAllParticles();
        explosionSound.playInstance();      

        //keeps score  
        score = score + 20;
        scoreHUD.setText(Integer.toString(score));
        cyanDestroyed++;
        cyanHUD.setText(Integer.toString(cyanDestroyed));
        hitCount++;
        System.out.println("hit" + hitCount);

      //spawns a new sphere
        cyanCollide = false;
        initCyanEnemy();

    }
    
    private void healthShootCollision() {
        
        laser.detachAllChildren();
        laser.removeControl(laserController);        
        healthSphereNode.detachAllChildren();       

      //instantiates audio  
        explosionNode.attachChild(explosionSound);
        explosionNode.setLocalTranslation(healthSphereNode.getLocalTranslation());
        rootNode.attachChild(explosionNode);

        punchSound.playInstance();



      //plays shockwave and sound  
        shockwave.emitAllParticles();
        flash.emitAllParticles();
        flame.emitAllParticles();
        smoketrail.emitAllParticles();
        explosionSound.playInstance();

      //keeps score  
        score = score - 5;
        scoreHUD.setText(Integer.toString(score));             

        healthCollide=false;
        
      //spawns new sphere
        initHealthSphere();
    }
    
    private void bluePlayerCollision() {
        
      //garbage collection  
        blueSphereNode.detachAllChildren();
        rootNode.detachChild(blueSphereNode);
        blueSphereNode.removeControl(blueSphereController);
        blueSphereController.destroy();

        playerHealth = playerHealth - 20;

      //updates health bar GUI
        updateHealthBar();
        punchSound.playInstance();

        playerBlueCollide = false;

        initBlueEnemy();
    }
    
    private void redPlayerCollision() {
        
        redSphereNode.detachAllChildren();
        rootNode.detachChild(redSphereNode);
        redSphereNode.removeControl(redSphereController);
        redSphereController.destroy();

        playerHealth = playerHealth - 15;

      //updates health bar GUI
        updateHealthBar();
        punchSound.playInstance();

        playerRedCollide = false;

        initRedEnemy();
    }
    
    private void yellowPlayerCollision() {
        
      //garbage collection  
        yellowSphereNode.detachAllChildren();
        rootNode.detachChild(yellowSphereNode);
        yellowSphereNode.removeControl(yellowSphereController);
        yellowSphereController.destroy();

        playerHealth = playerHealth - 10;

      //updates health bar GUI
        updateHealthBar();
        punchSound.playInstance();

        playerYellowCollide = false;

        initYellowEnemy(); 
    }
    
    private void cyanPlayerCollision() {
        
        cyanSphereNode.detachAllChildren();
        rootNode.detachChild(cyanSphereNode);
        cyanSphereNode.removeControl(cyanSphereController);
        cyanSphereController.destroy();

        playerHealth = playerHealth - 10;

      //updates health bar GUI
        updateHealthBar();
        punchSound.playInstance();

        playerCyanCollide = false;

        initCyanEnemy();
    }
    
    private void healthPlayerCollision() {
        
        healthSphereNode.detachAllChildren();
        healthSphereNode.removeControl(healthSphereController);

      //player's health cannot be over 100.  
        if (playerHealth < 100){
            if (playerHealth <= 90){
                playerHealth = playerHealth+10;
            }
            else if (playerHealth > 90){
                int tempHealth = 100 - playerHealth;
                playerHealth = playerHealth + tempHealth;
            }
        }
      //updates the health bar GUI  
        updateHealthBar();

        healthSound.playInstance();

        playerHealthCollide = false;

        initHealthSphere();
    }
    
    /*
     * Checks for all collisions and performs actions.
     */
    private void collideChecker() {
        
        if (blueCollide){
            blueShootCollision();
        }
        if (redCollide){
            redShootCollision();
        }
        if (yellowCollide){
            yellowShootCollision();
        }
        if (cyanCollide){
            cyanShootCollision();
        }
        if (healthCollide){
            healthShootCollision();
        }
        if (playerBlueCollide){
            bluePlayerCollision();
        }
        if (playerRedCollide){
            redPlayerCollision();
        }
        if (playerYellowCollide){
            yellowPlayerCollision();
        }
        if (playerCyanCollide){
            cyanPlayerCollision();
        }
        if (playerHealthCollide){
            healthPlayerCollision();
        }
    } //end collideChecker
    
    /*
     *  checks the bounds of  the spheres and redraws them if they are out of the arena bounds.
     */
    private void sphereOutofBoundsChecker() {
        if (blueSphereController.getPhysicsLocation().x > 1000
                || blueSphereController.getPhysicsLocation().y > 1000
                || blueSphereController.getPhysicsLocation().y < -10
                || blueSphereController.getPhysicsLocation().z > 1000) {
            
            blueSphereNode.detachAllChildren();
            initBlueEnemy();
        }
        if (redSphereController.getPhysicsLocation().x > 1000
                || redSphereController.getPhysicsLocation().y > 1000
                || redSphereController.getPhysicsLocation().y < -10
                || redSphereController.getPhysicsLocation().z > 1000) {
            
            redSphereNode.detachAllChildren();
            initRedEnemy();
        }
        if (yellowSphereController.getPhysicsLocation().x > 1000
                || yellowSphereController.getPhysicsLocation().y > 1000
                || yellowSphereController.getPhysicsLocation().y < -10
                || yellowSphereController.getPhysicsLocation().z > 1000) {
            
            yellowSphereNode.detachAllChildren();
            initYellowEnemy();
        }
        if (cyanSphereController.getPhysicsLocation().x > 1000
                || cyanSphereController.getPhysicsLocation().y > 1000
                || cyanSphereController.getPhysicsLocation().y < -10
                || cyanSphereController.getPhysicsLocation().z > 1000) {
            
            cyanSphereNode.detachAllChildren();
            initCyanEnemy();
        }
        if (healthSphereController.getPhysicsLocation().x > 1000
                || healthSphereController.getPhysicsLocation().y > 1000
                || healthSphereController.getPhysicsLocation().y < -10
                || healthSphereController.getPhysicsLocation().z > 1000) {
            
            healthSphereNode.detachAllChildren();
            initHealthSphere();
        }
    }
    
    private class ProjectileCollisionControl extends GhostControl implements PhysicsTickListener {
    
        public ProjectileCollisionControl(CollisionShape shape) {
            super(shape);
        }
    
        public void prePhysicsTick (PhysicsSpace space, float tpf) {
        
         }
    
        public void physicsTick(PhysicsSpace space, float tpf) {
            
        
            for (int i = 0; i < getOverlappingObjects().size(); i++){

                if (getOverlappingObjects().get(i).equals(blueSphereController)){
                    physicsState.getPhysicsSpace().remove(laserController);
                    physicsState.getPhysicsSpace().remove(blueSphereController);
                    physicsState.getPhysicsSpace().removeTickListener(this);
                    blueCollide=true; 
                    return;
                }
                if (getOverlappingObjects().get(i).equals(redSphereController)){
                    redCollide=true;
                    physicsState.getPhysicsSpace().remove(laserController);                   
                    physicsState.getPhysicsSpace().remove(redSphereController);
                    return;
                }
                if (getOverlappingObjects().get(i).equals(yellowSphereController)){
                    physicsState.getPhysicsSpace().remove(laserController);   
                    physicsState.getPhysicsSpace().remove(yellowSphereController);
                    physicsState.getPhysicsSpace().removeTickListener(this);
                    yellowCollide=true;
                    return;
                }
                if (getOverlappingObjects().get(i).equals(cyanSphereController)){
                    physicsState.getPhysicsSpace().remove(laserController);   
                    physicsState.getPhysicsSpace().remove(cyanSphereController);
                    physicsState.getPhysicsSpace().removeTickListener(this);
                    cyanCollide=true;
                    return;
                }
                if (getOverlappingObjects().get(i).equals(healthSphereController)){
                    physicsState.getPhysicsSpace().remove(laserController);
                    physicsState.getPhysicsSpace().remove(healthSphereController);
                    physicsState.getPhysicsSpace().removeTickListener(this);
                    healthCollide=true;
                    return;
                }
            } //end for
            
        } //end physicsTick
    
    
    } //end ProjectileCollisionControl
    private class CharacterCollisionControl extends GhostControl implements PhysicsTickListener {
        
        public CharacterCollisionControl(CollisionShape shape) {
            super(shape);
        }        

        public void prePhysicsTick(PhysicsSpace space, float tpf) {
            
        }

        public void physicsTick(PhysicsSpace space, float tpf) {
            
            for (int i = 0; i < getOverlappingObjects().size(); i++){

                if (getOverlappingObjects().get(i).equals(blueSphereController)){
                    physicsState.getPhysicsSpace().remove(blueSphereController);
                    playerBlueCollide = true;
                    return;
                }
                if (getOverlappingObjects().get(i).equals(redSphereController)){                  
                    physicsState.getPhysicsSpace().remove(redSphereController);
                    playerRedCollide = true;
                    return;
                }
                if (getOverlappingObjects().get(i).equals(yellowSphereController)){ 
                    physicsState.getPhysicsSpace().remove(yellowSphereController);
                    playerYellowCollide = true;
                    return;
                }
                if (getOverlappingObjects().get(i).equals(cyanSphereController)){  
                    physicsState.getPhysicsSpace().remove(cyanSphereController);
                    playerCyanCollide = true;
                    return;
                }
                if (getOverlappingObjects().get(i).equals(healthSphereController)){
                    physicsState.getPhysicsSpace().remove(healthSphereController);
                    playerHealthCollide = true;
                    return;
                }
            } //end for            
        }
        
    }
} //end public class


