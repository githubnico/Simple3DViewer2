import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Created by Deviltech on 08.12.2015.
 */
public class UI_2D3D extends Application {

    // mouse origin coordinates
    private double originX, originY;

    // indicates if shift key is pressed
    private boolean isShiftPressed;

    private Camera camera;

    // contains mesh
    private Group subSceneRoot = new Group();

    // contains rectangles
    private Group paneRoot = new Group();

    // button for topPane
    private Button paneButton = new Button("click me");




    @Override
    public void start(Stage primaryStage) throws Exception {


        // create subscene with depth buffer and antialiasing
        SubScene mySubscene = new SubScene(subSceneRoot, 600, 800, true, SceneAntialiasing.BALANCED);

        subSceneRoot.getChildren().add(new Button("click me 2"));

        Pane topPane = new Pane(paneRoot);

        StackPane myStackPane = new StackPane();

        myStackPane.getChildren().addAll(mySubscene, topPane);

        topPane.setPickOnBounds(false);


        // create scene with depth buffer
        Scene scene = new Scene(myStackPane, 600, 800, true);

        // set drag handlers
        scene.setOnMousePressed(sceneOnMousePressedEventHandler);
        scene.setOnMouseDragged(sceneOnMouseDraggedEventHandler);

        // scene key pressed
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SHIFT) {
                isShiftPressed = true;
            }
        });

        // scene key released
        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SHIFT) {
                isShiftPressed = false;
            }
        });

        camera = new PerspectiveCamera(true);

        // boxes and cylinder
        Box myBox1 = new Box(100, 100, 100);
        Box myBox2 = new Box(150, 20, 50);

        Cylinder myCylinder = new Cylinder(10, 200);


        // material black
        PhongMaterial blackMaterial = new PhongMaterial();
        blackMaterial.setDiffuseColor(Color.BLACK);
        blackMaterial.setSpecularColor(Color.DARKGRAY);

        // material green
        PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        // material blue
        PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);


        // set shape properties
        setShapeProperties(myCylinder, 0, 0, 0, greenMaterial, "Cylinder");
        setShapeProperties(myBox1, 0, -100, 0, blackMaterial, "Box1");
        setShapeProperties(myBox2, 0, 100, 0, blueMaterial, "Box2");


        subSceneRoot.getChildren().addAll(myBox1, myBox2, myCylinder);

        // set camera properties
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-500);

        mySubscene.setCamera(camera);


        primaryStage.setScene(scene);
        primaryStage.setTitle("2D and 3D");

        refreshRectangles();


        // show scene
        primaryStage.show();
    }

    /**
     * Sets the properties of the shapes
     * @param myShape
     * @param x
     * @param y
     * @param z
     * @param material
     * @param toolTipText
     * @return
     */
    private void setShapeProperties(Shape3D myShape, double x, double y, double z, PhongMaterial material, String toolTipText ){
        myShape.setMaterial(material);
        myShape.setTranslateX(x);
        myShape.setTranslateY(y);
        myShape.setTranslateZ(z);
        Tooltip.install(
                myShape,
                new Tooltip(toolTipText)
        );
    }


    /**
     * Eventhandler for mouse pressed for circle drag
     */
    EventHandler<MouseEvent> sceneOnMousePressedEventHandler =
            new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {
                    // set origin coordinates
                    originX = t.getSceneX();
                    originY = t.getSceneY();
                }




            };

    /**
     * Eventhandler for mouse follow on drag
     */
    EventHandler<MouseEvent> sceneOnMouseDraggedEventHandler =
            new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {
                    // calculate offset
                    double offsetX = t.getSceneX() - originX;
                    double offsetY = t.getSceneY() - originY;
                    // follow mouse
                    // move left and right
                    if (isShiftPressed) {
                        camera.setTranslateZ(camera.getTranslateZ() + (offsetX + offsetY) / 2);
                    } else {
                        subSceneRoot.getTransforms().add(new Rotate(offsetX, 0, 0, 0, Rotate.Y_AXIS));
                        subSceneRoot.getTransforms().add(new Rotate(offsetY, 0, 0, 0, Rotate.Z_AXIS));
                    }

                    originX += offsetX;
                    originY += offsetY;
                    refreshRectangles();

                }
            };

    /**
     *Generate Rectangle for shape overlay
     * @param shape
     * @return
     */
    public static javafx.scene.shape.Rectangle getBoundingBox2D(Node shape) {
        final Window window = shape.getScene().getWindow();
        final Bounds bounds = shape.getBoundsInLocal();
        final Bounds screenBounds = shape.localToScreen(bounds);
        javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle
                ((int) Math.round(screenBounds.getMinX() - window.getX()),
                        (int) Math.round(screenBounds.getMinY() - window.getY())-20,
                        (int) Math.round(screenBounds.getWidth()),
                        (int) Math.round(screenBounds.getHeight()));
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.LIGHTBLUE);
        rectangle.setMouseTransparent(true);
        return rectangle;
    }

    /**
     * Clear old Rectangles and draw now ones
     */
    private void refreshRectangles(){
        paneRoot.getChildren().clear();
        for(Node current: subSceneRoot.getChildren()){
            Rectangle rect = getBoundingBox2D(current);
            rect.setStroke(Color.DARKGRAY);
            paneRoot.getChildren().add(rect);
        }
        paneRoot.getChildren().add(paneButton);

    }
}
